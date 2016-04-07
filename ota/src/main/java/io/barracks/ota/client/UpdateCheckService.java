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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

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

    public static final String EXTRA_EXCEPTION = "exception";
    public static final String EXTRA_RESPONSE = "response";
    public static final String EXTRA_CALLBACK = "callback";

    private static final String TAG = UpdateCheckService.class.getSimpleName();

    public UpdateCheckService() {
        super(TAG);
    }

    private void checkUpdate(UpdateCheckRequest request, int callback) {
        Intent intent = new Intent(ACTION_CHECK);
        intent.putExtra(EXTRA_CALLBACK, callback);
        try {
            Log.d(TAG, "Checking for updates");
            GsonBuilder builder = new GsonBuilder();
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(setUpGsonBuilder(builder).create()))
                    .baseUrl(TextUtils.isEmpty(request.getBaseUrl()) ? "https://barracks.io/" : request.getBaseUrl())
                    .build();
            UpdateCheckApi api = retrofit.create(UpdateCheckApi.class);
            Call<UpdateCheckResponse> call = api.checkUpdate(request.getApiKey(), request);
            Response<UpdateCheckResponse> response = call.execute();
            if (response.isSuccessful()) {
                intent.putExtra(EXTRA_RESPONSE, response.body());
            } else {
                intent.putExtra(EXTRA_RESPONSE, UpdateCheckResponse.fromError(response.code() + " " + response.message()));
            }
        } catch (Throwable t) {
            intent.putExtra(UpdateCheckService.EXTRA_EXCEPTION, t);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected GsonBuilder setUpGsonBuilder(GsonBuilder builder) {
        return builder.registerTypeAdapterFactory(this);
    }

    protected TypeAdapter<UpdateCheckResponse> getResponsePropertiesAdapter(Gson gson, TypeToken<UpdateCheckResponse> type) {
        return new DefaultResponseAdapter(this, gson, type);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_CHECK:
                checkUpdate(intent.<UpdateCheckRequest>getParcelableExtra(EXTRA_REQUEST), intent.getIntExtra(EXTRA_CALLBACK, 0));
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
        protected final TypeAdapter<UpdateCheckResponse> delegate;
        protected final TypeAdapter<JsonElement> elementAdapter;

        public DefaultResponseAdapter(TypeAdapterFactory factory, Gson gson, TypeToken<UpdateCheckResponse> type) {
            delegate = gson.getDelegateAdapter(factory, type);
            elementAdapter = gson.getAdapter(JsonElement.class);
        }

        @Override
        public void write(JsonWriter out, UpdateCheckResponse value) throws IOException {
            JsonElement tree = delegate.toJsonTree(value);
            elementAdapter.write(out, tree);
        }

        @Override
        public UpdateCheckResponse read(JsonReader in) throws IOException {
            JsonElement tree = elementAdapter.read(in);
            return delegate.fromJsonTree(tree);
        }
    }
}
