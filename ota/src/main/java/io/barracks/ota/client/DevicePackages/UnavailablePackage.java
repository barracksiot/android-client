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

package io.barracks.ota.client.DevicePackages;

import android.os.Parcel;

/**
 * Created by Paul on 17-07-17.
 */

public class UnavailablePackage extends DevicePackage {

    public static final Creator<UnavailablePackage> CREATOR = new Creator<UnavailablePackage>() {
        @Override
        public UnavailablePackage createFromParcel(Parcel parcel) {
            return new UnavailablePackage(parcel);
        }

        @Override
        public UnavailablePackage[] newArray(int i) {
            return new UnavailablePackage[i];
        }
    };

    public UnavailablePackage(String reference) {
        super(reference);
    }

    private UnavailablePackage(Parcel in) {
        super(in);
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }

    public int describeContents() {
        return 0;
    }
}
