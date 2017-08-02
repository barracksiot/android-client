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

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.GetDevicePackagesService;
import io.barracks.ota.client.api.GetDevicePackagesRequest;
import io.barracks.ota.client.api.GetDevicePackagesResponse;

/**
 * Created by saiimons on 16-04-07.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class GetDevicePackagesHelperTest {
    @Test
    public void calls() {
        TestCallback callback;

        GetDevicePackagesHelper helper = new GetDevicePackagesHelper("deadbeef");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(GetDevicePackagesService.ACTION_GET)
                        .addCategory(GetDevicePackagesService.GET_DEVICE_PACKAGES_REQUEST_RESPONSE)
                        .putExtra(GetDevicePackagesService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.available);
        Assert.assertFalse(callback.unavailable);
        Assert.assertFalse(callback.error);
        helper.unbind(RuntimeEnvironment.application);

//        callback = new TestCallback();
//        helper.bind(RuntimeEnvironment.application, callback);
//        manager.sendBroadcast(
//                new Intent(GetDevicePackagesService.ACTION_GET)
//                        .addCategory(GetDevicePackagesService.UPDATE_UNAVAILABLE)
//                        .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
//        );
//        Assert.assertTrue(callback.unavailable);
//        Assert.assertFalse(callback.available);
//        Assert.assertFalse(callback.error);
//        helper.unbind(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(GetDevicePackagesService.ACTION_GET)
                        .addCategory(GetDevicePackagesService.GET_DEVICE_PACKAGES_REQUEST_ERROR)
                        .putExtra(GetDevicePackagesService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.error);
        Assert.assertFalse(callback.available);
        Assert.assertFalse(callback.unavailable);
        helper.unbind(RuntimeEnvironment.application);
    }

    @Test
    public void service() {
        GetDevicePackagesRequest request = new GetDevicePackagesRequest.Builder()
                .unitId("HAL")
                .build();
        TestCallback callback = new TestCallback();
        GetDevicePackagesHelper helper = new GetDevicePackagesHelper("deadbeef");
        helper.bind(RuntimeEnvironment.application, callback);
        helper.requestDevicePackages(request);
        Intent intent = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedService();
        Assert.assertNotNull(intent);
        Assert.assertEquals(intent.getComponent().getClassName(), GetDevicePackagesService.class.getName());
        Assert.assertEquals(intent.getAction(), GetDevicePackagesService.ACTION_GET);
        Assert.assertEquals(callback.hashCode(), intent.getIntExtra(GetDevicePackagesService.EXTRA_CALLBACK, 0));
        GetDevicePackagesRequest request2 = intent.getParcelableExtra(GetDevicePackagesService.EXTRA_REQUEST);
        Assert.assertNotNull(request2);
        helper.unbind(RuntimeEnvironment.application);
    }

    private static final class TestCallback implements GetDevicePackagesCallback {
        boolean available = false;
        boolean unavailable = false;
        boolean error = false;

        @Override
        public void onResponse(GetDevicePackagesRequest request, GetDevicePackagesResponse response) {
            available = true;
        }

        @Override
        public void onError(GetDevicePackagesRequest request, Throwable t) {
            error = true;
        }
    }
}
