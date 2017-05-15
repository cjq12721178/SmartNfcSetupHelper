package com.weisi.tool.smartnfcsetuphelper.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.SetupProject;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.ConfirmDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.toast.Prompter;
import com.weisi.tool.smartnfcsetuphelper.util.Logger;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener{

    private static final int REQUEST_CODE_SETUP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Prompter.init(this);
        Logger.register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_setup:
                if (SetupProject.getCurrentProjectName(getApplicationContext()) != null) {
                    startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), REQUEST_CODE_SETUP);
                } else {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.show(getSupportFragmentManager(),
                            "no_project_to_setup",
                            getString(R.string.no_project), false);
                }
                break;
            case R.id.tv_setup_info:
                startActivity(new Intent(MainActivity.this, InformationActivity.class));
                break;
            case R.id.tv_synchronization:
                break;
            case R.id.tv_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
        }
    }
}
