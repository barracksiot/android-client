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
 * Created by Paul on 17-07-14.
 */

public class ChangedPackage extends AvailablePackage {
    /**
     * @param reference
     * @param version
     * @param url
     * @param md5
     * @param size
     * @param filename
     */
    public ChangedPackage(String reference, String version, String url, String md5, Long size, String filename) {
        super(reference, version, url, md5, size, filename);
    }

    private ChangedPackage(Parcel in) {
        super(in);
    }

    public static final Creator<ChangedPackage> CREATOR = new Creator<ChangedPackage>() {
        @Override
        public ChangedPackage createFromParcel(Parcel parcel) {
            return new ChangedPackage(parcel);
        }

        @Override
        public ChangedPackage[] newArray(int i) {
            return new ChangedPackage[i];
        }
    };

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }

    public int describeContents(){
        return 0;
    }
}
