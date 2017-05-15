package com.weisi.tool.smartnfcsetuphelper.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Location;

import java.util.List;

/**
 * Created by KAT on 2017/3/31.
 */

public class SchemeSetupAdapter extends RecyclerViewBaseAdapter<SchemeSetupAdapter.ViewHolder> {

    private static final int NORMAL_ITEM_TYPE = 1;
    private static final int FOOTER_ITEM_TYPE = 2;

    private List<Location> mLocations;

    public SchemeSetupAdapter(Context context, List<Location> locations) {
        mLocations = locations;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_location, parent, false),
                this);
        if (viewType == FOOTER_ITEM_TYPE) {
            viewHolder.itemView.setBackgroundResource(R.drawable.selector_location_add);
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.selector_location_normal);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getItemViewType(position) == NORMAL_ITEM_TYPE) {
            Location location = mLocations.get(position);
            holder.mTvLocation.setText(location.getName()
                    + (location.containsFullInfo() ? "（已完成）" : ("（未完成）")));
        }
    }

    @Override
    public int getItemCount() {
        return (mLocations != null ? mLocations.size() : 0) + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position + 1 == getItemCount() ? FOOTER_ITEM_TYPE : NORMAL_ITEM_TYPE;
    }

    public Location getSelectedLocation() {
        int selectedIndex = getSelectedIndex();
        return selectedIndex >= 0 && selectedIndex < mLocations.size() ?
                mLocations.get(selectedIndex) : null;
    }

    public static class ViewHolder extends RecyclerViewBaseAdapter.ViewHolder {

        private TextView mTvLocation;

        public ViewHolder(View itemView, RecyclerViewBaseAdapter adapter) {
            super(itemView, adapter);
            mTvLocation = (TextView)itemView.findViewById(R.id.tv_location);
        }
    }
}
