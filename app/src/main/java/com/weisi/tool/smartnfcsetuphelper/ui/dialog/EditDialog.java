package com.weisi.tool.smartnfcsetuphelper.ui.dialog;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.weisi.tool.smartnfcsetuphelper.R;


/**
 * Created by KAT on 2016/11/25.
 */
public class EditDialog extends BaseDialog {

    public interface OnContentReceiver {
        boolean onReceive(EditDialog dialog, String oldValue, String newValue);
    }

    private static final String ARGUMENT_KEY_CONTENT = "in_content";
    private EditText mEtText;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.dialog_content_edit;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        mEtText = (EditText)content.findViewById(R.id.il_text);
        mEtText.setText(getArguments().getString(ARGUMENT_KEY_CONTENT));
    }

    @Override
    protected void onSetViewData() {
    }

    @Override
    protected boolean onOkClick() {
        OnContentReceiver receiver = getListener(OnContentReceiver.class);
        return receiver != null ?
                receiver.onReceive(this,
                        getArguments().
                        getString(ARGUMENT_KEY_CONTENT),
                        mEtText.getText().toString()) :
                true;
    }

    public void setContent(String content) {
        getArguments().putString(ARGUMENT_KEY_CONTENT, content);
    }

    public void show(FragmentManager manager, String tag, String title, String content) {
        setContent(content);
        super.show(manager, tag, title);
    }

    public int show(FragmentTransaction transaction, String tag, String title, String content) {
        setContent(content);
        return super.show(transaction, tag, title);
    }
}
