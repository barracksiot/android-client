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
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.Utils;

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

    @Test(expected = IllegalStateException.class)
    public void missingParameter() {
        new UpdateCheckRequest.Builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingApiKey() {
        new UpdateCheckRequest.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingUnitId() {
        new UpdateCheckRequest.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .apiKey(DEFAULT_API_KEY)
                .versionId(DEFAULT_VERSION_ID)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingVersionId() {
        new UpdateCheckRequest.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .apiKey(DEFAULT_API_KEY)
                .unitId(DEFAULT_UNIT_ID)
                .build();
    }

    @Test
    public void correctRequest() {
        UpdateCheckRequest request;
        Bundle properties = new Bundle();
        properties.putString("string", "toto");
        request = new UpdateCheckRequest.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .apiKey(DEFAULT_API_KEY)
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .properties(properties)
                .build();
        Assert.assertEquals(DEFAULT_BASE_URL, request.getBaseUrl());
        Assert.assertEquals(DEFAULT_API_KEY, request.getApiKey());
        Assert.assertEquals(DEFAULT_UNIT_ID, request.getUnitId());
        Assert.assertEquals(DEFAULT_VERSION_ID, request.getVersionId());
        Assert.assertTrue(Utils.compareBundles(properties, request.getProperties()));

        request = new UpdateCheckRequest.Builder()
                .apiKey(DEFAULT_API_KEY)
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .build();
        Assert.assertEquals(DEFAULT_API_KEY, request.getApiKey());
        Assert.assertEquals(DEFAULT_UNIT_ID, request.getUnitId());
        Assert.assertEquals(DEFAULT_VERSION_ID, request.getVersionId());
        Assert.assertTrue(TextUtils.isEmpty(request.getBaseUrl()));
    }

    @Test
    public void parcel() {
        Bundle properties = new Bundle();
        properties.putString("string", "toto");
        UpdateCheckRequest request = new UpdateCheckRequest.Builder()
                .apiKey(DEFAULT_API_KEY)
                .unitId(DEFAULT_UNIT_ID)
                .versionId(DEFAULT_VERSION_ID)
                .properties(properties)
                .build();
        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        UpdateCheckRequest createdFromParcel = UpdateCheckRequest.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(request.getBaseUrl(), createdFromParcel.getBaseUrl());
        Assert.assertEquals(request.getApiKey(), createdFromParcel.getApiKey());
        Assert.assertEquals(request.getUnitId(), createdFromParcel.getUnitId());
        Assert.assertEquals(request.getVersionId(), createdFromParcel.getVersionId());
        Assert.assertTrue(Utils.compareBundles(properties, request.getProperties()));
    }
}
