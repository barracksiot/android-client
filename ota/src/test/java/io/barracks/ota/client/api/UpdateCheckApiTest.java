package io.barracks.ota.client.api;

import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.Utils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class UpdateCheckApiTest {

    @Before
    public void setup() {

    }

    @Test
    public void checkUpdate_withDefaultUrl_shouldCallCorrectService() throws Exception {
        // Given
        final String defaultPath = "/";
        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(204).addHeader("Content-Type", "application/json; charset=utf-8"));
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url(defaultPath))
                .addConverterFactory(GsonConverterFactory.create(Utils.getRobolectricGson(new GsonBuilder())))
                .build();
        final UpdateCheckApi updateCheckApi = retrofit.create(UpdateCheckApi.class);
        final Call<UpdateDetails> call = updateCheckApi.checkUpdate(
                "deadbeef",
                new UpdateDetailsRequest.Builder().unitId("aaa").versionId("bbb").build()
        );

        // When
        Response<UpdateDetails> response = call.execute();

        // Then
        assertTrue(response.isSuccessful());
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(defaultPath + UpdateCheckApi.ENDPOINT, recordedRequest.getPath());
    }


    @Test
    public void checkUpdate_withCustomUrl_shouldCallCorrectService() throws Exception {
        // Given
        final String customPath = "/custom/";
        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(204).addHeader("Content-Type", "application/json; charset=utf-8"));
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url(customPath))
                .addConverterFactory(GsonConverterFactory.create(Utils.getRobolectricGson(new GsonBuilder())))
                .build();
        final UpdateCheckApi updateCheckApi = retrofit.create(UpdateCheckApi.class);
        final Call<UpdateDetails> call = updateCheckApi.checkUpdate("deadbeef", new UpdateDetailsRequest.Builder().unitId("aaa").versionId("bbb").build());

        // When
        Response<UpdateDetails> response = call.execute();

        // Then
        assertTrue(response.isSuccessful());
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(customPath + UpdateCheckApi.ENDPOINT, recordedRequest.getPath());
    }
}
