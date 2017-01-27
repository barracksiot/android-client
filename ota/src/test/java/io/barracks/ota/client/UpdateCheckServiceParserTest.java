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

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsTest;

/**
 * Created by saiimons on 16-04-07.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateCheckServiceParserTest {

    @Test
    public void standardParser() throws IOException {
        UpdateCheckService service = new UpdateCheckService();
        checkJsonResponse(service);
    }

    private void checkJsonResponse(UpdateCheckService service) throws IOException {
        GsonBuilder builder = service.setUpGsonBuilder(new GsonBuilder());
        Gson gson = Utils.getRobolectricGson(builder);
        File f = new File(ClassLoader.getSystemResource("update_check_response_success.json").getPath());
        UpdateDetails response = gson.fromJson(new FileReader(f), UpdateDetails.class);
        UpdateDetailsTest.assertValues(response);
        assertCustomUpdateData(response);
    }

    private void assertCustomUpdateData(UpdateDetails response) {
        Bundle b = response.getCustomUpdateData();
        Assert.assertTrue(b.getBoolean("boolean"));
        Assert.assertEquals(3.14159265d, b.getDouble("double"), 0.0d);
        Assert.assertEquals(123, b.getLong("integer"));
        Assert.assertTrue("toto".equals(b.getString("string")));
        Bundle nestedBundle = b.getBundle("object");
        Assert.assertFalse(nestedBundle.getBoolean("boolean"));
        Assert.assertEquals(6.28318530, nestedBundle.getDouble("double"), 0.0d);
        Assert.assertEquals(321, nestedBundle.getLong("integer"));
        Assert.assertTrue("tata".equals(nestedBundle.getString("string")));
    }
}
