package com.weisi.tool.smartnfcsetuphelper.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.ui.adapter.RecyclerViewBaseAdapter;
import com.weisi.tool.smartnfcsetuphelper.ui.decoration.SpaceItemDecoration;

/**
 * Created by KAT on 2017/4/11.
 */

public class ListDialog extends BaseDialog implements RecyclerViewBaseAdapter.OnItemClickListener {

    private static final String ARGUMENT_KEY_ITEMS = "items";
    private ItemAdapter mItemAdapter;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.dialog_content_list;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        setExitType(ExitType.NULL);
        RecyclerView rvItems = (RecyclerView)content.findViewById(R.id.rv_items);
        rvItems.addItemDecoration(new SpaceItemDecoration(getResources().
                getDimensionPixelSize(R.dimen.margin_micro_vertical), true));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvItems.setLayoutManager(linearLayoutManager);
        mItemAdapter = new ItemAdapter(getArguments().getStringArray(ARGUMENT_KEY_ITEMS));
        mItemAdapter.setOnItemClickListener(this);
        rvItems.setAdapter(mItemAdapter);
    }

    @Override
    protected void onSetViewData() {

    }

    public void setItems(String[] items) {
        getArguments().putStringArray(ARGUMENT_KEY_ITEMS, items);
    }

    public int show(FragmentTransaction transaction, String tag, String title, String[] items) {
        setItems(items);
        return super.show(transaction, tag, title);
    }

    public void show(FragmentManager manager, String tag, String title, String[] items) {
        setItems(items);
        super.show(manager, tag, title);
    }

    @Override
    public void onItemClick(View item, int position) {
        OnItemSelectedListener listener = getListener(OnItemSelectedListener.class);
        if (listener != null && mItemAdapter != null && mItemAdapter.mItems != null) {
            listener.onItemSelected(this, mItemAdapter.mItems[position]);
        }
        dismiss();
    }

    private static class ItemAdapter extends RecyclerViewBaseAdapter<ItemAdapter.ViewHolder> {

        private String[] mItems;

        public ItemAdapter(String[] items) {
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_item, parent, false), this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.mTvItem.setText(mItems[position]);
        }

        @Override
        public int getItemCount() {
            return mItems != null ? mItems.length : 0;
        }

        public static class ViewHolder extends RecyclerViewBaseAdapter.ViewHolder {

            private TextView mTvItem;

            public ViewHolder(View itemView, ItemAdapter itemAdapter) {
                super(itemView, itemAdapter);
                mTvItem = (TextView)itemView.findViewById(R.id.tv_item);
            }
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ListDialog dialog, String item);
    }
}
