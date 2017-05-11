package com.weisi.tool.smartnfcsetuphelper.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by KAT on 2017/3/31.
 */
public class Device implements Parcelable {

    private String mBleAddress;
    private String mPosition;

    public Device() {
    }

    public Device(Device other) {
        mBleAddress = other.mBleAddress;
        mPosition = other.mPosition;
    }

    protected Device(Parcel in) {
        mBleAddress = in.readString();
        mPosition = in.readString();
    }

    public String getBleAddress() {
        return mBleAddress;
    }

    public Device setBleAddress(String mBleAddress) {
        this.mBleAddress = mBleAddress;
        return this;
    }

    public String getPosition() {
        return mPosition;
    }

    public Device setPosition(String mPlaceName) {
        this.mPosition = mPlaceName;
        return this;
    }

    @Override
    public int hashCode() {
        return mBleAddress.hashCode() + mPosition.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Device) {
            Device other = (Device)o;
            return TextUtils.equals(mBleAddress, other.mBleAddress) &&
                    TextUtils.equals(mPosition, other.mPosition);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBleAddress);
        dest.writeString(mPosition);
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
