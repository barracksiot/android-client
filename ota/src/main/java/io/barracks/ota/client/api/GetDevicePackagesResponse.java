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

package io.barracks.ota.client.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import io.barracks.ota.client.model.DevicePackage;
import io.barracks.ota.client.model.DownloadablePackage;

/**
 * Created by Paul on 17-07-17.
 */

public class GetDevicePackagesResponse implements Parcelable {

    public static final Creator<GetDevicePackagesResponse> CREATOR = new Creator<GetDevicePackagesResponse>() {
        @Override
        public GetDevicePackagesResponse createFromParcel(Parcel parcel) {
            return new GetDevicePackagesResponse(parcel);
        }

        @Override
        public GetDevicePackagesResponse[] newArray(int i) {
            return new GetDevicePackagesResponse[i];
        }
    };

    private ArrayList<DownloadablePackage> availablePackages = new ArrayList<>();
    private ArrayList<DownloadablePackage> changedPackages = new ArrayList<>();
    private ArrayList<String> unavailablePackages = new ArrayList<>();
    private ArrayList<DevicePackage> unchangedPackages = new ArrayList<>();

    private GetDevicePackagesResponse(Parcel in) {
        in.readTypedList(availablePackages, DownloadablePackage.CREATOR);
        in.readStringList(unavailablePackages);
        in.readTypedList(changedPackages, DownloadablePackage.CREATOR);
        in.readTypedList(unchangedPackages, DevicePackage.CREATOR);

    }

    public GetDevicePackagesResponse(ArrayList<DownloadablePackage> availablePackages, ArrayList<DownloadablePackage> changedPackages, ArrayList<String> unavailablePackages, ArrayList<DevicePackage> unchangedPackages) {
        this.availablePackages = availablePackages;
        this.changedPackages = changedPackages;
        this.unavailablePackages = unavailablePackages;
        this.unchangedPackages = unchangedPackages;
    }

    public ArrayList<DownloadablePackage> getAvailablePackages() {
        return availablePackages;
    }

    public ArrayList<DownloadablePackage> getChangedPackages() {
        return changedPackages;
    }

    public ArrayList<String> getUnavailablePackages() {
        return unavailablePackages;
    }

    public ArrayList<DevicePackage> getUnchangedPackages() {
        return unchangedPackages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}