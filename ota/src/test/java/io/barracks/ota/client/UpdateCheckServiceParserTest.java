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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
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
import io.barracks.ota.client.api.UpdateCheckResponse;
import io.barracks.ota.client.api.UpdateCheckResponseTest;

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

    @Test
    public void customParser() throws IOException {
        UpdateCheckService service = new CustomUpdateCheckService();
        checkJsonResponse(service);
    }

    private void checkJsonResponse(UpdateCheckService service) throws IOException {
        GsonBuilder builder = service.setUpGsonBuilder(new GsonBuilder());
        Gson gson = builder
                .setExclusionStrategies(
                        new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return "__robo_data__".equals(f.getName());
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        }
                )
                .create();
        ClassLoader.getSystemResource("update_check_response_success.json");
        File f = new File(ClassLoader.getSystemResource("update_check_response_success.json").getPath());
        UpdateCheckResponse response = gson.fromJson(new FileReader(f), UpdateCheckResponse.class);
        UpdateCheckResponseTest.assertValues(response);
        assertProperties(response);

        String json = gson.toJson(response);
        response = gson.fromJson(json, UpdateCheckResponse.class);
        UpdateCheckResponseTest.assertValues(response);
        assertProperties(response);
    }

    private void assertProperties(UpdateCheckResponse response) {
        Bundle b = response.getProperties();
        Assert.assertTrue(b.getBoolean("boolean"));
        Assert.assertEquals(3.14159265d, b.getDouble("double"), 0.0d);
        Assert.assertEquals(123, b.getLong("integer"));
        Assert.assertTrue("toto".equals(b.getString("string")));
    }

    private static final class CustomUpdateCheckService extends UpdateCheckService {
        @Override
        protected TypeAdapter<UpdateCheckResponse> getResponsePropertiesAdapter(Gson gson, TypeToken<UpdateCheckResponse> type) {
            return new CustomPropertiesAdapter(this, gson, type);
        }
    }

    private static final class CustomPropertiesAdapter extends UpdateCheckService.DefaultResponseAdapter {

        public CustomPropertiesAdapter(TypeAdapterFactory factory, Gson gson, TypeToken<UpdateCheckResponse> type) {
            super(factory, gson, type);
        }

        @Override
        public void write(JsonWriter out, UpdateCheckResponse response) throws IOException {
            JsonElement tree = getDelegate().toJsonTree(response);
            JsonObject obj = tree.getAsJsonObject();
            JsonObject properties = new JsonObject();
            properties.addProperty("string", response.getProperties().getString("string"));
            properties.addProperty("integer", response.getProperties().getLong("integer"));
            properties.addProperty("boolean", response.getProperties().getBoolean("boolean"));
            properties.addProperty("double", response.getProperties().getBoolean("double"));
            obj.add("properties", properties);
            getElementAdapter().write(out, tree);
        }

        @Override
        public UpdateCheckResponse read(JsonReader in) throws IOException {
            JsonElement tree = getElementAdapter().read(in);
            UpdateCheckResponse response = getDelegate().fromJsonTree(tree);
            response.getProperties().putString("string", "toto");
            response.getProperties().putLong("integer", 123);
            response.getProperties().putBoolean("boolean", true);
            response.getProperties().putDouble("double", 3.14159265d);
            return response;
        }
    }
}
