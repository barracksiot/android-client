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

public class AvailablePackage extends DevicePackage {

    // TODO: 17-07-14 doc
    private String version;

    /**
     * The url to call for downloading the package.
     */
    private String url;
    /**
     * The MD5 hash of the package.
     */
    private String md5;
    /**
     * The size of the package.
     */
    private Long size;

    // TODO: 17-07-14 doc
    private String filename;

    /**
     *
     * @param reference
     * @param version
     * @param url
     * @param md5
     * @param size
     * @param filename
     */
    public AvailablePackage(String reference, String version, String url, String md5, Long size, String filename) {
        super(reference);
        this.version = version;
        this.url = url;
        this.md5 = md5;
        this.size = size;
        this.filename = filename;
    }

    protected AvailablePackage(Parcel in) {
        super(in);
        version = in.readString();
        url= in.readString();
        md5 = in.readString();
        filename = in.readString();
        size = in.readLong();
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getMd5() {
        return md5;
    }

    public Long getSize() {
        return size;
    }

    public String getFilename() {
        return filename;
    }

    public static final Creator<AvailablePackage> CREATOR = new Creator<AvailablePackage>() {
        @Override
        public AvailablePackage createFromParcel(Parcel parcel) {
            return new AvailablePackage(parcel);
        }

        @Override
        public AvailablePackage[] newArray(int i) {
            return new AvailablePackage[i];
        }
    };

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(version);
        parcel.writeString(url);
        parcel.writeString(md5);
        parcel.writeString(filename);
        parcel.writeLong(size);
    }

    public int describeContents(){
        return 0;
    }
}
