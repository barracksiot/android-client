/*
 *    Copyright 2016 Barracks Solutions Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.barracks.ota.client;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDownloadApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by saiimons on 16-04-20.
 */
public class PackageDownloadService extends IntentService {
    public static final String ACTION_DOWNLOAD_PACKAGE = "io.barracks.ota.client.DOWNLOAD_PACKAGE";
    public static final String EXTRA_UPDATE_RESPONSE = "update_response";
    public static final String EXTRA_API_KEY = "apiKey";
    public static final String EXTRA_TMP_DEST = "tmpDest";
    public static final String EXTRA_FINAL_DEST = "finalDest";
    public static final String EXTRA_EXCEPTION = "exception";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_CALLBACK = "callback";
    public static final int MSG_PAUSE_DOWNLOAD = 1;
    public static final int MSG_CANCEL_DOWNLOAD = 2;
    public static final String DOWNLOAD_SUCCESS = "io.barracks.ota.client.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_ERROR = "io.barracks.ota.client.DOWNLOAD_ERROR";
    public static final String DOWNLOAD_PROGRESS = "io.barracks.ota.client.DOWNLOAD_PROGRESS";
    public static final IntentFilter ACTION_DOWNLOAD_PACKAGE_FILTER;

    static {
        ACTION_DOWNLOAD_PACKAGE_FILTER = new IntentFilter(ACTION_DOWNLOAD_PACKAGE);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_SUCCESS);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_ERROR);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_PROGRESS);
    }

    public PackageDownloadService() {
        this(PackageDownloadService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PackageDownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_DOWNLOAD_PACKAGE:
                downloadPackage(
                        intent.getStringExtra(EXTRA_API_KEY),
                        intent.getStringExtra(EXTRA_TMP_DEST),
                        intent.getStringExtra(EXTRA_FINAL_DEST),
                        intent.<UpdateDetails>getParcelableExtra(EXTRA_UPDATE_RESPONSE),
                        intent.getIntExtra(EXTRA_CALLBACK, -1)
                );
                break;
        }
    }

    private void downloadPackage(String apiKey, String tmpDest, String finalDest, UpdateDetails update, int callback) {
        File tmp = TextUtils.isEmpty(tmpDest) ? new File(getFilesDir(), Defaults.DEFAULT_TMP_DL_DESTINATION) : new File(tmpDest);
        File destination = TextUtils.isEmpty(finalDest) ? new File(getFilesDir(), Defaults.DEFAULT_FINAL_DL_DESTINATION) : new File(finalDest);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Defaults.DEFAULT_BASE_URL).build();
        UpdateDownloadApi loader = retrofit.create(UpdateDownloadApi.class);
        Call<ResponseBody> call = loader.downloadUpdate(update.getPackageInfo().getUrl(), apiKey);

        // Setup the files to be loaded and moved
        if (!setupFile(tmp) || !setupFile(destination)) {
            notifyError(new IOException("Failed to setup " + tmp.getPath() + " or " + destination.getPath()), callback);
            return;
        }

        // Initiate the transfer
        OutputStream os = null;
        try {
            os = new FileOutputStream(tmp);
            Response<ResponseBody> response = call.execute();
            if (!response.isSuccessful()) {
                notifyError(new IOException("Call to : " + call.request().url().toString() + " failed : " + response.code() + " " + response.message()), callback);
                return;
            }
            InputStream is = response.body().byteStream();
            int read;
            int total = 0;
            byte buff[] = new byte[1024];
            while ((read = is.read(buff)) != -1) {
                os.write(buff, 0, read);
                total += read;
                notifyProgress((int) (total * 100 / update.getPackageInfo().getSize()), callback);
            }
            checkPackageIntegrity(update, tmp);
            moveToFinalDestination(tmp, destination);
        } catch (IOException | GeneralSecurityException e) {
            notifyError(e, callback);
            return;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        notifySuccess(update, destination, callback);
    }

    private void notifySuccess(UpdateDetails response, File destination, int callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_SUCCESS)
                        .putExtra(EXTRA_UPDATE_RESPONSE, response)
                        .putExtra(EXTRA_CALLBACK, callback)
                        .putExtra(EXTRA_FINAL_DEST, destination.getPath())
        );
    }

    private void notifyError(Exception exception, int callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_ERROR)
                        .putExtra(EXTRA_CALLBACK, callback)
                        .putExtra(EXTRA_EXCEPTION, exception)
        );
    }

    private void notifyProgress(int progress, int callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_PROGRESS)
                        .putExtra(EXTRA_CALLBACK, callback)
                        .putExtra(EXTRA_PROGRESS, progress)
        );
    }

    protected boolean setupFile(File tmp) {
        // Check if the destination exists
        if (tmp.exists()) {
            if (!tmp.delete()) {
                return false;
            }
        }
        // Check if the parent directory exists or can be created and is a directory
        File tmpParent = tmp.getParentFile();
        return (tmpParent.mkdirs() || tmpParent.exists()) && tmpParent.isDirectory();
    }

    protected void checkPackageIntegrity(UpdateDetails response, File f) throws IOException, GeneralSecurityException {
        InputStream is = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            is = new FileInputStream(f);
            is = new DigestInputStream(is, md);
            byte[] buffer = new byte[8192];
            while (is.read(buffer) != -1) {
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (md != null) {
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            if (!sb.toString().equals(response.getPackageInfo().getMd5())) {
                throw new DigestException("Wrong file signature " + sb.toString() + " - " + response.getPackageInfo().getMd5());
            }
        }
    }

    protected void moveToFinalDestination(File tmp, File destination) throws IOException {
        if (!tmp.renameTo(destination)) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(tmp);
                fos = new FileOutputStream(destination);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            } catch (IOException e) {
                throw e;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private class Binder extends android.os.Binder {
        PackageDownloadService getService() {
            // TODO return a wrapper exposing only the necessary methods
            return PackageDownloadService.this;
        }
    }
}
