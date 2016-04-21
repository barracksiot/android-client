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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.barracks.ota.client.api.UpdateCheckResponse;
import io.barracks.ota.client.api.UpdateDownloadApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by saiimons on 16-04-20.
 */
public class UpdateDownloadService extends IntentService {
    public static final String ACTION_DOWNLOAD_PACKAGE = "io.barracks.ota.client.DOWNLOAD_PACKAGE";
    public static final String EXTRA_UPDATE_RESPONSE = "update_response";
    public static final String EXTRA_API_KEY = "apiKey";
    public static final String EXTRA_TMP_DEST = "tmpDest";
    public static final String EXTRA_FINAL_DEST = "finalDest";
    public static final String EXTRA_EXCEPTION = "exception";
    public static final int MSG_PAUSE_DOWNLOAD = 1;
    public static final int MSG_CANCEL_DOWNLOAD = 2;
    public static final String DOWNLOAD_SUCCESS = "io.barracks.ota.client.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_ERROR = "io.barracks.ota.client.DOWNLOAD_ERROR";
    public static final String DOWNLOAD_PROGRESS = "io.barracks.ota.client.DOWNLOAD_ERROR";
    public static final IntentFilter ACTION_DOWNLOAD_PACKAGE_FILTER;

    static {
        ACTION_DOWNLOAD_PACKAGE_FILTER = new IntentFilter(ACTION_DOWNLOAD_PACKAGE);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_SUCCESS);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_ERROR);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_PROGRESS);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateDownloadService(String name) {
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
                        intent.<UpdateCheckResponse>getParcelableExtra(EXTRA_UPDATE_RESPONSE)
                );
                break;
        }
    }

    private void downloadPackage(String apiKey, String tmpDest, String finalDest, UpdateCheckResponse update) {
        // TODO make a utility function or class to get the temporary and final default locations easily
        File tmp = TextUtils.isEmpty(tmpDest) ? new File(getFilesDir(), Defaults.DEFAULT_TMP_DL_DESTINATION) : new File(tmpDest);
        File destination = TextUtils.isEmpty(finalDest) ? new File(getFilesDir(), Defaults.DEFAULT_FINAL_DL_DESTINATION) : new File(finalDest);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Defaults.DEFAULT_BASE_URL).build();
        UpdateDownloadApi loader = retrofit.create(UpdateDownloadApi.class);
        Call<ResponseBody> call = loader.downloadUpdate(update.getPackageInfo().getUrl(), apiKey);

        // Setup the files to be loaded and moved
        if (!setupFile(tmp) || !setupFile(destination)) {
            notifyError(new Exception("Failed to setup " + tmp.getPath() + " or " + destination.getPath()));
            return;
        }

        // Initiate the transfer
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(tmp));
            InputStream is = call.execute().body().byteStream();
            reader = new BufferedReader(new InputStreamReader(is));
            int read;
            char buff[] = new char[1024];
            while ((read = reader.read(buff)) != -1) {
                writer.write(buff, 0, read);
                // TODO Notify progress
            }
        } catch (IOException e) {
            notifyError(e);
            return;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!checkPackageIntegrity(update, tmp)) {
            notifyError(new Exception("Failed to check package integrity."));
            return;
        }
        if (!moveToFinalDestination(tmp, destination)) {
            notifyError(new Exception("Failed to move package from " + tmp.getPath() + " to " + destination.getPath()));
            return;
        }
        notifySuccess(update, destination);
    }

    private void notifySuccess(UpdateCheckResponse response, File destination) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_SUCCESS)
                        .putExtra(EXTRA_UPDATE_RESPONSE, response)
                        .putExtra(EXTRA_FINAL_DEST, destination.getPath())
        );
    }

    private void notifyError(Exception exception) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_ERROR)
                        .putExtra(EXTRA_EXCEPTION, exception)
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

    protected boolean checkPackageIntegrity(UpdateCheckResponse response, File f) {

        return true;
    }

    protected boolean moveToFinalDestination(File tmp, File destination) {

        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private class Binder extends android.os.Binder {
        UpdateDownloadService getService() {
            // TODO return a wrapper exposing only the necessary methods
            return UpdateDownloadService.this;
        }
    }
}
