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

package io.barracks.ota.client;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String apiKey;

    protected UpdateCheckRequest(Parcel in) {
        apiKey = in.readString();
    }

    public UpdateCheckRequest(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(apiKey);
    }
}
