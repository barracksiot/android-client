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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ServiceController;

import io.barracks.client.ota.BuildConfig;

import static org.junit.Assert.assertTrue;


/**
 * Created by saiimons on 16-04-06.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateCheckServiceTest {
    ServiceController<UpdateCheckService> controller;
    UpdateCheckService service;
    private LocalBroadcastManager manager;

    @Before
    public void prepare() {
        manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        controller = Robolectric.buildService(UpdateCheckService.class);
        service = controller.attach().create().get();
    }

    @Test
    public void testCallbackRan() {
        CallbackCalledTest testCallback = new CallbackCalledTest();
        manager.registerReceiver(testCallback, new IntentFilter(UpdateCheckService.ACTION_CHECK));
        service.onHandleIntent(new Intent(UpdateCheckService.ACTION_CHECK));
        manager.unregisterReceiver(testCallback);
        assertTrue(testCallback.callbackRan);
    }

    @Test
    public void testMissingParameters() {
        CallbackFailedTest testCallback = new CallbackFailedTest();
        manager.registerReceiver(testCallback, new IntentFilter(UpdateCheckService.ACTION_CHECK));
        service.onHandleIntent(new Intent(UpdateCheckService.ACTION_CHECK));
        manager.unregisterReceiver(testCallback);
        assertTrue(testCallback.callbackFailed);
    }

    @After
    public void finish() {
        controller.destroy();
    }

    private final static class CallbackCalledTest extends BroadcastReceiver {
        boolean callbackRan = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateCheckService.ACTION_CHECK)) {
                callbackRan = true;
            }
        }
    }

    private final static class CallbackFailedTest extends BroadcastReceiver {
        boolean callbackFailed = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateCheckService.ACTION_CHECK) && intent.hasExtra(UpdateCheckService.EXTRA_EXCEPTION)) {
                callbackFailed = true;
            }
        }
    }
}
