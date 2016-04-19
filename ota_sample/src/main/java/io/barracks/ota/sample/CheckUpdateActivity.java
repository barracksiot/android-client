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

package io.barracks.ota.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.barracks.ota.client.api.UpdateCheckRequest;
import io.barracks.ota.client.api.UpdateCheckResponse;
import io.barracks.ota.client.helper.UpdateCheckCallback;
import io.barracks.ota.client.helper.UpdateCheckHelper;

public class CheckUpdateActivity extends AppCompatActivity {

    private static final String TAG = CheckUpdateActivity.class.getSimpleName();
    private UpdateCheckHelper helper;
    private Button check;
    private EditText version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        check = (Button) findViewById(R.id.btn_check);
        version = (EditText) findViewById(R.id.version);
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper = new UpdateCheckHelper("deadbeef", "http://integration-01.barracks.io/");
        helper.bind(this, new UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateCheckResponse response) {
                Toast.makeText(CheckUpdateActivity.this, "Update available: " + response.getVersionId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpdateUnavailable() {
                Toast.makeText(CheckUpdateActivity.this, "Update unavailable", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpdateRequestError(Throwable t) {
                Toast.makeText(CheckUpdateActivity.this, "Update check failed " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        check.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        helper.requestUpdate(
                                new UpdateCheckRequest.Builder()
                                        .versionId(version.getText().toString())
                                        .unitId("bond007")
                                        .build()
                        );
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        helper.unbind(this);
    }
}
