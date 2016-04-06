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
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.barracks.client.ota.BuildConfig;

/**
 * Created by saiimons on 16-04-06.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateCheckRequestTest {
    private static final String DEFAULT_UNIT_ID = "deadbeef";
    private static final String DEFAULT_VERSION_ID = "42";
    private static final String DEFAULT_API_KEY = "badc0fee";
    private static final String DEFAULT_BASE_URL = "http://barracks.io/";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test(expected = IllegalStateException.class)
    public void missingParameter() {
        new UpdateCheckRequest.Builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingApiKey() {
        new UpdateCheckRequest.Builder().baseUrl(DEFAULT_BASE_URL).unitId(DEFAULT_UNIT_ID).versionId(DEFAULT_VERSION_ID).build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingUnitId() {
        new UpdateCheckRequest.Builder().baseUrl(DEFAULT_BASE_URL).apiKey(DEFAULT_API_KEY).versionId(DEFAULT_VERSION_ID).build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingVersionId() {
        new UpdateCheckRequest.Builder().baseUrl(DEFAULT_BASE_URL).apiKey(DEFAULT_API_KEY).unitId(DEFAULT_UNIT_ID).build();
    }

    @Test
    public void correctRequest() {
        UpdateCheckRequest request;
        request = new UpdateCheckRequest.Builder().baseUrl(DEFAULT_BASE_URL).apiKey(DEFAULT_API_KEY).unitId(DEFAULT_UNIT_ID).versionId(DEFAULT_VERSION_ID).build();
        Assert.assertEquals(DEFAULT_BASE_URL, request.getBaseUrl());
        Assert.assertEquals(DEFAULT_API_KEY, request.getApiKey());
        Assert.assertEquals(DEFAULT_UNIT_ID, request.getUnitId());
        Assert.assertEquals(DEFAULT_VERSION_ID, request.getVersionId());
        request = new UpdateCheckRequest.Builder().apiKey(DEFAULT_API_KEY).unitId(DEFAULT_UNIT_ID).versionId(DEFAULT_VERSION_ID).build();
        Assert.assertEquals(DEFAULT_API_KEY, request.getApiKey());
        Assert.assertEquals(DEFAULT_UNIT_ID, request.getUnitId());
        Assert.assertEquals(DEFAULT_VERSION_ID, request.getVersionId());
        Assert.assertTrue(TextUtils.isEmpty(request.getBaseUrl()));
    }

    @Test
    public void parcel() {
        UpdateCheckRequest request = new UpdateCheckRequest.Builder().apiKey(DEFAULT_API_KEY).unitId(DEFAULT_UNIT_ID).versionId(DEFAULT_VERSION_ID).build();

        // Obtain a Parcel object and write the parcelable object to it:
        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        UpdateCheckRequest createdFromParcel = UpdateCheckRequest.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(request.getBaseUrl(), createdFromParcel.getBaseUrl());
        Assert.assertEquals(request.getApiKey(), createdFromParcel.getApiKey());
        Assert.assertEquals(request.getUnitId(), createdFromParcel.getUnitId());
        Assert.assertEquals(request.getVersionId(), createdFromParcel.getVersionId());
    }
}
