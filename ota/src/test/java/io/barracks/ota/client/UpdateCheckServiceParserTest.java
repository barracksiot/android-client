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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class UpdateCheckServiceParserTest {

    @Test
    public void standardParser() throws IOException {
        UpdateCheckService service = new UpdateCheckService();
        checkJsonResponse(service);
    }

    private void checkJsonResponse(UpdateCheckService service) throws IOException {
        final GsonBuilder builder = service.setUpGsonBuilder(new GsonBuilder());
        final Gson gson = Utils.getRobolectricGson(builder);
        final File f = new File(ClassLoader.getSystemResource("update_check_response_success.json").getPath());
        UpdateDetails response = gson.fromJson(new FileReader(f), UpdateDetails.class);
        UpdateDetailsTest.assertValues(response);
        assertCustomUpdateData(response);
    }

    private void assertCustomUpdateData(UpdateDetails response) {
        Map customUpdateData = response.getCustomUpdateData();
        assertTrue((Boolean) customUpdateData.get("boolean"));
        assertEquals(3.14159265d, (Double) customUpdateData.get("double"), 0.0d);
        assertEquals(123.0, (Double) customUpdateData.get("integer"), 0.0d);
        assertTrue("toto".equals(customUpdateData.get("string")));
        Map nestedObject = (Map) customUpdateData.get("object");
        assertFalse((Boolean) nestedObject.get("boolean"));
        assertEquals(6.28318530, (Double) nestedObject.get("double"), 0.0d);
        assertEquals(321.0d, (Double) nestedObject.get("integer"), 0.0d);
        assertTrue("tata".equals(nestedObject.get("string")));
        List nestedArray = (List) customUpdateData.get("array");
        assertEquals(nestedArray.get(0), false);
        assertEquals(nestedArray.get(1), 1.0);
        assertEquals(nestedArray.get(2), "two");
        assertEquals(nestedArray.get(3), 3.14159265);
    }
}
