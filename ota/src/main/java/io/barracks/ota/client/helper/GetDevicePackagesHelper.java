/*
 *    Copyright 2017 Barracks Solutions Inc.
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

import io.barracks.ota.client.GetDevicePackagesService;
import io.barracks.ota.client.UpdateCheckService;
import io.barracks.ota.client.api.GetDevicePackagesRequest;
import io.barracks.ota.client.api.GetDevicePackagesResponse;

/**
 * Created by Paul on 17-07-26.
 */

public class GetDevicePackagesHelper extends BroadcastReceiver {
    private final String apiKey;
    private final String baseUrl;
    private Context context;
    private GetDevicePackagesCallback callback;

    /**
     * Helper' constructor
     *
     * @param apiKey  The API key provided by the Barracks platform.
     * @param baseUrl The base URL for the Barracks platform.
     */
    public GetDevicePackagesHelper(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * Helper's contstructor.
     *
     * @param apiKey The API key provided by the Barracks platform.
     */
    public GetDevicePackagesHelper(String apiKey) {
        this(apiKey, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case GetDevicePackagesService.ACTION_GET:
                if (callback.hashCode() == intent.getIntExtra(GetDevicePackagesService.EXTRA_CALLBACK, 0)) {
                    if (intent.hasCategory(GetDevicePackagesService.GET_DEVICE_PACKAGES_REQUEST_ERROR)) {
                        callback.onError(
                                (GetDevicePackagesRequest) intent.getParcelableExtra(GetDevicePackagesService.EXTRA_CALLBACK),
                                (Throwable) intent.getParcelableExtra(GetDevicePackagesService.EXTRA_EXCEPTION)
                        );
                    } else if (intent.hasCategory(GetDevicePackagesService.GET_DEVICE_PACKAGES_REQUEST_RESPONSE)) {
                        callback.onResponse(
                                (GetDevicePackagesRequest) intent.getParcelableExtra(GetDevicePackagesService.EXTRA_CALLBACK),
                                (GetDevicePackagesResponse) intent.getParcelableExtra(GetDevicePackagesService.EXTRA_DEVICE_PACKAGES)
                        );
                    }
                }
                break;
        }
    }

    /**
     * @param context  The context
     * @param callback The {@link GetDevicePackagesCallback} which will be called during the request
     */
    public void bind(Context context, GetDevicePackagesCallback callback) {
        this.context = context;
        this.callback = callback;
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(this, GetDevicePackagesService.ACTION_GET_FILTER);
    }

    /**
     * @param context The context
     */
    public void unbind(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(this);
        this.context = null;
        this.callback = null;
    }

    /**
     * Call this method to request details about the device's packages status to the Barracks platform
     *
     * @param request The request to be sent to the Barracks platform. Detailed in a {@link GetDevicePackagesRequest}
     */
    public void requestDevicePackages(GetDevicePackagesRequest request) {
        Intent intent = new Intent(context, GetDevicePackagesService.class)
                .setAction(GetDevicePackagesService.ACTION_GET)
                .putExtra(UpdateCheckService.EXTRA_API_KEY, apiKey)
                .putExtra(UpdateCheckService.EXTRA_URL, baseUrl)
                .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
                .putExtra(UpdateCheckService.EXTRA_REQUEST, request);
        context.startService(intent);
    }
}
