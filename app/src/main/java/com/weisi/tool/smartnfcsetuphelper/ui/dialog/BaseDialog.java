package com.weisi.tool.smartnfcsetuphelper.ui.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;


/**
 * Created by KAT on 2016/11/10.
 */
public abstract class BaseDialog extends DialogFragment {

    protected enum ExitType {
        NULL(0),
        OK_CANCEL(R.layout.group_ok_cancel),
        OK(R.layout.group_ok);

        @LayoutRes
        private int resId;

        ExitType(@LayoutRes int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return resId;
        }
    }

    private static final String ARGUMENT_KEY_TITLE = "in_title";
    private static final String ARGUMENT_KEY_EXIT_TYPE = "in_exit_type";
    private int mInterval = 0;

    private View.OnClickListener mOnOkClickListenerInner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onOkClick()) {
                dismiss();
            }
        }
    };

    private View.OnClickListener mOnClickListenerInner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onCancelClick()) {
                dismiss();
            }
        }
    };

    public BaseDialog() {
        setArguments(new Bundle());
        setExitType(ExitType.OK_CANCEL);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setChildViewVerticalIntervalDp(getResources().getDimensionPixelSize(R.dimen.margin_small_vertical));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout liBase = (LinearLayout)inflater.inflate(R.layout.dialog_base, null);
        //设置内容
        int contentLayoutRes = getContentLayoutRes();
        onFindView(contentLayoutRes == 0 ?
                null :
                inflater.inflate(contentLayoutRes, liBase),
                savedInstanceState);
        //设置标题（可选）
        inflateTitle((ViewStub) liBase.findViewById(R.id.vs_title_dialog_base));
        //设置确定/取消按钮及其事件
        ExitType exitType = getExitType();
        if (exitType != ExitType.NULL) {
            View grpOkCancel = inflater.inflate(exitType.getResId(), liBase);
            setExitButton(grpOkCancel, R.id.btn_ok, getOkLabelRes(), mOnOkClickListenerInner);
            if (exitType == ExitType.OK_CANCEL) {
                setExitButton(grpOkCancel, R.id.btn_cancel, getCancelLabelRes(), mOnClickListenerInner);
            }
        }
        //设置子控件垂直间距
        setChildViewVerticalInterval(liBase);
        return liBase;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            onSetViewData();
        }
        super.onViewStateRestored(savedInstanceState);
    }

    private void setExitButton(View group, @IdRes int id, @StringRes int label,
                               View.OnClickListener listener) {
        Button btn = (Button)group.findViewById(id);
        btn.setText(label);
        btn.setOnClickListener(listener);
    }

    protected ExitType getExitType() {
        return (ExitType) getArguments().getSerializable(ARGUMENT_KEY_EXIT_TYPE);
    }

    @StringRes
    protected int getOkLabelRes() {
        return R.string.ok;
    }

    @StringRes
    protected int getCancelLabelRes() {
        return R.string.cancel;
    }

    private void setChildViewVerticalInterval(LinearLayout liBase) {
        if (mInterval > 0) {
            LinearLayout.LayoutParams params;
            for (int i = 0, end = liBase.getChildCount() - 1;i < end;++i) {
                params = (LinearLayout.LayoutParams)liBase.getChildAt(i).getLayoutParams();
                params.setMargins(0, 0, 0, mInterval);
            }
        }
    }

    //注意，设置子view间距大于0时，同一行最好只有一个控件，
    //若有多个，则其高度最好相同（除非其包裹在一个Layout中）。
    //此方法应在show之前调用
    public void setChildViewVerticalIntervalDp(int intervalDp) {
        mInterval = intervalDp;
    }

    private void inflateTitle(ViewStub vsTitle) {
        String title = getArguments().getString(ARGUMENT_KEY_TITLE);
        if (!TextUtils.isEmpty(title)) {
            TextView tvTitle = (TextView) vsTitle.inflate();
            tvTitle.setText(title);
        }
    }

    public void show(FragmentManager manager, String tag, String title) {
        setTitle(title);
        super.show(manager, tag);
    }

    public int show(FragmentTransaction transaction, String tag, String title) {
        setTitle(title);
        return super.show(transaction, tag);
    }

    public void setTitle(String title) {
        getArguments().putString(ARGUMENT_KEY_TITLE, title);
    }

    public void setExitType(ExitType type) {
        getArguments().putSerializable(ARGUMENT_KEY_EXIT_TYPE, type);
    }

    protected abstract @LayoutRes int getContentLayoutRes();

    //绑定布局中的view，给view设置固定数据（变化数据在此设置将自动恢复原数据）
    protected abstract void onFindView(View content, @Nullable Bundle savedInstanceState);

    //给view设置变动数据
    protected abstract void onSetViewData();

    protected boolean onOkClick() {
        OnOkClickListener listener = getListener(OnOkClickListener.class);
        return listener != null ? listener.onOkClick(this) : true;
    }

    protected boolean onCancelClick() {
        OnCancelClickListener listener = getListener(OnCancelClickListener.class);
        if (listener != null) {
            listener.onCancelClick(this);
        }
        return true;
    }

    protected <L> L getListener(Class<L> c) {
        if (c == null)
            return null;
        Fragment fragment = getParentFragment();
        if (c.isInstance(fragment)) {
            return (L)fragment;
        }
        Activity activity = getActivity();
        if (c.isInstance(activity)) {
            return (L)activity;
        }
        return null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onCancelClick();
    }

    public interface OnOkClickListener {
        boolean onOkClick(BaseDialog dialog);
    }

    public interface OnCancelClickListener {
        void onCancelClick(BaseDialog dialog);
    }
}
