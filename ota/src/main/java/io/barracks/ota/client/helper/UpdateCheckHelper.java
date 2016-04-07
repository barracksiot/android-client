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
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import io.barracks.ota.client.UpdateCheckService;
import io.barracks.ota.client.api.UpdateCheckRequest;
import io.barracks.ota.client.api.UpdateCheckResponse;

/**
 * Created by saiimons on 16-04-05.
 */
public class UpdateCheckHelper extends BroadcastReceiver {
    private static final String TAG = UpdateCheckHelper.class.getSimpleName();
    private Context context;
    private UpdateCheckCallback callback;

    public UpdateCheckHelper() {
    }

    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received " + intent);
        switch (intent.getAction()) {
            case UpdateCheckService.ACTION_CHECK:
                if (callback.hashCode() == intent.getIntExtra(UpdateCheckService.EXTRA_CALLBACK, 0)) {
                    if (intent.hasCategory(UpdateCheckService.UPDATE_REQUEST_ERROR)) {
                        callback.onUpdateRequestError((Throwable) intent.getSerializableExtra(UpdateCheckService.EXTRA_EXCEPTION));
                    } else if (intent.hasCategory(UpdateCheckService.UPDATE_AVAILABLE)) {
                        callback.onUpdateAvailable((UpdateCheckResponse) intent.getParcelableExtra(UpdateCheckService.EXTRA_RESPONSE));
                    } else if (intent.hasCategory(UpdateCheckService.UPDATE_UNAVAILABLE)) {
                        callback.onUpdateUnavailable();
                    }
                }
                break;
        }
    }

    public synchronized void bind(Context context, UpdateCheckCallback callback) {
        this.context = context;
        this.callback = callback;
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(this, new IntentFilter(UpdateCheckService.ACTION_CHECK));
    }

    public synchronized void unbind(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(this);
        this.context = null;
        this.callback = null;
    }

    public synchronized void requestUpdate(UpdateCheckRequest request) {
        Intent intent = new Intent(context, UpdateCheckService.class)
                .setAction(UpdateCheckService.ACTION_CHECK)
                .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
                .putExtra(UpdateCheckService.EXTRA_REQUEST, request);
        context.startService(intent);
    }
}
