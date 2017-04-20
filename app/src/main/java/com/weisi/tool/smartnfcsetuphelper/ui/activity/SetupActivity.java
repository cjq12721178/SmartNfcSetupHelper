package com.weisi.tool.smartnfcsetuphelper.ui.activity;

import android.support.annotation.StringRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Environment;
import com.weisi.tool.smartnfcsetuphelper.bean.Location;
import com.weisi.tool.smartnfcsetuphelper.bean.SetupProject;
import com.weisi.tool.smartnfcsetuphelper.bean.SetupScheme;
import com.weisi.tool.smartnfcsetuphelper.ui.adapter.SchemeAdapter;
import com.weisi.tool.smartnfcsetuphelper.ui.adapter.RecyclerViewBaseAdapter;
import com.weisi.tool.smartnfcsetuphelper.ui.decoration.SpaceItemDecoration;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.BaseDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.ConfirmDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.EditDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.ListDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.fragment.SetupFragment;
import com.weisi.tool.smartnfcsetuphelper.ui.toast.Prompter;
import com.weisi.tool.smartnfcsetuphelper.util.Logger;

import java.util.List;

public class SetupActivity
        extends AppCompatActivity
        implements RecyclerViewBaseAdapter.OnItemClickListener,
        RecyclerViewBaseAdapter.OnItemLongClickListener,
        BaseDialog.OnOkClickListener,
        BaseDialog.OnCancelClickListener,
        EditDialog.OnContentReceiver,
        ListDialog.OnItemSelectedListener, View.OnClickListener {

    private static final String ARGUMENT_KEY_SCHEME = "scheme";
    private static final String ARGUMENT_KEY_SELECTED_INDEX = "selected_index";
    private static final String ARGUMENT_KEY_SCHEME_CHANGED = "changed";
    private static final String DIALOG_TAG_IS_START_NEW_SCHEME = "is_start_new_scheme";
    private static final String DIALOG_TAG_SET_SCHEME_NAME = "set_scheme_name";
    private static final String DIALOG_TAG_SET_LOCATION_NAME = "set_location_name";
    private static final String DIALOG_TAG_EXIT_AND_SAVE_SCHEME = "exit_and_save_scheme";
    private static final String DIALOG_TAG_CHECK_LOCATION_INFO = "check_location_info";
    private static final String DIALOG_TAG_IS_SAVE_SCHEME = "is_save_scheme";
    private static final String DIALOG_TAG_SELECT_SCHEME = "select_scheme";
    private static final String DIALOG_TAG_RENAME_LOCATION = "rename_location";
    private static final String ARGUMENT_KEY_SCHEMES_NAME = "schemes_name";

    private SetupScheme mSetupScheme;
    private SchemeAdapter mSchemeAdapter;
    private SetupFragment mSetupFragment;
    private boolean mSchemeChanged;
    private PopupWindow mPwLocationOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initSetupFragment();
        initSetupScheme(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARGUMENT_KEY_SELECTED_INDEX,
                mSchemeAdapter == null ? -1 : mSchemeAdapter.getSelectedIndex());
        outState.putParcelable(ARGUMENT_KEY_SCHEME, mSetupScheme);
        outState.putBoolean(ARGUMENT_KEY_SCHEME_CHANGED, mSchemeChanged);
        super.onSaveInstanceState(outState);
    }

    private void initSetupFragment() {
        mSetupFragment = (SetupFragment) getSupportFragmentManager().findFragmentById(R.id.fm_setup);
        showSetupFragment(false);
    }

    private void showSetupFragment(boolean isShow) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isShow) {
            transaction.show(mSetupFragment);
        } else {
            transaction.hide(mSetupFragment);
        }
        transaction.commit();
    }

    private void initSetupScheme(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final String[] schemesName = Environment.getSchemesName(this, SetupProject.getCurrentProjectName(this));
            if (schemesName.length > 0) {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.getArguments().putStringArray(ARGUMENT_KEY_SCHEMES_NAME, schemesName);
                dialog.show(getSupportFragmentManager(),
                        DIALOG_TAG_IS_START_NEW_SCHEME,
                        getString(R.string.is_create_scheme));
            } else {
                startNewScheme();
            }
        } else {
            onSchemeInitCompleted(savedInstanceState);
        }
    }

    private void startNewScheme() {
        EditDialog editDialog = new EditDialog();
        editDialog.show(getSupportFragmentManager(),
                DIALOG_TAG_SET_SCHEME_NAME,
                getString(R.string.name_scheme),
                getDefaultSetupName());
    }

    private boolean judgeInputSchemeNameEmpty(String schemeName) {
        return judgeInputNameEmpty(schemeName, R.string.scheme_name_can_not_be_null);
    }

    private boolean judgeInputLocationNameEmpty(String locationName) {
        return judgeInputNameEmpty(locationName, R.string.location_name_can_not_be_empty);
    }

    private boolean judgeInputNameEmpty(String name, @StringRes int promptInfoStrId) {
        if (TextUtils.isEmpty(name)) {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.show(getSupportFragmentManager(),
                    "check_name_empty",
                    getString(promptInfoStrId),
                    false);
            return false;
        }
        return true;
    }

    private boolean judgeInputLocationNameDuplicated(String locationName) {
        if (mSetupScheme.containsLocation(locationName)) {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.show(getSupportFragmentManager(),
                    "check_location_name_duplicated",
                    getString(R.string.location_name_can_not_be_duplicated),
                    false);
            return false;
        }
        return true;
    }

    private String getDefaultSetupName() {
        return SetupProject.getCurrentProjectName(this) + "方案";
    }

    private void createSetupScheme(String schemeName) {
        mSetupScheme =  SetupScheme.from(SetupProject.getCurrentProject(SetupActivity.this), schemeName);
    }

    private void importSetupScheme(String schemeName) {
        mSetupScheme = SetupScheme.importScheme(this, SetupProject.getCurrentProjectName(this), schemeName);
        if (mSetupScheme == null) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.show(getSupportFragmentManager(), getString(R.string.import_scheme_failed));
        }
    }

    private void onSchemeInitCompleted(Bundle savedInstanceState) {
        int selectedLocationIndex;
        if (savedInstanceState != null) {
            mSetupScheme = savedInstanceState.getParcelable(ARGUMENT_KEY_SCHEME);
            //当处于scheme初始化阶段发生意外情况
            if (mSetupScheme == null)
                return;
            selectedLocationIndex = savedInstanceState.getInt(ARGUMENT_KEY_SELECTED_INDEX);
            mSchemeChanged = savedInstanceState.getBoolean(ARGUMENT_KEY_SCHEME_CHANGED);
        } else {
            selectedLocationIndex = -1;
        }
        if (mSetupScheme == null) {
            mSetupScheme = new SetupScheme("未命名安装方案");
        }
        setTitle(mSetupScheme.getName());
        initLocationInfoListView(selectedLocationIndex);
    }

    private void initLocationInfoListView(int selectedLocationIndex) {
        RecyclerView rvScheme = (RecyclerView)findViewById(R.id.rv_scheme);
        rvScheme.addItemDecoration(new SpaceItemDecoration(getResources().
                getDimensionPixelSize(R.dimen.margin_small_vertical), true));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvScheme.setLayoutManager(linearLayoutManager);
        mSchemeAdapter = new SchemeAdapter(this, mSetupScheme.getLocations());
        mSchemeAdapter.setUpdateSelectedState(true);
        mSchemeAdapter.setSelectedIndex(selectedLocationIndex);
        mSchemeAdapter.setOnItemClickListener(this);
        mSchemeAdapter.setOnItemLongClickListener(this);
        rvScheme.setAdapter(mSchemeAdapter);
    }

    @Override
    public void onItemClick(View item, int position) {
        if (position == mSetupScheme.getLocations().size()) {
            EditDialog editDialog = new EditDialog();
            editDialog.show(getSupportFragmentManager(),
                    DIALOG_TAG_SET_LOCATION_NAME,
                    getString(R.string.input_location_name),
                    getString(R.string.new_location));
        } else {
            Location location = mSetupScheme.getLocations().get(position);
            mSetupFragment.setLocation(location);
            showSetupFragment(true);
            setTitle(location.getName());
        }
    }

    @Override
    public void onItemLongClick(View item, int position) {
        if (mPwLocationOperation == null) {
            View popupView = LayoutInflater.from(this).inflate(R.layout.popup_location_operate, null);
            mPwLocationOperation = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            mPwLocationOperation.setContentView(popupView);
            popupView.findViewById(R.id.tv_rename_location).setOnClickListener(this);
            popupView.findViewById(R.id.tv_delete_location).setOnClickListener(this);
            popupView.findViewById(R.id.tv_cancel).setOnClickListener(this);
        }
        mPwLocationOperation.showAsDropDown(item, item.getWidth() / 2, -item.getHeight() / 2);
    }

    @Override
    public void onBackPressed() {
        if (mSetupFragment.isHidden()) {
            if (mSetupScheme.allLocationHasFullInfo()) {
                saveSchemeAndExit();
            } else {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.show(getSupportFragmentManager(),
                        DIALOG_TAG_EXIT_AND_SAVE_SCHEME,
                        getString(R.string.locations_lack_info));
            }
        } else {
            Location currentLocation = mSetupFragment.getLocation();
            if (currentLocation.containsFullInfo()) {
                onBackSetupActivity();
            } else {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.show(getSupportFragmentManager(),
                        DIALOG_TAG_CHECK_LOCATION_INFO,
                        getString(R.string.location_contains_unfull_info));
            }
        }
    }

    private void saveSchemeAndExit() {
        if (mSetupScheme.exists(this, SetupProject.getCurrentProjectName(this)) &&
                mSchemeChanged) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.show(getSupportFragmentManager(),
                    DIALOG_TAG_IS_SAVE_SCHEME,
                    getString(R.string.scheme_has_changed));
        } else {
            saveSchemeAndExitImp();
        }
    }

    private void saveSchemeAndExitImp() {
        if (mSetupScheme.saveToLocal(this, SetupProject.getCurrentProjectName(this))) {
            Prompter.show(R.string.scheme_save_success);
        } else {
            Logger.record(getString(R.string.scheme_save_failed));
        }
        super.onBackPressed();
    }

    private void onBackSetupActivity() {
        showSetupFragment(false);
        setTitle(mSetupScheme.getName());
        int selectedIndex = mSchemeAdapter.getSelectedIndex();
        List<Location> locations = mSetupScheme.getLocations();
        if (selectedIndex > -1 && selectedIndex < locations.size()) {
            Location currentLocation = locations.get(selectedIndex);
            if (!currentLocation.equals(mSetupFragment.getLocation())) {
                mSchemeChanged = true;
                locations.set(selectedIndex, mSetupFragment.getLocation());
                mSchemeAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onReceive(EditDialog dialog, String oldValue, String newValue) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_SET_SCHEME_NAME:
                if (!judgeInputSchemeNameEmpty(newValue))
                    return false;
                createSetupScheme(newValue);
                onSchemeInitCompleted(null);
                break;
            case DIALOG_TAG_SET_LOCATION_NAME:
                if (!judgeInputLocationNameEmpty(newValue))
                    return false;
                if (!judgeInputLocationNameDuplicated(newValue))
                    return false;
                mSchemeChanged = true;
                mSetupScheme.getLocations().add(new Location(newValue));
                mSchemeAdapter.notifyDataSetChanged();
                break;
            case DIALOG_TAG_RENAME_LOCATION:
                if (!judgeInputLocationNameEmpty(newValue))
                    return false;
                if (newValue.equals(oldValue))
                    return true;
                if (!judgeInputLocationNameDuplicated(newValue))
                    return false;
                mSchemeChanged = true;
                Location selectedLocation = mSchemeAdapter.getSelectedLocation();
                if (selectedLocation != null) {
                    selectedLocation.setName(newValue);
                    mSchemeAdapter.notifyDataSetChanged();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onOkClick(BaseDialog dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_IS_START_NEW_SCHEME:
                startNewScheme();
                break;
            case DIALOG_TAG_EXIT_AND_SAVE_SCHEME:
                saveSchemeAndExit();
                break;
            case DIALOG_TAG_CHECK_LOCATION_INFO:
                onBackSetupActivity();
                break;
            case DIALOG_TAG_IS_SAVE_SCHEME:
                saveSchemeAndExitImp();
                break;
        }
        return true;
    }

    @Override
    public void onCancelClick(BaseDialog dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_IS_START_NEW_SCHEME:
                String[] schemesName = dialog.getArguments().getStringArray(ARGUMENT_KEY_SCHEMES_NAME);
                if (schemesName.length == 1) {
                    importSetupScheme(schemesName[0]);
                    onSchemeInitCompleted(null);
                } else {
                    ListDialog listDialog = new ListDialog();
                    listDialog.show(getSupportFragmentManager(),
                            DIALOG_TAG_SELECT_SCHEME,
                            getString(R.string.select_scheme),
                            schemesName);
                }
                break;
            case DIALOG_TAG_SET_SCHEME_NAME:
                createSetupScheme(getDefaultSetupName());
                onSchemeInitCompleted(null);
                break;
        }
    }

    @Override
    public void onItemSelected(ListDialog dialog, String item) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_SELECT_SCHEME:
                importSetupScheme(item);
                onSchemeInitCompleted(null);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_rename_location:
                Location locationToRename = mSchemeAdapter.getSelectedLocation();
                if (locationToRename != null) {
                    EditDialog dialog = new EditDialog();
                    dialog.show(getSupportFragmentManager(),
                            DIALOG_TAG_RENAME_LOCATION,
                            getString(R.string.rename_location),
                            locationToRename.getName());
                }
                mPwLocationOperation.dismiss();
                break;
            case R.id.tv_delete_location:
                Location locationToDelete = mSchemeAdapter.getSelectedLocation();
                if (locationToDelete != null) {
                    mSetupScheme.getLocations().remove(locationToDelete);
                    mSchemeAdapter.notifyDataSetChanged();
                }
                mPwLocationOperation.dismiss();
                break;
            case R.id.tv_cancel:
                mPwLocationOperation.dismiss();
                break;
        }
    }
}
