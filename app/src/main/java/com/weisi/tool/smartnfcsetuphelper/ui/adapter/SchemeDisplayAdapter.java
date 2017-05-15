package com.weisi.tool.smartnfcsetuphelper.ui.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Device;
import com.weisi.tool.smartnfcsetuphelper.bean.Location;
import com.weisi.tool.smartnfcsetuphelper.bean.SetupScheme;

/**
 * Created by CJQ on 2017/5/12.
 */

public class SchemeDisplayAdapter extends RecyclerViewBaseAdapter<SchemeDisplayAdapter.ViewHolder> {

    private final String[] mPositionNames;
    private SetupScheme mScheme;

    public SchemeDisplayAdapter(Context context) {
        mPositionNames = context.getResources().getStringArray(R.array.setup_positions);
    }

    public void setScheme(SetupScheme scheme) {
        mScheme = scheme;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_location_info,
                        parent,
                        false),
                this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Location location = mScheme.getLocations().get(position);
        holder.tvLocationName.setText(location.getName());
        for (int i = 0;i < Location.DEVICE_COUNT;++i) {
            Device device = location.getDevices()[i];
            if (device != null) {
                String positionName = device.getPosition();
                if (TextUtils.isEmpty(positionName)) {
                    holder.tvBleAddresses[i].setText(null);
                } else if (mPositionNames[i].equals(positionName)) {
                    holder.tvBleAddresses[i].setText(device.getBleAddress());
                } else {
                    holder.tvBleAddresses[i].setText("数据异常");
                }
            } else {
                holder.tvBleAddresses[i].setText(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mScheme != null ? mScheme.getLocations().size() : 0;
    }

    public static class ViewHolder extends RecyclerViewBaseAdapter.ViewHolder {

        TextView tvLocationName;
        //TextView[] tvPositions = new TextView[Location.DEVICE_COUNT];
        TextView[] tvBleAddresses = new TextView[Location.DEVICE_COUNT];

        public ViewHolder(View itemView, RecyclerViewBaseAdapter adapter) {
            super(itemView, adapter);
            tvLocationName = (TextView) itemView.findViewById(R.id.tv_location_name);
            //tvPositions[0] = (TextView) itemView.findViewById(R.id.tv_position_a);
            //tvPositions[1] = (TextView) itemView.findViewById(R.id.tv_position_b);
            //tvPositions[2] = (TextView) itemView.findViewById(R.id.tv_position_c);
            tvBleAddresses[0] = (TextView) itemView.findViewById(R.id.tv_ble_a);
            tvBleAddresses[1] = (TextView) itemView.findViewById(R.id.tv_ble_b);
            tvBleAddresses[2] = (TextView) itemView.findViewById(R.id.tv_ble_c);
        }
    }
}
