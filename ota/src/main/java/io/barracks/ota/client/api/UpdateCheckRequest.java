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
import android.text.TextUtils;

/**
 * Created by saiimons on 16-04-05.
 */
public class UpdateCheckRequest implements Parcelable {
    public static final Creator<UpdateCheckRequest> CREATOR = new Creator<UpdateCheckRequest>() {
        @Override
        public UpdateCheckRequest createFromParcel(Parcel in) {
            return new UpdateCheckRequest(in);
        }

        @Override
        public UpdateCheckRequest[] newArray(int size) {
            return new UpdateCheckRequest[size];
        }
    };

    private final String unitId;
    private final String versionId;
    private final Bundle properties;

    protected UpdateCheckRequest(Parcel in) {
        unitId = in.readString();
        versionId = in.readString();
        properties = in.readBundle(getClass().getClassLoader());
    }

    private UpdateCheckRequest(String unitId, String versionId, Bundle properties) {
        this.unitId = unitId;
        this.versionId = versionId;
        this.properties = properties;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getVersionId() {
        return versionId;
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
        dest.writeString(unitId);
        dest.writeString(versionId);
        dest.writeBundle(properties);
    }

    public static final class Builder {
        private String unitId = null;
        private String versionId = null;
        private Bundle properties = null;

        public Builder() {

        }

        public Builder unitId(String unitId) {
            this.unitId = unitId;
            return this;
        }

        public Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder properties(Bundle properties) {
            this.properties = properties;
            return this;
        }

        public UpdateCheckRequest build() {
            if (TextUtils.isEmpty(unitId)) {
                throw new IllegalStateException("Unit ID is required");
            }
            if (TextUtils.isEmpty(versionId)) {
                throw new IllegalStateException("Version ID is required");
            }
            return new UpdateCheckRequest(unitId, versionId, properties);
        }
    }
}
