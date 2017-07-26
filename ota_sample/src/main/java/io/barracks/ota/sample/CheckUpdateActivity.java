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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.barracks.ota.client.DevicePackages.AvailablePackage;
import io.barracks.ota.client.DevicePackages.ChangedPackage;
import io.barracks.ota.client.DevicePackages.DevicePackage;
import io.barracks.ota.client.api.GetDevicePackagesRequest;
import io.barracks.ota.client.api.GetDevicePackagesResponse;
import io.barracks.ota.client.helper.BarracksHelper;
import io.barracks.ota.client.helper.GetDevicePackagesCallback;
import io.barracks.ota.client.helper.GetDevicePackagesHelper;
import io.barracks.ota.client.helper.PackageDownloadCallback;
import io.barracks.ota.client.helper.PackageDownloadHelper;

public class CheckUpdateActivity extends AppCompatActivity {

    private static final String TAG = CheckUpdateActivity.class.getSimpleName();
    private GetDevicePackagesHelper getDevicePackageHelper;
    private PackageDownloadHelper packageDownloadHelper;
    private Button check, download;
    private EditText unitId;
    private TextView details;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        check = (Button) findViewById(R.id.btn_check);
        download = (Button) findViewById(R.id.btn_download);
        unitId = (EditText) findViewById(R.id.version);
        details = (TextView) findViewById(R.id.package_details);
        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BarracksHelper helper = new BarracksHelper("747ae2efa7c0e68fa7dda312aaea2ddeb8bfcffb003c9205b509ae430760cabf", "https://app.barracks.io/");

        getDevicePackageHelper = helper.getUpdateCheckHelper();
        getDevicePackageHelper.bind(this, new GetDevicePackagesCallback() {


            @Override
            public void onResponse(GetDevicePackagesRequest request, GetDevicePackagesResponse response) {

                String availables = "";
                for (DevicePackage p : response.getAvailable()
                        ) {
                    availables += " " + p.getReference();
                }
                String unavailables = "";
                for (DevicePackage p : response.getUnavailable()
                        ) {
                    unavailables += " " + p.getReference();
                }
                String changed = "";
                for (DevicePackage p : response.getChanged()
                        ) {
                    changed += " " + p.getReference();
                }
                String unchanged = "";
                for (DevicePackage p : response.getUnchanged()
                        ) {
                    availables += " " + p.getReference();
                }

                String pkgs = "Available : " + availables + "\n";
                pkgs += "Unavailable : " + unavailables + "\n";
                pkgs += "Changed : " + changed + "\n";
                pkgs += "Unchange : " + unchanged + "\n";

                details.setText(pkgs);
                details.setTag(response);
            }

            @Override
            public void onError(GetDevicePackagesRequest request, Throwable t) {
                details.setText(getString(R.string.get_device_packages_error, t.getMessage()));
                details.setTag(null);
            }
        });

        packageDownloadHelper = helper.getPackageDownloadHelper();
        packageDownloadHelper.bind(this, new PackageDownloadCallback() {
            @Override
            public void onDownloadSuccess(AvailablePackage details, String path) {
                progressBar.setProgress(0);
            }

            @Override
            public void onDownloadFailure(AvailablePackage details, Throwable throwable) {
                progressBar.setProgress(0);
            }

            @Override
            public void onDownloadProgress(AvailablePackage details, int progress) {
                progressBar.setProgress(progress);
            }
        });

        check.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!TextUtils.isEmpty(unitId.getText().toString())){
                            try {
                                Bundle customClientData = new Bundle();
                                Bundle dataMetric = new Bundle();
                                dataMetric.putCharSequence("status", "off");
                                dataMetric.putFloat("temperature", 20.54f);
                                customClientData.putBundle("dataMetric", dataMetric);
                                customClientData.putCharSequence("userStatus", "registered");
                                getDevicePackageHelper.requestDevicePackages(
                                        new GetDevicePackagesRequest.Builder()
                                                .unitId(unitId.getText().toString())
                                                .build()
                                );
                            } catch (Exception e) {
                                details.setText(getString(R.string.get_device_packages_error, e.getMessage()));
                            }
                        }
                    }
                }
        );

        download.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        for (AvailablePackage p : ((GetDevicePackagesResponse) details.getTag()).getAvailable()
                                ) {
                            packageDownloadHelper.requestDownload(p);
                        }

                        for (ChangedPackage p : ((GetDevicePackagesResponse) details.getTag()).getChanged()
                                ) {
                            packageDownloadHelper.requestDownload(p);
                        }
                    }
                }
        );


    }

    @Override
    protected void onPause() {
        super.onPause();
        getDevicePackageHelper.unbind(this);
        packageDownloadHelper.unbind(this);
    }
}
