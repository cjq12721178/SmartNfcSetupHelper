package com.weisi.tool.smartnfcsetuphelper.bean;

import android.content.Context;
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
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by KAT on 2017/3/31.
 */

public class SetupScheme implements Parcelable {

    private static final String XML_TAG_SCHEME = "scheme";
    private static final String XML_TAG_SCHEME_NAME = "SchemeName";
    private static final String XML_TAG_LOCATIONS = "locations";
    private static final String XML_TAG_LOCATION = "location";
    private static final String XML_TAG_LOCATION_NAME = "LocationName";
    private static final String XML_TAG_DEVICES = "devices";
    private static final String XML_TAG_DEVICE = "device";
    private static final String XML_TAG_BLE_ADDRESS = "BleAddress";
    private static final String XML_TAG_POSITION = "position";

    private long mCompleteTime;
    private String mName;
    private List<Location> mLocations = new ArrayList<>();

    public static final Creator<SetupScheme> CREATOR = new Creator<SetupScheme>() {
        @Override
        public SetupScheme createFromParcel(Parcel in) {
            return new SetupScheme(in);
        }

        @Override
        public SetupScheme[] newArray(int size) {
            return new SetupScheme[size];
        }
    };

    public static SetupScheme from(SetupProject project, String schemeName) {
        if (project == null || TextUtils.isEmpty(project.getName()))
            return TextUtils.isEmpty(schemeName) ?
                    null :
                    new SetupScheme(schemeName);
        SetupScheme scheme = new SetupScheme(
                TextUtils.isEmpty(schemeName) ?
                project.getName() :
                schemeName);
        for (String locationName :
                project.getLocationNames()) {
            scheme.mLocations.add(new Location(locationName));
        }
        return scheme;
    }

    public static SetupScheme importScheme(Context context, String projectName, String schemeName) {
        String schemePath = Environment.getSchemeFilePath(context, projectName, schemeName);
        if (TextUtils.isEmpty(schemePath))
            return null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            InputStream is = new FileInputStream(schemePath);
            SAXParser parser = factory.newSAXParser();
            Importer importer = new Importer();
            parser.parse(is, importer);
            return importer.getScheme();
        } catch (Exception e) {
        }
        return null;
    }

    private static class Importer extends DefaultHandler {

        private StringBuilder mBuilder = new StringBuilder();
        private SetupScheme mScheme;
        private Location mLocation;
        private Device mDevice;
        private int mDeviceIndex;

        public SetupScheme getScheme() {
            return mScheme;
        }

        @Override
        public void startDocument() throws SAXException {
            mScheme = new SetupScheme();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mBuilder.setLength(0);
            switch (localName) {
                case XML_TAG_DEVICE:
                    mDevice = new Device();
                    ++mDeviceIndex;
                    break;
                case XML_TAG_DEVICES:
                    mDeviceIndex = -1;
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName) {
                case XML_TAG_LOCATION_NAME:
                    mLocation = new Location(mBuilder.toString());
                    break;
                case XML_TAG_BLE_ADDRESS:
                    mDevice.setBleAddress(mBuilder.toString());
                    break;
                case XML_TAG_POSITION:
                    mDevice.setPosition(mBuilder.toString());
                    break;
                case XML_TAG_DEVICE:
                    mLocation.getDevices()[mDeviceIndex] = mDevice;
                    break;
                case XML_TAG_LOCATION:
                    mScheme.mLocations.add(mLocation);
                    break;
                case XML_TAG_SCHEME_NAME:
                    mScheme.mName = mBuilder.toString();
                    break;
            }
        }
    }

    private SetupScheme() {
    }

    protected SetupScheme(Parcel in) {
        mCompleteTime = in.readLong();
        mName = in.readString();
        mLocations = in.createTypedArrayList(Location.CREATOR);
    }

    public SetupScheme(String name) {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException();
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public List<Location> getLocations() {
        return mLocations;
    }

    public boolean containsLocation(String locationName) {
        if (TextUtils.isEmpty(locationName))
            return false;
        return mLocations.contains(locationName);
    }

    public boolean allLocationHasFullInfo() {
        for (Location location :
                mLocations) {
            if (!location.containsFullInfo())
                return false;
        }
        return true;
    }

    public boolean exists(Context context, String projectName) {
        return Environment.getSchemeFile(context, projectName, mName) != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mCompleteTime);
        dest.writeString(mName);
        dest.writeTypedList(mLocations);
    }

    public boolean saveToLocal(Context context, String projectName) {
        File schemeDirectory = Environment.getSchemeDirectory(context, projectName, true);
        String filePath = Environment.getSchemeFilePath(schemeDirectory, mName);
        if (filePath == null)
            return false;
        try {
            OutputStream os = new FileOutputStream(filePath);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(os, "UTF-8");
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, XML_TAG_SCHEME);
            serializer.startTag(null, XML_TAG_SCHEME_NAME);
            serializer.text(mName);
            serializer.endTag(null, XML_TAG_SCHEME_NAME);
            serializer.startTag(null, XML_TAG_LOCATIONS);
            for (Location location :
                    mLocations) {
                serializer.startTag(null, XML_TAG_LOCATION);
                serializer.startTag(null, XML_TAG_LOCATION_NAME);
                serializer.text(location.getName());
                serializer.endTag(null, XML_TAG_LOCATION_NAME);
                serializer.startTag(null, XML_TAG_DEVICES);
                for (Device device :
                        location.getDevices()) {
                    serializer.startTag(null, XML_TAG_DEVICE);
                    serializer.startTag(null, XML_TAG_BLE_ADDRESS);
                    String bleAddress = device != null ? device.getBleAddress() : "";
                    serializer.text(bleAddress != null ? bleAddress : "");
                    serializer.endTag(null, XML_TAG_BLE_ADDRESS);
                    serializer.startTag(null, XML_TAG_POSITION);
                    String position = device != null ? device.getPosition() : "";
                    serializer.text(position != null ? position : "");
                    serializer.endTag(null, XML_TAG_POSITION);
                    serializer.endTag(null, XML_TAG_DEVICE);
                }
                serializer.endTag(null, XML_TAG_DEVICES);
                serializer.endTag(null, XML_TAG_LOCATION);
            }
            serializer.endTag(null, XML_TAG_LOCATIONS);
            serializer.endTag(null, XML_TAG_SCHEME);
            serializer.endDocument();
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }
}
