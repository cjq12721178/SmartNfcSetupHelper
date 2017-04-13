package com.weisi.tool.smartnfcsetuphelper.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Device;

import java.util.Arrays;

/**
 * Created by KAT on 2017/4/10.
 */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private static final int NORMAL_ITEM_TYPE = 1;
    private static final int HEADER_ITEM_TYPE = 2;

    private Device[] mDevices;
    private final String[] mPositionNames;
    private final boolean[] mPositionUseFlags;

    public LocationAdapter(Context context) {
        mPositionNames = context.getResources().getStringArray(R.array.setup_positions);
        mPositionUseFlags = new boolean[mPositionNames.length];
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_ITEM_TYPE) {
            ViewHolder holder = new ViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(
                            R.layout.list_item_device_header, parent, false));
            return holder;
        } else {
            final ViewHolder holder = new ViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(
                            R.layout.list_item_device_content, parent, false));
            holder.mSpnPosition = (Spinner)holder.itemView.findViewById(R.id.spn_position);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    parent.getContext(),
                    R.array.setup_positions,
                    R.layout.list_item_position);
            holder.mSpnPosition.setAdapter(adapter);
            holder.mSpnPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int deviceIndex = holder.getLayoutPosition() - 1;
                    if (deviceIndex >= 0 && deviceIndex <= mDevices.length) {
                        Device selectedDevice = mDevices[deviceIndex];
                        String newPosition = holder.mSpnPosition.getItemAtPosition(position).toString();
                        if (selectedDevice != null && !newPosition.equals(selectedDevice.getPosition())) {
                            Device samePositionDevice = findDeviceByPosition(newPosition);
                            if (samePositionDevice != null) {
                                samePositionDevice.setPosition(selectedDevice.getPosition());
                                notifyDataSetChanged();
                            }
                            selectedDevice.setPosition(newPosition);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == NORMAL_ITEM_TYPE) {
            holder.mTvNumber.setText(String.valueOf(position));
            Device device = mDevices[position - 1];
            holder.mTvBleAddress.setText(device.getBleAddress());
            if (!device.getPosition().equals(holder.mSpnPosition.getSelectedItem())) {
                holder.mSpnPosition.setSelection(Arrays.binarySearch(mPositionNames, device.getPosition()));
                //setSpinnerItemSelectedByValue(holder.mSpnPosition, device.getPosition());
            }
//            if (device != null) {
//                holder.mTvBleAddress.setText(device.getBleAddress());
//                if (!device.getPosition().equals(holder.mSpnPosition.getSelectedItem())) {
//                    setSpinnerItemSelectedByValue(holder.mSpnPosition, device.getPosition());
//                }
//            } else {
//                device = new Device();
//                device.setPosition(holder.mSpnPosition.getSelectedItem().toString());
//                mDevices[position - 1] = device;
//                holder.mSpnPosition.setSelection(position - 1);
//            }
        }
    }

    @Override
    public int getItemCount() {
        return (mDevices != null ? mDevices.length : 0) + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? HEADER_ITEM_TYPE : NORMAL_ITEM_TYPE;
    }

    public void setDevices(Device[] devices) {
        mDevices = devices;
        initDevices();
    }

    public void initDevices() {
        if (mDevices != null) {
            for (int i = 0;i < mDevices.length;++i) {
                mDevices[i] = setDeviceDefaultPosition(i);
            }
        }
    }

    private Device setDeviceDefaultPosition(int deviceIndex) {
        Device device = mDevices[deviceIndex];
        if (device == null) {
            device = new Device();
        }
        if (TextUtils.isEmpty(device.getPosition()) ||
                Arrays.binarySearch(mPositionNames, device.getPosition()) < 0) {
            //检查有哪些位置名称已被使用
            Arrays.fill(mPositionUseFlags, false);
            for (int i = 0, positionUseIndex;i < mDevices.length;++i) {
                if (i != deviceIndex &&
                        mDevices[i] != null &&
                        !TextUtils.isEmpty(mDevices[i].getPosition())) {
                    positionUseIndex = Arrays.binarySearch(mPositionNames, mDevices[i].getPosition());
                    if (positionUseIndex >= 0) {
                        mPositionUseFlags[positionUseIndex] = true;
                    }
                }
            }
            //初始化为命名位置信息
            for (int i = 0;i < mPositionUseFlags.length;++i) {
                if (!mPositionUseFlags[i]) {
                    device.setPosition(mPositionNames[i]);
                    break;
                }
            }
        }
        return device;
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

    private void setSpinnerItemSelectedByValue(Spinner spinner, String value){
        SpinnerAdapter apsAdapter= spinner.getAdapter(); //得到SpinnerAdapter对象
        int n = apsAdapter.getCount();
        for (int i = 0;i < n;++i){
            if(value.equals(apsAdapter.getItem(i))){
                spinner.setSelection(i, true);// 默认选中项
                break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvNumber;
        private TextView mTvBleAddress;
        private Spinner mSpnPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvNumber = (TextView)itemView.findViewById(R.id.tv_number);
            mTvBleAddress = (TextView)itemView.findViewById(R.id.tv_ble_address);
        }
    }
}
