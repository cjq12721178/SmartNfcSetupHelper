package com.weisi.tool.smartnfcsetuphelper.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Environment;
import com.weisi.tool.smartnfcsetuphelper.bean.SetupProject;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.ConfirmDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.BaseDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.dialog.EditDialog;
import com.weisi.tool.smartnfcsetuphelper.ui.toast.Prompter;
import com.weisi.tool.smartnfcsetuphelper.util.Logger;

public class SettingFragment
        extends PreferenceFragment
        implements BaseDialog.OnOkClickListener,
        EditDialog.OnContentReceiver {

    private static final String PROJECT_ENTRY_VALUE_SELECT = "select";
    private static final String PROJECT_ENTRY_VALUE_RENAME = "rename";
    private static final String PROJECT_ENTRY_VALUE_DELETE = "delete";
    private static final String[] PROJECT_ENTRY_VALUES = new String[] {
            PROJECT_ENTRY_VALUE_SELECT,
            PROJECT_ENTRY_VALUE_RENAME,
            PROJECT_ENTRY_VALUE_DELETE
    };
    private static final int REQUEST_CODE_IMPORT_SETUP_CONFIGURATION = 1;
    private static final String DIALOG_TAG_RENAME_PROJECT = "rename_project";
    private static final String DIALOG_TAG_DELETE_PROJECT = "delete_project";
    private static final String DIALOG_TAG_ADD_PROJECT = "add_project";
    private static final String ARGUMENT_TAG_PROJECT_NAME = "project_name";
    private static final String ARGUMENT_TAG_PROJECT = "project";

    private PreferenceCategory mProjectCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("setting");
        addPreferencesFromResource(R.xml.setting);
        initProjectCategory();
    }

    private void initProjectCategory() {
        mProjectCategory = (PreferenceCategory) findPreference("project");
        findPreference("add_project").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("text/plain");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.import_setup_configuration)),
                            REQUEST_CODE_IMPORT_SETUP_CONFIGURATION);
                } catch (android.content.ActivityNotFoundException ex) {
                    Logger.record(getString(R.string.file_manager_can_not_open));
                }
                return true;
            }
        });

        for (String projectName :
                Environment.getProjectsName(getActivity().getApplicationContext())) {
            addProjectPreference(projectName);
        }
    }

    private void addProjectPreference(String projectName) {
        ListPreference preference = new ListPreference(getActivity());
        preference.setKey(projectName);
        preference.setTitle(projectName);
        preference.setEntries(R.array.project_entry);
        preference.setEntryValues(PROJECT_ENTRY_VALUES);
        if (projectName.equals(SetupProject.getCurrentProjectName(getActivity()))) {
            preference.setSummary(R.string.current_project);
        }
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                if (newValue instanceof String) {
                    switch ((String)newValue) {
                        case PROJECT_ENTRY_VALUE_SELECT:
                            changeCurrentProject(preference.getKey());
                            break;
                        case PROJECT_ENTRY_VALUE_RENAME:
                            EditDialog editDialog = new EditDialog();
                            editDialog.show(getChildFragmentManager(),
                                    DIALOG_TAG_RENAME_PROJECT,
                                    getString(R.string.input_new_project_name),
                                    preference.getKey());
                            break;
                        case PROJECT_ENTRY_VALUE_DELETE:
                            ConfirmDialog dialog = new ConfirmDialog();
                            dialog.getArguments().putString(ARGUMENT_TAG_PROJECT_NAME, preference.getKey());
                            dialog.show(getChildFragmentManager(),
                                    DIALOG_TAG_DELETE_PROJECT,
                                    getString(R.string.delete_project_and_schemes));
                    }
                }
                return false;
            }
        });
        mProjectCategory.addPreference(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_CODE_IMPORT_SETUP_CONFIGURATION &&
                data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                final SetupProject newProject = SetupProject.importProject(uri.getPath());
                if (newProject == null) {
                    Logger.record(getString(R.string.import_setup_configuration_failed));
                } else {
                    if (newProject.exists(getActivity())) {
                        ConfirmDialog dialog = new ConfirmDialog();
                        dialog.getArguments().putParcelable(ARGUMENT_TAG_PROJECT, newProject);
                        dialog.show(getChildFragmentManager(),
                                DIALOG_TAG_ADD_PROJECT,
                                getString(R.string.is_cover_existed_project));
                    } else {
                        addNewProject(newProject, false);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addNewProject(SetupProject newProject, boolean exists) {
        if (newProject.saveToLocal(getActivity())) {
            if (!exists) {
                addProjectPreference(newProject.getName());
            }
            changeCurrentProject(newProject.getName());
        } else {
            Logger.record(getString(R.string.save_project_to_local_failed));
        }
    }

    private void changeCurrentProject(String newProjectName) {
        String oldProjectName = SetupProject.getCurrentProjectName(getActivity());
        if (TextUtils.isEmpty(newProjectName) || newProjectName.equals(oldProjectName))
            return;
        if (!TextUtils.isEmpty(oldProjectName)) {
            Preference preference = findPreference(oldProjectName);
            if (preference != null) {
                preference.setSummary(null);
            }
        }
        findPreference(newProjectName).setSummary(getString(R.string.current_project));
        SetupProject.setCurrentProjectName(getActivity(), newProjectName);
    }

    @Override
    public boolean onReceive(EditDialog dialog, String oldValue, String newValue) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_RENAME_PROJECT:
                if (TextUtils.isEmpty(newValue)) {
                    Prompter.show(R.string.project_name_can_not_be_null);
                    return false;
                }
                if (!oldValue.equals(newValue)) {
                    SetupProject project = new SetupProject(newValue);
                    if (project.exists(getActivity())) {
                        Prompter.show(R.string.project_has_existed);
                        return false;
                    }
                    if (SetupProject.rename(getActivity(), oldValue, newValue)) {
                        Preference preference = findPreference(oldValue);
                        if (preference != null) {
                            preference.setKey(newValue);
                            preference.setTitle(newValue);
                        }
                        if (SetupProject.getCurrentProjectName(getActivity()).equals(oldValue)) {
                            SetupProject.setCurrentProjectName(getActivity(), newValue);
                        }
                    } else {
                        Prompter.show(R.string.rename_project_failed);
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onOkClick(BaseDialog dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_DELETE_PROJECT:
                Context context = getActivity();
                String projectName = dialog.getArguments().getString(ARGUMENT_TAG_PROJECT_NAME);
                if (SetupProject.delete(context, projectName)) {
                    Preference preference = findPreference(projectName);
                    mProjectCategory.removePreference(preference);
                    if (SetupProject.getCurrentProjectName(context).equals(projectName)) {
                        String[] names = Environment.getProjectsName(context);
                        if (names.length > 0) {
                            changeCurrentProject(names[0]);
                        } else {
                            SetupProject.setCurrentProjectName(context, null);
                        }
                    }
                } else {
                    Prompter.show(R.string.delete_project_failed);
                }
                break;
            case DIALOG_TAG_ADD_PROJECT:
                addNewProject((SetupProject) dialog.getArguments().getParcelable(ARGUMENT_TAG_PROJECT), true);
                break;
        }
        return true;
    }
}
