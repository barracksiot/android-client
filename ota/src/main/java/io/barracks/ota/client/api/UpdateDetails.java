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

package io.barracks.ota.client.api;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by saiimons on 16-04-05.
 */
public class UpdateDetails implements Parcelable {
    public static final Creator<UpdateDetails> CREATOR = new Creator<UpdateDetails>() {
        @Override
        public UpdateDetails createFromParcel(Parcel in) {
            return new UpdateDetails(in);
        }

        @Override
        public UpdateDetails[] newArray(int size) {
            return new UpdateDetails[size];
        }
    };

    private String versionId;
    private PackageInfo packageInfo;
    private Bundle properties = new Bundle();

    private UpdateDetails() {

    }

    protected UpdateDetails(Parcel in) {
        versionId = in.readString();
        packageInfo = in.readParcelable(getClass().getClassLoader());
        properties = in.readBundle(getClass().getClassLoader());
    }

    public String getVersionId() {
        return versionId;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public Bundle getProperties() {
        return properties;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(versionId);
        dest.writeParcelable(packageInfo, 0);
        dest.writeBundle(properties);
    }

}
