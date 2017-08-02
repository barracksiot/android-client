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

package io.barracks.ota.client.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Paul on 17-07-14.
 */

public class DevicePackage implements Parcelable{

    public static Creator<DevicePackage> CREATOR = new Creator<DevicePackage>() {
        @Override
        public DevicePackage createFromParcel(Parcel parcel) {
            return new DevicePackage(parcel);
        }

        @Override
        public DevicePackage[] newArray(int i) {
            return new DevicePackage[i];
        }
    };


    /**
     *
     */
    private String reference;

    /**
     *
     */
    private String version;

    public DevicePackage(String reference, String version) {
        this.reference = reference;
        this.version = version;
    }

    protected DevicePackage(Parcel in) {
        reference = in.readString();
        version = in.readString();
    }

    public String getReference() {
        return reference;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(reference);
    }
}
