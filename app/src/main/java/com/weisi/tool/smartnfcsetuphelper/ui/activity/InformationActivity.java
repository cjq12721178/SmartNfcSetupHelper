package com.weisi.tool.smartnfcsetuphelper.ui.activity;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.weisi.tool.smartnfcsetuphelper.R;
import com.weisi.tool.smartnfcsetuphelper.bean.Environment;
import com.weisi.tool.smartnfcsetuphelper.bean.SetupScheme;
import com.weisi.tool.smartnfcsetuphelper.ui.adapter.SchemeDisplayAdapter;
import com.weisi.tool.smartnfcsetuphelper.ui.decoration.SpaceItemDecoration;
import com.weisi.tool.smartnfcsetuphelper.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InformationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String ARGUMENT_KEY_PROJECT_INDEX = "project_index";
    private static final String ARGUMENT_KEY_SCHEME_INDEX = "scheme_index";
    private Spinner mSpnProject;
    private Spinner mSpnScheme;
    private TextView mTvEmptyItemPrompt;
    private RecyclerView mRvScheme;
    private SchemeDisplayAdapter mSchemeDisplayAdapter;
    private Map<String, SetupScheme[]> mSchemeMap = new HashMap<>();
    //private SetupScheme mScheme;
    //private int mProjectIndex = -1;
    //private int mSchemeIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        mTvEmptyItemPrompt = (TextView) findViewById(R.id.tv_empty_item_prompt);

        mRvScheme = (RecyclerView) findViewById(R.id.rv_scheme_display);
        mRvScheme.addItemDecoration(new SpaceItemDecoration(getResources().
                getDimensionPixelSize(R.dimen.margin_small_vertical), true));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRvScheme.setLayoutManager(linearLayoutManager);
        mSchemeDisplayAdapter = new SchemeDisplayAdapter(this);
        mRvScheme.setAdapter(mSchemeDisplayAdapter);

        mSpnProject = (Spinner) findViewById(R.id.spn_project);
        mSpnProject.setOnItemSelectedListener(this);
        String[] projectsName = Environment.getProjectsName(this);
        ArrayAdapter<CharSequence> projectAdapter = new ArrayAdapter<CharSequence>(
                this,
                R.layout.list_item_name,
                projectsName);
        mSpnProject.setAdapter(projectAdapter);

        mSpnScheme = (Spinner) findViewById(R.id.spn_scheme);
        mSpnScheme.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> schemeAdapter = new ArrayAdapter<CharSequence>(
                this,
                R.layout.list_item_name,
                new ArrayList<CharSequence>());
        mSpnScheme.setAdapter(schemeAdapter);
        if (projectsName.length > 0) {
            mTvEmptyItemPrompt.setVisibility(View.GONE);
            mRvScheme.setVisibility(View.VISIBLE);
            if (savedInstanceState == null) {
                //mSchemeIndex = 0;
                mSpnProject.setTag(0);
                mSpnProject.setSelection(0);
            } else {
                mSpnProject.setTag(savedInstanceState.getInt(ARGUMENT_KEY_SCHEME_INDEX));
                mSpnProject.setSelection(savedInstanceState.getInt(ARGUMENT_KEY_PROJECT_INDEX));
            }
        } else {
            mTvEmptyItemPrompt.setText(R.string.no_project);
            mTvEmptyItemPrompt.setVisibility(View.VISIBLE);
            mRvScheme.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spn_project:
                String[] schemesName = Environment.getSchemesName(this, (String)parent.getItemAtPosition(position));
                if (schemesName.length == 0) {
                    mTvEmptyItemPrompt.setText(R.string.no_scheme);
                    mTvEmptyItemPrompt.setVisibility(View.VISIBLE);
                    mRvScheme.setVisibility(View.GONE);
                } else {
                    mTvEmptyItemPrompt.setVisibility(View.GONE);
                    mRvScheme.setVisibility(View.VISIBLE);
                }
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpnScheme.getAdapter();
                adapter.clear();
                adapter.addAll(schemesName);
                adapter.notifyDataSetChanged();
                int schemeIndex = (int) parent.getTag();
                if (schemeIndex >= 0 && schemeIndex < schemesName.length) {
                    mSpnScheme.setSelection(schemeIndex);
                }
                break;
            case R.id.spn_scheme:
                String projectName = (String) mSpnProject.getSelectedItem();
                SetupScheme[] schemes = mSchemeMap.get(projectName);
                if (schemes == null) {
                    schemes = new SetupScheme[parent.getCount()];
                    mSchemeMap.put(projectName, schemes);
                }
                SetupScheme scheme = schemes[position];
                if (scheme == null) {
                    scheme = SetupScheme.importScheme(
                            this,
                            projectName,
                            (String) parent.getItemAtPosition(position));
                    schemes[position] = scheme;
                }
                if (scheme != null) {
                    mSchemeDisplayAdapter.setScheme(scheme);
                    mSchemeDisplayAdapter.notifyDataSetChanged();
                } else {
                    Logger.record(getString(R.string.import_scheme_failed));
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARGUMENT_KEY_PROJECT_INDEX, mSpnProject.getSelectedItemPosition());
        outState.putInt(ARGUMENT_KEY_SCHEME_INDEX, mSpnScheme.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }
}
