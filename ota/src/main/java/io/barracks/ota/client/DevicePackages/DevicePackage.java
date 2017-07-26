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
import android.os.Parcelable;

/**
 * Created by Paul on 17-07-14.
 */

public abstract class DevicePackage implements Parcelable{

    /**
     *
     */
    private String reference;

    protected DevicePackage(String reference) {
        this.reference = reference;
    }

    protected DevicePackage(Parcel in) {
        reference = in.readString();
    }

    public String getReference() {
        return reference;
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
