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

public class InstalledPackage extends DevicePackage {

    private String version;

    public InstalledPackage(String reference, String version) {
        super(reference);
        this.version = version;
    }

    private InstalledPackage(Parcel in) {
        super(in);
        version = in.readString();
    }

    public static final Creator<InstalledPackage> CREATOR = new Creator<InstalledPackage>() {
        @Override
        public InstalledPackage createFromParcel(Parcel parcel) {
            return new InstalledPackage(parcel);
        }

        @Override
        public InstalledPackage[] newArray(int i) {
            return new InstalledPackage[0];
        }
    };

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(version);
    }

    public int describeContents(){
        return 0;
    }
}
