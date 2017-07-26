/*
 *    Copyright 2017 Barracks Solutions Inc.
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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

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

import io.barracks.ota.client.api.GetDevicePackagesApi;
import io.barracks.ota.client.api.GetDevicePackagesRequest;
import io.barracks.ota.client.api.GetDevicePackagesResponse;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.barracks.ota.client.UpdateCheckService.UPDATE_REQUEST_ERROR;

/**
 * Created by Paul on 17-07-26.
 */

public class GetDevicePackagesService extends IntentService implements TypeAdapterFactory {

    /**
     * Defines the action used to start the request to the Barracks platform.
     *
     * @see Intent#setAction(String)
     */
    public static final String ACTION_GET = "io.barracks.ota.client.GET_DEVICE_PACKAGES";


    /**
     * This key is used to specify the {@link io.barracks.ota.client.api.GetDevicePackagesRequest} used as a reference
     * to call the Barracks platform.
     */
    public static final String EXTRA_REQUEST = "getRequest";
    /**
     * This key is used to specify the url used to call the Barracks platform.
     */
    public static final String EXTRA_URL = "url";
    /**
     * This key is used to specify the API key used for making a request to the Barracks platform.
     */
    public static final String EXTRA_API_KEY = "apiKey";

    /**
     * This key is used to report the callback's identifier
     */
    public static final String EXTRA_CALLBACK = "callback";

    /**
     * This key is used to report an {@link Throwable exception} thrown during the request.
     */
    public static final String EXTRA_EXCEPTION = "exception";

    /**
     * This key is used to report the {@link io.barracks.ota.client.api.GetDevicePackagesResponse} received from the Barracks platform.
     */
    public static final String EXTRA_DEVICE_PACKAGES = "devicePackages";

    /**
     * Category used to notify when a request has failed.
     */
    public static final String GET_DEVICE_PACKAGES_REQUEST_ERROR = "io.barracks.ota.client.update_available.GET_DEVICE_PACKAGES_REQUEST_ERROR";


    /**
     * Category used to notify when a request has failed.
     */
    public static final String GET_DEVICE_PACKAGES_REQUEST_RESPONSE = "io.barracks.ota.client.update_available.GET_DEVICE_PACKAGES_REQUEST_RESPONSE";


    /**
     * Intent filter used by {@link android.content.BroadcastReceiver} to register to the {@link LocalBroadcastManager}
     */
    public static final IntentFilter ACTION_GET_FILTER;

    static {
        ACTION_GET_FILTER = new IntentFilter(ACTION_GET);
        ACTION_GET_FILTER.addCategory(GET_DEVICE_PACKAGES_REQUEST_ERROR);
        ACTION_GET_FILTER.addCategory(GET_DEVICE_PACKAGES_REQUEST_RESPONSE);
    }

    /**
     * Create an {@link GetDevicePackagesService}.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GetDevicePackagesService(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()) {
            case ACTION_GET:
                getDevicePackages(
                        intent.getStringExtra(EXTRA_API_KEY),
                        intent.getStringExtra(EXTRA_URL),
                        intent.<GetDevicePackagesRequest>getParcelableExtra(EXTRA_REQUEST),
                        intent.getIntExtra(EXTRA_CALLBACK, 0)

                );

        }
    }

    private void getDevicePackages(String apiKey, String baseUrl, GetDevicePackagesRequest request, int callback) {
        Intent intent = new Intent(ACTION_GET);
        intent.putExtra(EXTRA_CALLBACK, callback);
        intent.putExtra(EXTRA_REQUEST, request);
        try {
            if (TextUtils.isEmpty(apiKey)) {
                throw new IllegalArgumentException("Missing API key");
            }
            if (TextUtils.isEmpty(baseUrl)) {
                throw new IllegalArgumentException("Missing base URL");
            }
            if (request == null) {
                throw new IllegalArgumentException("Missing request");
            }
            GsonBuilder builder = new GsonBuilder();
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(setUpGsonBuilder(builder).create()))
                    .baseUrl(baseUrl)
                    .build();
            GetDevicePackagesApi api = retrofit.create(GetDevicePackagesApi.class);
            Call<GetDevicePackagesResponse> call = api.getDevicePackages(apiKey, request);
            Response<GetDevicePackagesResponse> response = call.execute();
            if (response.isSuccessful()) {
                GetDevicePackagesResponse getDevicePackagesResponse = response.body();
                intent.addCategory(GET_DEVICE_PACKAGES_REQUEST_RESPONSE);
                intent.putExtra(EXTRA_DEVICE_PACKAGES, getDevicePackagesResponse);
            } else {
                throw new RuntimeException(response.code() + " " + response.message());
            }
        } catch (Throwable t) {
            intent.addCategory(UPDATE_REQUEST_ERROR);
            intent.putExtra(UpdateCheckService.EXTRA_EXCEPTION, t);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    /**
     * This method provides a {@link TypeAdapterFactory} - the service itself - for the {@link GsonBuilder}.
     *
     * @param builder The service's basic {@link GsonBuilder} which will be updated in this method.
     * @return The updated {@link GsonBuilder}
     */
    GsonBuilder setUpGsonBuilder(GsonBuilder builder) {
        return builder.registerTypeAdapterFactory(this);
    }

    /**
     * This method provides a {@link TypeAdapter} for the {@link Bundle}.
     * Override this method to provide a custom implementation, in order to fill the
     * {@link Bundle bundle} with specific values.
     *
     * @param gson The {@link Gson} parser instance.
     * @return The {@link TypeAdapter} for the {@link Bundle}
     * @see GetDevicePackagesService.DefaultBundleAdapter The default implementation.
     */
    private TypeAdapter<Bundle> getBundleAdapter(Gson gson) {
        return new UpdateCheckService.DefaultBundleAdapter(gson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() == Bundle.class) {
            return (TypeAdapter<T>) getBundleAdapter(gson);
        }
        return null;
    }

    /**
     * The default {@link TypeAdapter} for the {@link Bundle}.
     *
     * @see TypeAdapterFactory#create(Gson, TypeToken)
     * @see UpdateCheckService#getBundleAdapter(Gson)
     */
    public static class DefaultBundleAdapter extends TypeAdapter<Bundle> {
        /**
         *
         */
        private final TypeAdapter<JsonElement> elementAdapter;

        public DefaultBundleAdapter(Gson gson) {
            elementAdapter = gson.getAdapter(JsonElement.class);
        }

        /**
         * This method provides basic support for nested Bundles.
         * {@inheritDoc}
         */
        @Override
        public void write(JsonWriter out, Bundle bundle) throws IOException {
            JsonObject jsonObject = bundleToJsonObject(bundle);
            getElementAdapter().write(out, jsonObject);
        }

        private JsonObject bundleToJsonObject(Bundle bundle) {
            JsonObject jsonObject = new JsonObject();
            if(bundle != null) {
                Set<String> keys = bundle.keySet();
                for (String key : keys) {
                    Object value = bundle.get(key);
                    if (Boolean.class.isInstance(value)) {
                        jsonObject.addProperty(key, (Boolean) value);
                    } else if (String.class.isInstance(value)) {
                        jsonObject.addProperty(key, (String) value);
                    } else if (Number.class.isInstance(value)) {
                        jsonObject.addProperty(key, (Number) value);
                    } else if (Bundle.class.isInstance(value)) {
                        jsonObject.add(key, bundleToJsonObject((Bundle) value));
                    }
                }
            }
            return jsonObject;
        }

        /**
         * This method provides basic support for nested bundles.
         * {@inheritDoc}
         */
        @Override
        public Bundle read(JsonReader in) throws IOException {
            JsonElement tree = getElementAdapter().read(in);
            JsonObject obj = tree.isJsonObject() ? tree.getAsJsonObject(): null;
            Bundle bundle = new Bundle();
            if (obj != null) {
                jsonObjectToBundle(bundle, obj);
            }
            return bundle;
        }

        private void jsonObjectToBundle(Bundle bundle, JsonObject jsonObject) {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
                    if (primitive.isBoolean()) {
                        bundle.putBoolean(entry.getKey(), primitive.getAsBoolean());
                    } else if (primitive.isNumber()) {
                        // This number is a LazilyParsedNumber, aka a String, we have to check whether it has a floating
                        Number num = primitive.getAsNumber();
                        try {
                            long longVal = Long.parseLong(num.toString());
                            bundle.putLong(entry.getKey(), longVal);
                        } catch (NumberFormatException e) {
                            double dVal = Double.parseDouble(num.toString());
                            bundle.putDouble(entry.getKey(), dVal);
                        }
                    } else if (primitive.isString()) {
                        bundle.putString(entry.getKey(), primitive.getAsString());
                    }
                }
                else {
                    Bundle entryBundle = new Bundle();
                    bundle.putBundle(entry.getKey(), entryBundle);
                    jsonObjectToBundle(entryBundle, (JsonObject) entry.getValue());
                }

            }
        }

        public TypeAdapter<JsonElement> getElementAdapter() {
            return elementAdapter;
        }
    }

}
