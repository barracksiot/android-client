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
import android.util.Log;
import android.view.View;
import android.widget.Button;

import io.barracks.ota.client.UpdateCheckRequest;
import io.barracks.ota.client.helper.UpdateCheckCallback;
import io.barracks.ota.client.helper.UpdateCheckHelper;

public class CheckUpdateActivity extends AppCompatActivity {

    private static final String TAG = CheckUpdateActivity.class.getSimpleName();
    private UpdateCheckHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        Button check = (Button) findViewById(R.id.btn_check);
        check.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        helper.requestUpdate(
                                new UpdateCheckCallback() {
                                    @Override
                                    public void onCheckSuccess() {
                                        Log.d(TAG, "Update check success");
                                    }

                                    @Override
                                    public void onCheckFailed(Throwable t) {
                                        Log.e(TAG, "Update check failed", t);
                                    }
                                },
                                new UpdateCheckRequest("")
                        );
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper = new UpdateCheckHelper();
        helper.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        helper.unbind(this);
    }
}
