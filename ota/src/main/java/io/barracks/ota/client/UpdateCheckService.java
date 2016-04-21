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

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import io.barracks.ota.client.api.UpdateCheckApi;
import io.barracks.ota.client.api.UpdateCheckRequest;
import io.barracks.ota.client.api.UpdateCheckResponse;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateCheckService extends IntentService implements TypeAdapterFactory {
    public static final String ACTION_CHECK = "io.barracks.ota.client.CHECK_UPDATE";
    public static final String EXTRA_REQUEST = "check_request";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_API_KEY = "apiKey";

    public static final String UPDATE_AVAILABLE = "io.barracks.ota.client.UPDATE_AVAILABLE";
    public static final String UPDATE_UNAVAILABLE = "io.barracks.ota.client.update_available.UPDATE_UNAVAILABLE";
    public static final String UPDATE_REQUEST_ERROR = "io.barracks.ota.client.update_available.UPDATE_REQUEST_ERROR";

    public static final String EXTRA_EXCEPTION = "exception";
    public static final String EXTRA_RESPONSE = "response";
    public static final String EXTRA_CALLBACK = "callback";
    public static final IntentFilter ACTION_CHECK_FILTER;

    static {
        ACTION_CHECK_FILTER = new IntentFilter(ACTION_CHECK);
        ACTION_CHECK_FILTER.addCategory(UPDATE_AVAILABLE);
        ACTION_CHECK_FILTER.addCategory(UPDATE_UNAVAILABLE);
        ACTION_CHECK_FILTER.addCategory(UPDATE_REQUEST_ERROR);
    }

    public UpdateCheckService() {
        this(UpdateCheckService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateCheckService(String name) {
        super(name);
    }

    private void checkUpdate(String apiKey, String baseUrl, UpdateCheckRequest request, int callback) {
        Intent intent = new Intent(ACTION_CHECK);
        try {
            if (TextUtils.isEmpty(apiKey)) {
                throw new RuntimeException("Missing API key");
            }
            if (request == null) {
                throw new RuntimeException("Missing request");
            }
            GsonBuilder builder = new GsonBuilder();
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(setUpGsonBuilder(builder).create()))
                    .baseUrl(TextUtils.isEmpty(baseUrl) ? Defaults.DEFAULT_BASE_URL : baseUrl)
                    .build();
            UpdateCheckApi api = retrofit.create(UpdateCheckApi.class);
            Call<UpdateCheckResponse> call = api.checkUpdate(apiKey, request);
            Response<UpdateCheckResponse> response = call.execute();
            if (response.isSuccessful()) {
                UpdateCheckResponse update = response.body();
                if (update == null) {
                    intent.addCategory(UPDATE_UNAVAILABLE);
                } else {
                    intent.addCategory(UPDATE_AVAILABLE);
                    intent.putExtra(EXTRA_RESPONSE, update);
                }
            } else {
                throw new RuntimeException(response.code() + " " + response.message());
            }
        } catch (Throwable t) {
            intent.addCategory(UPDATE_REQUEST_ERROR);
            intent.putExtra(UpdateCheckService.EXTRA_EXCEPTION, t);
        }
        intent.putExtra(EXTRA_CALLBACK, callback);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected GsonBuilder setUpGsonBuilder(GsonBuilder builder) {
        builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return clazz == Bundle.class;
            }
        });
        return builder.registerTypeAdapterFactory(this);
    }

    protected TypeAdapter<UpdateCheckResponse> getResponsePropertiesAdapter(Gson gson, TypeToken<UpdateCheckResponse> type) {
        return new DefaultResponseAdapter(this, gson, type);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_CHECK:
                checkUpdate(
                        intent.getStringExtra(EXTRA_API_KEY),
                        intent.getStringExtra(EXTRA_URL),
                        intent.<UpdateCheckRequest>getParcelableExtra(EXTRA_REQUEST),
                        intent.getIntExtra(EXTRA_CALLBACK, 0)
                );
                break;
        }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() == UpdateCheckResponse.class) {
            return (TypeAdapter<T>) getResponsePropertiesAdapter(gson, TypeToken.get(UpdateCheckResponse.class));
        }
        return null;
    }

    public static class DefaultResponseAdapter extends TypeAdapter<UpdateCheckResponse> {
        private final TypeAdapter<UpdateCheckResponse> delegate;
        private final TypeAdapter<JsonElement> elementAdapter;

        public DefaultResponseAdapter(TypeAdapterFactory factory, Gson gson, TypeToken<UpdateCheckResponse> type) {
            delegate = gson.getDelegateAdapter(factory, type);
            elementAdapter = gson.getAdapter(JsonElement.class);
        }

        @Override
        public void write(JsonWriter out, UpdateCheckResponse response) throws IOException {
            JsonElement tree = getDelegate().toJsonTree(response);
            JsonObject properties = new JsonObject();
            Set<String> keys = response.getProperties().keySet();
            for (String key : keys) {
                Object value = response.getProperties().get(key);
                if (Boolean.class.isInstance(value)) {
                    properties.addProperty(key, (Boolean) value);
                } else if (String.class.isInstance(value)) {
                    properties.addProperty(key, (String) value);
                } else if (Number.class.isInstance(value)) {
                    properties.addProperty(key, (Number) value);
                }
            }
            tree.getAsJsonObject().add("properties", properties);
            getElementAdapter().write(out, tree);
        }

        @Override
        public UpdateCheckResponse read(JsonReader in) throws IOException {
            JsonElement tree = getElementAdapter().read(in);
            JsonObject obj = tree.getAsJsonObject();
            UpdateCheckResponse response = getDelegate().fromJsonTree(tree);
            JsonObject properties = obj.getAsJsonObject("properties");
            if (properties != null) {
                for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
                        if (primitive.isBoolean()) {
                            response.getProperties().putBoolean(entry.getKey(), primitive.getAsBoolean());
                        } else if (primitive.isNumber()) {
                            // This number is a LazilyParsedNumber, aka a String, we have to check wether it has a floating
                            Number num = primitive.getAsNumber();
                            try {
                                long longVal = Long.parseLong(num.toString());
                                response.getProperties().putLong(entry.getKey(), longVal);
                            } catch (NumberFormatException e) {
                                double dVal = Double.parseDouble(num.toString());
                                response.getProperties().putDouble(entry.getKey(), dVal);
                            }
                        } else if (primitive.isString()) {
                            response.getProperties().putString(entry.getKey(), primitive.getAsString());
                        }
                    }
                }
            }
            return response;
        }

        public TypeAdapter<UpdateCheckResponse> getDelegate() {
            return delegate;
        }

        public TypeAdapter<JsonElement> getElementAdapter() {
            return elementAdapter;
        }
    }
}
