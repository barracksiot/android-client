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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;

public class UpdateCheckService extends Service {
    public static final int ACTION_CHECK = 0;
    public static final String INTENT_ACTION_CHECK = "check_update";

    public static final int RESULT_FAILED = -1;
    public static final int RESULT_SUCCESS = 1;

    public static final String DATA_EXCEPTION = "exception";

    private static final String TAG = UpdateCheckService.class.getSimpleName();
    private final Handler messageHandler = new MessageHandler(this);
    private final Messenger messenger = new Messenger(messageHandler);

    public UpdateCheckService() {
    }

    private static void sendMessage(Messenger messenger, Message msg) {
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to send response", new Exception());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case INTENT_ACTION_CHECK:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkUpdate(UpdateCheckRequest request) {
        Log.d(TAG, "Checking for updates");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private static final class MessageHandler extends Handler {
        private final WeakReference<UpdateCheckService> service;

        private MessageHandler(UpdateCheckService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateCheckService service = this.service.get();
            if (service != null) {
                switch (msg.what) {
                    case ACTION_CHECK:
                        service.checkUpdate((UpdateCheckRequest) msg.obj);
                        Message response = obtainMessage(RESULT_SUCCESS);
                        UpdateCheckService.sendMessage(msg.replyTo, response);
                        break;
                    default:
                        Log.e(TAG, "Failed to handle message " + msg, new Exception());
                        break;
                }
            } else {
                Message response = obtainMessage(RESULT_FAILED);
                Bundle extras = new Bundle();
                extras.putSerializable(UpdateCheckService.DATA_EXCEPTION, new Exception("Unable to handle request"));
                response.setData(extras);
                UpdateCheckService.sendMessage(msg.replyTo, response);
            }
        }
    }


}
