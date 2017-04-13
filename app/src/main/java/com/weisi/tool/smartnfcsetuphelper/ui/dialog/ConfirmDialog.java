package com.weisi.tool.smartnfcsetuphelper.ui.dialog;


import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;

/**
 * Created by KAT on 2016/7/18.
 */
public class ConfirmDialog extends BaseDialog {

    @Override
    protected int getContentLayoutRes() {
        return 0;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {

    }

    @Override
    protected void onSetViewData() {

    }

    public int show(FragmentTransaction transaction, String tag, String title, boolean hasCancelButton) {
        if (!hasCancelButton) {
            setExitType(ExitType.OK);
        }
        return super.show(transaction, tag, title);
    }

    public void show(FragmentManager manager, String tag, String title, boolean hasCancelButton) {
        if (!hasCancelButton) {
            setExitType(ExitType.OK);
        }
        super.show(manager, tag, title);
    }

    public int show(FragmentTransaction transaction, String tag, String title) {
        return show(transaction, tag, title, true);
    }

    public void show(FragmentManager manager, String tag, String title) {
        show(manager, tag, title, true);
    }
}
