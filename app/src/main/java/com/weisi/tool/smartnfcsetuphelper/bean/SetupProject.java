package com.weisi.tool.smartnfcsetuphelper.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static java.lang.System.out;

/**
 * Created by KAT on 2017/4/1.
 */

public class SetupProject implements Parcelable {

    private static final String PROJECT_PREFERENCE = "setup_project";
    private static final String PREFERENCE_KEY_CURRENT_PROJECT_NAME = "current_project_name";
    private static final String XML_TAG_PROJECT = "SetupProject";
    private static final String XML_TAG_LOCATIONS = "locations";
    private static final String XML_TAG_LOCATION = "location";
    private static final String XML_TAG_NAME = "name";

    private static SetupProject currentProject;
    private static String currentProjectName;

    private String mName;
    private List<String> mLocationNames = new ArrayList<>();

    protected SetupProject(Parcel in) {
        mName = in.readString();
        mLocationNames = in.createStringArrayList();
    }

    public static final Creator<SetupProject> CREATOR = new Creator<SetupProject>() {
        @Override
        public SetupProject createFromParcel(Parcel in) {
            return new SetupProject(in);
        }

        @Override
        public SetupProject[] newArray(int size) {
            return new SetupProject[size];
        }
    };

    public static String getCurrentProjectName(Context context) {
        if (context == null)
            return null;
        if (currentProjectName == null) {
            SharedPreferences preferences = context.getSharedPreferences(
                    PROJECT_PREFERENCE,
                    Context.MODE_PRIVATE);
            currentProjectName = preferences.getString(PREFERENCE_KEY_CURRENT_PROJECT_NAME, null);
        }
        return currentProjectName;
    }

    public static void setCurrentProjectName(Context context, String newName) {
        if (context == null || TextUtils.equals(currentProjectName, newName))
            return;
        context.getSharedPreferences(PROJECT_PREFERENCE,
                Context.MODE_PRIVATE)
                .edit()
                .putString(PREFERENCE_KEY_CURRENT_PROJECT_NAME, newName)
                .commit();
        currentProjectName = newName;
        currentProject = null;
    }

    public static SetupProject getCurrentProject(Context context) {
        if (currentProject == null) {
            currentProject = importProject(Environment.getProjectPath(context,
                            getCurrentProjectName(context)));
        }
        return currentProject;
    }

    public static SetupProject importProject(String projectPath) {
        if (TextUtils.isEmpty(projectPath))
            return null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            InputStream is = new FileInputStream(projectPath);
            SAXParser parser = factory.newSAXParser();
            Importer importer = new Importer();
            parser.parse(is, importer);
            return importer.getProject();
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean rename(Context context, String oldName, String newName) {
        File projectFile = Environment.getProjectFile(context, oldName);
        if (projectFile == null)
            return false;
        if (!projectFile.renameTo(new File(Environment.getProjectPath(context, newName))))
            return false;
        File schemeDirectory = Environment.getSchemeDirectory(context, oldName, true);
        if (schemeDirectory == null)
            return false;
        return schemeDirectory.renameTo(new File(Environment.getSchemeDirectoryPath(context, newName)));
    }

    public static boolean delete(Context context, String projectName) {
        if (TextUtils.isEmpty(projectName))
            return false;
        String projectPath = Environment.getProjectPath(context, projectName);
        if (projectPath == null)
            return false;
        File project = new File(projectPath);
        if (!project.exists())
            return false;
        if (!project.delete())
            return false;
        File schemes = Environment.getSchemeDirectory(context, projectName, false);
        if (schemes == null)
            return true;
        return schemes.delete();
    }

    private SetupProject() {
    }

    public SetupProject(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<String> getLocationNames() {
        return mLocationNames;
    }

    //判断安装项目是否存在
    public boolean exists(Context context) {
//        String[] projectsName = Environment.getProjectsName(context);
//        for (String projectName :
//                projectsName) {
//            if (projectName.equals(mName)) {
//                return true;
//            }
//        }
        return Environment.getProjectFile(context, mName) != null;
    }

    public boolean saveToLocal(Context context) {
        File projectsDirectory = Environment.getProjectsDirectory(context);
        String projectPath = Environment.getProjectPath(projectsDirectory, mName);
        if (projectPath == null)
            return false;
        try {
            //将安装项目保存至文件
            OutputStream os = new FileOutputStream(projectPath);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(os, "UTF-8");
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, XML_TAG_PROJECT);
            serializer.startTag(null, XML_TAG_NAME);
            serializer.text(mName);
            serializer.endTag(null, XML_TAG_NAME);
            serializer.startTag(null, XML_TAG_LOCATIONS);
            for (String locationName
                    : mLocationNames) {
                serializer.startTag(null, XML_TAG_LOCATION);
                serializer.text(locationName);
                serializer.endTag(null, XML_TAG_LOCATION);
            }
            serializer.endTag(null, XML_TAG_LOCATIONS);
            serializer.endTag(null, XML_TAG_PROJECT);
            serializer.endDocument();
            os.flush();
            os.close();
            //在方案目录中增加相应项目目录
            return Environment.getSchemeDirectory(context, mName, true) != null;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeStringList(mLocationNames);
    }

    private static class Importer extends DefaultHandler {

        private StringBuilder mBuilder = new StringBuilder();
        private SetupProject mProject;
        private Set<String> mLocationNamesSet;

        public SetupProject getProject() {
            return mProject;
        }

        @Override
        public void startDocument() throws SAXException {
            mProject = new SetupProject();
            mLocationNamesSet = new HashSet<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mBuilder.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName) {
                case XML_TAG_LOCATION:
                    mLocationNamesSet.add(mBuilder.toString());
                    break;
                case XML_TAG_NAME:
                    mProject.setName(mBuilder.toString());
                    break;
                case XML_TAG_LOCATIONS:
                    mProject.mLocationNames.addAll(mLocationNamesSet);
                    break;
            }
        }
    }
}
