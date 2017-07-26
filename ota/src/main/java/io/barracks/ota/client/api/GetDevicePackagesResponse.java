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

import io.barracks.ota.client.DevicePackages.AvailablePackage;
import io.barracks.ota.client.DevicePackages.ChangedPackage;
import io.barracks.ota.client.DevicePackages.UnavailablePackage;
import io.barracks.ota.client.DevicePackages.UnchangedPackage;

/**
 * Created by Paul on 17-07-17.
 */

public class GetDevicePackagesResponse implements Parcelable {

    public static final Creator<GetDevicePackagesResponse> CREATOR = new Creator<GetDevicePackagesResponse>() {
        @Override
        public GetDevicePackagesResponse createFromParcel(Parcel parcel) {
            return null;
        }

        @Override
        public GetDevicePackagesResponse[] newArray(int i) {
            return new GetDevicePackagesResponse[0];
        }
    };

    private ArrayList<AvailablePackage> available;
    private ArrayList<ChangedPackage> changed;
    private ArrayList<UnavailablePackage> unavailable;
    private ArrayList<UnchangedPackage> unchanged;

    private GetDevicePackagesResponse(Parcel in) {
        in.readTypedList(available, AvailablePackage.CREATOR);
        in.readTypedList(unavailable, UnavailablePackage.CREATOR);
        in.readTypedList(changed, ChangedPackage.CREATOR);
        in.readTypedList(unchanged, UnchangedPackage.CREATOR);

    }

    public GetDevicePackagesResponse(ArrayList<AvailablePackage> available, ArrayList<ChangedPackage> changed, ArrayList<UnavailablePackage> unavailable, ArrayList<UnchangedPackage> unchanged) {
        this.available = available;
        this.changed = changed;
        this.unavailable = unavailable;
        this.unchanged = unchanged;
    }


    public ArrayList<AvailablePackage> getAvailable() {
        return available;
    }

    public ArrayList<ChangedPackage> getChanged() {
        return changed;
    }

    public ArrayList<UnavailablePackage> getUnavailable() {
        return unavailable;
    }

    public ArrayList<UnchangedPackage> getUnchanged() {
        return unchanged;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}