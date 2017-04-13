package com.weisi.tool.smartnfcsetuphelper.bean;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KAT on 2017/4/1.
 */

public class Environment {

    public static final String ATTACHMENT_DIRECTORY = "SmartSetupHelper";
    private static final String PROJECTS_DIRECTORY_NAME = "projects";
    private static final String SCHEMES_DIRECTORY_NAME = "schemes";
    static final String FILE_EXTENSIONS_LITTLE = ".xml";
    private static final String FILE_EXTENSIONS_BIG = ".XML";

    private Environment() {

    }

    private static String catenatePath(String p1, String p2) {
        return p1 != null && p2 != null ? p1 + File.separator + p2 : null;
    }

    public static String getDataPath(Context context) {
        return context == null ? null :
                context.getFilesDir().getParent();
    }

    private static String getProjectsPath(Context context) {
        return catenatePath(getDataPath(context), PROJECTS_DIRECTORY_NAME);
    }

    private static String getSchemesPath(Context context) {
        return catenatePath(getDataPath(context), SCHEMES_DIRECTORY_NAME);
    }

    //isCreate = true, 则若无该目录，则新建
    private static File getDirectory(String path, boolean isCreate) {
        if (path == null)
            return null;
        File projects = new File(path);
        return projects.exists() ||
                (isCreate ? projects.mkdir() : false) ?
                projects :
                null;
    }

    static File getProjectsDirectory(Context context) {
        return getDirectory(getProjectsPath(context), true);
    }

    static File getProjectFile(Context context, String projectName) {
        return getDirectory(getProjectPath(getProjectsDirectory(context), projectName), false);
    }

    static File getSchemesDirectory(Context context) {
        return getDirectory(getSchemesPath(context), true);
    }

    static File getSchemeDirectory(Context context, String projectName, boolean isCreate) {
        return getDirectory(getSchemeDirectoryPath(context, projectName), isCreate);
    }

    static File getSchemeFile(Context context, String projectName, String schemeName) {
        return getDirectory(getSchemeFilePath(context, projectName, schemeName), false);
    }

    static String getSchemeDirectoryPath(Context context, String projectName) {
        File schemesDirectory = getSchemesDirectory(context);
        return catenatePath(schemesDirectory != null ?
                schemesDirectory.getPath() : null, projectName);
    }

    static String getSchemeFilePath(Context context, String projectName, String schemeName) {
        return getFilePath(getSchemeDirectoryPath(context, projectName), schemeName);
    }

    static String getSchemeFilePath(File schemeDirectory, String schemeName) {
        return schemeDirectory == null ?
                null :
                getFilePath(schemeDirectory.getAbsolutePath(), schemeName);
    }

    private static String getFilePath(String directoryPath, String fileNameWithoutExtension) {
        return TextUtils.isEmpty(directoryPath) || TextUtils.isEmpty(fileNameWithoutExtension) ?
                null :
                directoryPath +
                        File.separatorChar +
                        fileNameWithoutExtension +
                        FILE_EXTENSIONS_LITTLE;
    }

    static String getProjectPath(Context context, String projectName) {
        return getFilePath(getProjectsPath(context), projectName);
    }

    static String getProjectPath(File projectsDirectory, String projectName) {
        return projectsDirectory == null ?
                null :
                getFilePath(projectsDirectory.getAbsolutePath(), projectName);
    }

    public static String getDefaultName(String projectPath) {
        if (TextUtils.isEmpty(projectPath))
            return null;
        int start = projectPath.lastIndexOf('/') + 1;
        int end = projectPath.indexOf(FILE_EXTENSIONS_LITTLE, start);
        if (end == -1) {
            end = projectPath.indexOf(FILE_EXTENSIONS_BIG, start);
            if (end == -1)
                return null;
        }
        return projectPath.substring(start, end);
    }

    public static String[] getProjectsName(Context context) {
        return getNames(getProjectsDirectory(context));
    }

    //获取某一安装项目的所有安装方案名称
    public static String[] getSchemesName(Context context, String projectName) {
        return getNames(getSchemeDirectory(context, projectName, false));
    }

    private static String[] getNames(File directory) {
        List<String> names = new ArrayList<>();
        if (directory != null) {
            for (File project :
                    directory.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(FILE_EXTENSIONS_LITTLE);
                        }
                    })) {
                names.add(getDefaultName(project.getName()));
            }
        }
        return names.toArray(new String[names.size()]);
    }
}
