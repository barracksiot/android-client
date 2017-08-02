package io.barracks.ota.client.api;

import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.GetDevicePackagesService;
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

/**
 * Created by saiimons on 14/09/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class GetDevicePackagesApiTest {

    @Before
    public void setup() {

    }

    @Test
    public void getDevicePackages_withDefaultUrl_shouldCallCorrectService() throws Exception {
        // Given
        final String defaultPath = "/";
        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
                        .setResponseCode(204)
                        .addHeader("Content-Type", "application/json; charset=utf-8"));

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url(defaultPath))
                .addConverterFactory(GsonConverterFactory.create(Utils.getRobolectricGson(new GsonBuilder())))
                .build();

        final GetDevicePackagesApi getDevicePackagesApi = retrofit.create(GetDevicePackagesApi.class);
        final Call<GetDevicePackagesResponse> call = getDevicePackagesApi.getDevicePackages("deadbeef", new GetDevicePackagesRequest.Builder().unitId("aaa").build());

        // When
        Response<GetDevicePackagesResponse> response = call.execute();

        // Then
        assertTrue(response.isSuccessful());
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(defaultPath + GetDevicePackagesApi.ENDPOINT, recordedRequest.getPath());
    }


    @Test
    public void getDevicePackages_withCustomUrl_shouldCallCorrectService() throws Exception {
        assertTrue(true);
        // Given
        final String customPath = "/custom/";
        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setResponseCode(204)
                .addHeader("Content-Type", "application/json; charset=utf-8"));

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url(customPath))
                .addConverterFactory(GsonConverterFactory.create(Utils.getRobolectricGson(new GsonBuilder())))
                .build();

        final GetDevicePackagesApi getDevicePackagesApi = retrofit.create(GetDevicePackagesApi.class);
        final Call<GetDevicePackagesResponse> call = getDevicePackagesApi.getDevicePackages("deadbeef", new GetDevicePackagesRequest.Builder().unitId("aaa").build());

        // When
        Response<GetDevicePackagesResponse> response = call.execute();

        // Then
        assertTrue(response.isSuccessful());
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(customPath + GetDevicePackagesApi.ENDPOINT, recordedRequest.getPath());
    }
}
