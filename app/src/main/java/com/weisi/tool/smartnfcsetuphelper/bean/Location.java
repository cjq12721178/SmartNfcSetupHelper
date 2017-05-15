package com.weisi.tool.smartnfcsetuphelper.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Arrays;

/**
 * Created by KAT on 2017/3/31.
 */
public class Location implements Parcelable {

    public static final int DEVICE_COUNT = 3;
    private String mName;
    private Device[] mDevices = new Device[DEVICE_COUNT];

    public Location(String name) {
        setName(name);
    }

    public Location(Location other) {
        mName = other.mName;
        for (int i = 0;i < DEVICE_COUNT;++i) {
            if (other.mDevices[i] != null) {
                mDevices[i] = new Device(other.mDevices[i]);
            }
        }
    }

    protected Location(Parcel in) {
        mName = in.readString();
        mDevices = in.createTypedArray(Device.CREATOR);
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        if (TextUtils.isEmpty(name))
            throw new NullPointerException();
        mName = name;
    }

    public Device[] getDevices() {
        return mDevices;
    }

    public boolean containsFullInfo() {
        for (Device device :
                mDevices) {
            if (device == null)
                return false;
            String bleAddress = device.getBleAddress();
            if (TextUtils.isEmpty(bleAddress))
                return false;
            if (bleAddress.length() != 6 &&
                    bleAddress.length() != 8)
                return false;
            if (TextUtils.isEmpty(device.getPosition()))
                return false;
        }
        return true;
    }

    public Device findDeviceByPosition(String position) {
        if (TextUtils.isEmpty(position))
            return null;
        for (Device device :
                mDevices) {
            if (device != null && position.equals(device.getPosition()))
                return device;
        }
        return null;
    }

    public void sortDevices() {
        Arrays.sort(mDevices);
    }

    public void clearDeviceInfo() {
        Arrays.fill(mDevices, null);
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() == o.getClass()) {
            Location other = (Location)o;
            if (!mName.equals(other.mName))
                return false;
            return Arrays.equals(mDevices, other.mDevices);
        } else if (o instanceof String) {
            String otherName = (String)o;
            return mName.equals(otherName);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeTypedArray(mDevices, flags);
    }
}
