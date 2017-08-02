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

package io.barracks.ota.client.api;

import android.os.Bundle;
import android.os.Parcel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.model.DevicePackage;
import io.barracks.ota.client.Utils;

/**
 * Created by saiimons on 16-04-06.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class GetDevicePackagesRequestTest {
    private static final String DEFAULT_UNIT_ID = "deadbeef";

    @Test(expected = IllegalStateException.class)
    public void missingParameter() {
        new GetDevicePackagesRequest.Builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingUnitId() {
        new GetDevicePackagesRequest.Builder()
                .build();
    }


    @Test
    public void correctRequest() {
        GetDevicePackagesRequest request;
        Bundle customClientData = new Bundle();
        customClientData.putString("string", "toto");

        ArrayList<DevicePackage> installedPackages = new ArrayList<>();
        installedPackages.add(new DevicePackage("a.pkg.ref","1.0"));

        request = new GetDevicePackagesRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .customClientData(customClientData)
                .installedPackages(installedPackages)
                .build();

        Assert.assertEquals(DEFAULT_UNIT_ID, request.getUnitId());
        Assert.assertTrue(Utils.compareBundles(customClientData, request.getCustomClientData()));

        request = new GetDevicePackagesRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .build();
        Assert.assertEquals(DEFAULT_UNIT_ID, request.getUnitId());

        // TODO: 17-07-31 ADD Installed packages tests
    }

    @Test
    public void parcel() {
        Bundle customClientData = new Bundle();
        customClientData.putString("string", "toto");

        ArrayList<DevicePackage> installedPackages = new ArrayList<>();
        installedPackages.add(new DevicePackage("a.pkg.ref","1.0"));

        GetDevicePackagesRequest request = new GetDevicePackagesRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .customClientData(customClientData)
                .installedPackages(installedPackages)
                .build();
        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        GetDevicePackagesRequest createdFromParcel = GetDevicePackagesRequest.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(request.getUnitId(), createdFromParcel.getUnitId());
        Assert.assertTrue(Utils.compareBundles(customClientData, request.getCustomClientData()));

        // TODO: 17-07-31 add installed packages tests
    }
}
