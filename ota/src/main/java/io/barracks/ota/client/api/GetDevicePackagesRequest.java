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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;

import io.barracks.ota.client.model.DevicePackage;

/**
 * Created by Paul on 17-07-17.
 */

public class GetDevicePackagesRequest implements Parcelable {

    public static final Creator<GetDevicePackagesRequest> CREATOR = new Creator<GetDevicePackagesRequest>() {
        @Override
        public GetDevicePackagesRequest createFromParcel(Parcel in) {
            return new GetDevicePackagesRequest(in);
        }

        @Override
        public GetDevicePackagesRequest[] newArray(int size) {
            return new GetDevicePackagesRequest[size];
        }
    };
    /**
     * The unique identifier for the unit which is requesting the details.
     */
    private final String unitId;
    /**
     * A {@link Bundle} of user-defined customClientData.
     */
    private final Bundle customClientData;
    /**
     * The list of package installed packages
     */
    private ArrayList<DevicePackage> installedPackages;

    /**
     * Parcelable constructor
     *
     * @param in The parcel to read from
     * @see Parcelable
     */
    protected GetDevicePackagesRequest(Parcel in) {
        unitId = in.readString();
        if (installedPackages != null && installedPackages.size() > 0) {
            in.readTypedList(installedPackages, DevicePackage.CREATOR);
        }
        customClientData = in.readBundle(getClass().getClassLoader());
    }

    /**
     * @see Builder
     */
    private GetDevicePackagesRequest(ArrayList<DevicePackage> installedPackages, String unitId, Bundle customClientData) {
        this.installedPackages = installedPackages;
        this.unitId = unitId;
        this.customClientData = customClientData;
    }

    public String getUnitId() {
        return unitId;
    }

    public Bundle getCustomClientData() {
        return customClientData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(unitId);
        parcel.writeBundle(customClientData);
        parcel.writeTypedList(installedPackages);
    }


    public static final class Builder {
        private String unitId = null;
        private ArrayList<DevicePackage> installedPackages = new ArrayList<>();
        private Bundle customClientData = null;

        /**
         * Builder constructor
         */
        public Builder() {

        }

        /**
         * @param unitId
         * @return
         */
        public Builder unitId(String unitId) {
            this.unitId = unitId;
            return this;
        }

        /**
         * @param installedPackages
         * @return
         */
        public Builder installedPackages(ArrayList<DevicePackage> installedPackages) {
            this.installedPackages = installedPackages;
            return this;
        }

        /**
         * @param customClientData
         * @return
         */
        public Builder customClientData(Bundle customClientData) {
            this.customClientData = customClientData;
            return this;
        }


        public GetDevicePackagesRequest build() throws IllegalStateException {
            if (TextUtils.isEmpty(unitId)) {
                throw new IllegalStateException("Unit ID is required");
            }
            return new GetDevicePackagesRequest(installedPackages, unitId, customClientData);
        }
    }

}
