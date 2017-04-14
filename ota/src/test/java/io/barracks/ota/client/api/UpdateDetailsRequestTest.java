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

import android.os.Parcel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.barracks.client.ota.BuildConfig;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class UpdateDetailsRequestTest {
    private static final String DEFAULT_UNIT_ID = "deadbeef";
    private static final String DEFAULT_VERSION_ID = "42";

    @Test(expected = IllegalStateException.class)
    public void missingParameter() {
        new UpdateDetailsRequest.Builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingUnitId() {
        new UpdateDetailsRequest.Builder()
                .versionId(DEFAULT_VERSION_ID)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingVersionId() {
        new UpdateDetailsRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .build();
    }

    @Test
    public void correctRequest() {
        final Map<String, Object> customClientData = new HashMap<>();
        customClientData.put("string", "toto");
        customClientData.put("array", Arrays.asList(false, 1.0, "two", 3.14159265));
        final UpdateDetailsRequest requestWithClientData = new UpdateDetailsRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .customClientData(customClientData)
                .build();

        Assert.assertEquals(DEFAULT_UNIT_ID, requestWithClientData.getUnitId());
        Assert.assertEquals(DEFAULT_VERSION_ID, requestWithClientData.getVersionId());
        Assert.assertEquals(customClientData, requestWithClientData.getCustomClientData());

        final UpdateDetailsRequest requestWithoutClientData = new UpdateDetailsRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .build();
        Assert.assertEquals(DEFAULT_UNIT_ID, requestWithoutClientData.getUnitId());
        Assert.assertEquals(DEFAULT_VERSION_ID, requestWithoutClientData.getVersionId());
    }

    @Test
    public void parcel() {
        final Map<String, Object> customClientData = new HashMap<>();
        customClientData.put("string", "toto");
        customClientData.put("array", Arrays.asList(false, 1.0, "two", 3.14159265));
        final UpdateDetailsRequest request = new UpdateDetailsRequest.Builder()
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .customClientData(customClientData)
                .build();
        final Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        final UpdateDetailsRequest createdFromParcel = UpdateDetailsRequest.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(request.getUnitId(), createdFromParcel.getUnitId());
        Assert.assertEquals(request.getVersionId(), createdFromParcel.getVersionId());
        Assert.assertEquals(request.getCustomClientData(), createdFromParcel.getCustomClientData());
    }
}
