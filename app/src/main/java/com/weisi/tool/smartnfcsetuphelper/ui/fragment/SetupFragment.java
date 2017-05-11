package com.weisi.tool.smartnfcsetuphelper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Device;
import com.weisi.tool.smartnfcsetuphelper.bean.Location;
import com.weisi.tool.smartnfcsetuphelper.nfc.NfcReader;
import com.weisi.tool.smartnfcsetuphelper.ui.adapter.LocationAdapter;
import com.weisi.tool.smartnfcsetuphelper.ui.decoration.SpaceItemDecoration;
import com.weisi.tool.smartnfcsetuphelper.util.Logger;


/**
 * Created by KAT on 2017/4/10.
 */

public class SetupFragment extends Fragment implements NfcReader.OnDataReceivedListener {

    private static final String ARGUMENT_KEY_LOCATION = "location";
    //private Location mLocation;
    private LocationAdapter mLocationAdapter;
    private StringBuilder mBuilder = new StringBuilder();
    private NfcReader mNfcReader;
    private Location mLocation = new Location("位置");

    public void setLocation(Location location) {
        mLocation = new Location(location);
    }

    public Location getLocation() {
        return mLocation;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        view.findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocation.clearDeviceInfo();
                mLocationAdapter.initDevices();
                mLocationAdapter.notifyDataSetChanged();
                mNfcReader.resume();
            }
        });
        RecyclerView rvLocation = (RecyclerView)view.findViewById(R.id.rl_location);
        rvLocation.addItemDecoration(new SpaceItemDecoration(getResources().
                getDimensionPixelSize(R.dimen.margin_small_vertical), true));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvLocation.setLayoutManager(linearLayoutManager);
        mLocationAdapter = new LocationAdapter(getActivity());
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getParcelable(ARGUMENT_KEY_LOCATION);
            if (mLocation != null) {
                mLocationAdapter.setDevices(mLocation.getDevices());
            }
        }
        rvLocation.setAdapter(mLocationAdapter);
        initNfcReader();
        return view;
    }

    @Override
    public void onDestroy() {
        mNfcReader.stop();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARGUMENT_KEY_LOCATION, mLocation);
        super.onSaveInstanceState(outState);
    }

    private void initNfcReader() {
        mNfcReader = new NfcReader();
        mNfcReader.setOnDataReceivedListener(this);
        mNfcReader.init(getActivity());
        mNfcReader.autoRead(0, 4);
        mNfcReader.start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            mNfcReader.pause();
        } else {
            mLocationAdapter.setDevices(mLocation.getDevices());
            mLocationAdapter.notifyDataSetChanged();
            mNfcReader.resume();
        }
    }

    @Override
    public void onReceived(byte[] data) {
        if (data != null && data.length == 4) {
            int operation = setDeviceBleAddress(toBleAddressString(data, 0, data[3] == 0 ? 3 : 4));
            if (operation == 0) {
                getActivity().runOnUiThread(mUpdateDeviceBleAddress);
            } else if (operation == 2) {
                mNfcReader.pause();
            }
        }
    }

    private Runnable mUpdateDeviceBleAddress = new Runnable() {
        @Override
        public void run() {
            mLocationAdapter.notifyDataSetChanged();
        }
    };

    //返回值为0，则设备BLE地址设置成功
    //返回值为1，则该BLE地址已存在
    //返回值为2，则所有设备均已设置了BLE地址
    private int setDeviceBleAddress(String bleAddress) {
        for (Device device :
                mLocation.getDevices()) {
            if (device != null) {
                if (TextUtils.isEmpty(device.getBleAddress())) {
                    device.setBleAddress(bleAddress);
                    return 0;
                } else if (bleAddress.equals(device.getBleAddress())) {
                    return 1;
                }
            }
        }
        return 2;
    }

    private String toBleAddressString(byte[] data, int start, int end) {
        mBuilder.setLength(0);
        for (int i = start;i < end;++i) {
            mBuilder.append(String.format("%02X", data[i] & 0xff));
        }
        return mBuilder.toString();
    }
}
