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

package io.barracks.ota.client.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import io.barracks.ota.client.PackageDownloadService;
import io.barracks.ota.client.UpdateCheckService;
import io.barracks.ota.client.api.UpdateCheckResponse;

/**
 * Created by saiimons on 26/04/2016.
 */
public class PackageDownloadHelper extends BroadcastReceiver {
    private static final String TAG = PackageDownloadHelper.class.getSimpleName();
    private final String apiKey;
    private Context context;
    private PackageDownloadCallback callback;


    public PackageDownloadHelper(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case PackageDownloadService.ACTION_DOWNLOAD_PACKAGE:
                if (callback.hashCode() == intent.getIntExtra(UpdateCheckService.EXTRA_CALLBACK, 0)) {
                    if (intent.hasCategory(PackageDownloadService.DOWNLOAD_PROGRESS)) {
                        callback.onDownloadProgress(intent.<UpdateCheckResponse>getParcelableExtra(PackageDownloadService.EXTRA_UPDATE_RESPONSE), intent.getIntExtra(PackageDownloadService.EXTRA_PROGRESS, 0));
                    } else if (intent.hasCategory(PackageDownloadService.DOWNLOAD_SUCCESS)) {
                        callback.onDownloadSuccess(intent.<UpdateCheckResponse>getParcelableExtra(PackageDownloadService.EXTRA_UPDATE_RESPONSE));
                    } else if (intent.hasCategory(PackageDownloadService.DOWNLOAD_ERROR)) {
                        callback.onDownloadFailure((Throwable) intent.getSerializableExtra(PackageDownloadService.EXTRA_EXCEPTION));
                    }
                }
                break;
        }
    }

    public void bind(Context context, PackageDownloadCallback callback) {
        this.context = context;
        this.callback = callback;
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(this, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
    }

    public void unbind(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(this);
        this.context = null;
        this.callback = null;
    }

    public void requestDownload(UpdateCheckResponse response) {
        requestDownload(response, null, null);
    }

    public void requestDownload(UpdateCheckResponse response, String tmpFile, String finalFile) {
        Intent intent = new Intent(context, PackageDownloadService.class)
                .setAction(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                .putExtra(PackageDownloadService.EXTRA_API_KEY, apiKey)
                .putExtra(PackageDownloadService.EXTRA_TMP_DEST, tmpFile)
                .putExtra(PackageDownloadService.EXTRA_FINAL_DEST, finalFile)
                .putExtra(PackageDownloadService.EXTRA_CALLBACK, callback.hashCode())
                .putExtra(PackageDownloadService.EXTRA_UPDATE_RESPONSE, response);
        context.startService(intent);
    }
}
