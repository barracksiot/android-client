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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import io.barracks.ota.client.api.UpdateCheckApi;
import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsRequest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This service is used to handle the request to the barracks service in the background.<br>
 * It uses the {@link LocalBroadcastManager} to send updates about the request, using the categories
 * for {@link UpdateCheckService#UPDATE_AVAILABLE an available update},
 * {@link UpdateCheckService#UPDATE_UNAVAILABLE no update available} and
 * {@link UpdateCheckService#UPDATE_REQUEST_ERROR failure} for a defined {@link UpdateCheckService#ACTION_CHECK action}
 *
 * @see io.barracks.ota.client.helper.BarracksHelper
 */
public class UpdateCheckService extends IntentService implements TypeAdapterFactory {
    /**
     * Defines the action used to start the request to the Barracks platform.
     *
     * @see Intent#setAction(String)
     */
    public static final String ACTION_CHECK = "io.barracks.ota.client.CHECK_UPDATE";

    /**
     * This key is used to specify the {@link UpdateDetailsRequest request} used as a reference
     * to call the Barracks platform.
     */
    public static final String EXTRA_REQUEST = "check_request";
    /**
     * This key is used to specify the url used to call the Barracks platform.
     */
    public static final String EXTRA_URL = "url";
    /**
     * This key is used to specify the API key used for making a request to the Barracks platform.
     */
    public static final String EXTRA_API_KEY = "apiKey";

    /**
     * Category used to notify when some {@link UpdateDetails} are available.
     */
    public static final String UPDATE_AVAILABLE = "io.barracks.ota.client.UPDATE_AVAILABLE";
    /**
     * Category used to notify when no details are available.
     */
    public static final String UPDATE_UNAVAILABLE = "io.barracks.ota.client.update_available.UPDATE_UNAVAILABLE";
    /**
     * Category used to notify when a request has failed.
     */
    public static final String UPDATE_REQUEST_ERROR = "io.barracks.ota.client.update_available.UPDATE_REQUEST_ERROR";

    /**
     * This key is used to report an {@link Throwable exception} thrown during the request.
     */
    public static final String EXTRA_EXCEPTION = "exception";
    /**
     * This key is used to report the {@link UpdateDetails details} received from the Barracks platform.
     */
    public static final String EXTRA_UPDATE_DETAILS = "updateDetails";
    /**
     * This key is used to report the callback's identifier
     */
    public static final String EXTRA_CALLBACK = "callback";

    /**
     * Intent filter used by {@link android.content.BroadcastReceiver} to register to the {@link LocalBroadcastManager}
     */
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
     * Creates an {@link UpdateCheckService}.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateCheckService(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_CHECK:
                checkUpdate(
                        intent.getStringExtra(EXTRA_API_KEY),
                        intent.getStringExtra(EXTRA_URL),
                        intent.<UpdateDetailsRequest>getParcelableExtra(EXTRA_REQUEST),
                        intent.getIntExtra(EXTRA_CALLBACK, 0)
                );
                break;
        }
    }

    /**
     * This method processes the request and notifies the rest of the application using the {@link LocalBroadcastManager}.
     *
     * @param apiKey   The API key provided by the Barracks platform.
     * @param baseUrl  The url used to call the Barracks platform
     * @param request  The {@link UpdateDetailsRequest request} parameters for the Barracks platform.
     * @param callback The callback identifier.
     */
    private void checkUpdate(String apiKey, String baseUrl, UpdateDetailsRequest request, int callback) {
        Intent intent = new Intent(ACTION_CHECK);
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
            UpdateCheckApi api = retrofit.create(UpdateCheckApi.class);
            Call<UpdateDetails> call = api.checkUpdate(apiKey, request);
            Response<UpdateDetails> response = call.execute();
            if (response.isSuccessful()) {
                UpdateDetails update = response.body();
                if (update == null) {
                    intent.addCategory(UPDATE_UNAVAILABLE);
                } else {
                    intent.addCategory(UPDATE_AVAILABLE);
                    intent.putExtra(EXTRA_UPDATE_DETAILS, update);
                }
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
     * {@inheritDoc}
     */
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return null;
    }
}
