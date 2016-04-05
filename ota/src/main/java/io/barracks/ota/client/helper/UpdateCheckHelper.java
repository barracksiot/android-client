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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import io.barracks.ota.client.UpdateCheckRequest;
import io.barracks.ota.client.UpdateCheckService;

/**
 * Created by saiimons on 16-04-05.
 */
public class UpdateCheckHelper implements ServiceConnection {
    private static final String TAG = UpdateCheckHelper.class.getSimpleName();
    private final ArrayList<Message> queue = new ArrayList<>();
    private boolean isBound = false;
    private Messenger messenger;


    public UpdateCheckHelper() {
    }

    @Override
    public synchronized void onServiceDisconnected(ComponentName name) {
        messenger = null;
        isBound = false;
    }

    public synchronized void bind(Context context) {
        context.bindService(new Intent(context, UpdateCheckService.class), this, Context.BIND_AUTO_CREATE);
    }

    public synchronized void unbind(Context context) {
        context.unbindService(this);
    }

    public synchronized void requestUpdate(UpdateCheckCallback callback, UpdateCheckRequest request) {
        Message msg = Message.obtain(null, UpdateCheckService.ACTION_CHECK, request);
        msg.replyTo = new Messenger(new CallbackHandler(callback));
        if (isBound) {
            sendMessage(msg);
        } else {
            queue.add(msg);
        }
    }

    private void sendMessage(Message msg) {
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            try {
                Bundle extras = new Bundle();
                extras.putSerializable(UpdateCheckService.DATA_EXCEPTION, e);
                Message err = Message.obtain(null, UpdateCheckService.RESULT_FAILED);
                err.setData(extras);
                msg.replyTo.send(err);
            } catch (RemoteException e1) {
                Log.e(TAG, "Callback failed", e1);
            }
        }
    }

    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        messenger = new Messenger(service);
        isBound = true;
        for (Message msg : queue) {
            sendMessage(msg);
        }
    }

    private static final class CallbackHandler extends Handler {
        private final UpdateCheckCallback callback;

        private CallbackHandler(UpdateCheckCallback callback) {
            this.callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what > 0) {
                callback.onCheckSuccess();
            } else {
                Throwable exception = (Throwable) msg.getData().getSerializable(UpdateCheckService.DATA_EXCEPTION);
                callback.onCheckFailed(exception);
            }
        }
    }
}
