package com.weisi.tool.smartnfcsetuphelper.nfc;

import android.content.Context;
import android.media.AudioManager;

import com.acs.audiojack.AudioJackReader;
import com.acs.audiojack.Result;

import java.util.Arrays;

/**
 * Created by KAT on 2017/4/10.
 */

public class NfcReader implements
        AudioJackReader.OnResultAvailableListener,
        AudioJackReader.OnPiccResponseApduAvailableListener,
        AudioJackReader.OnPiccAtrAvailableListener,
        AudioJackReader.OnResetCompleteListener {

    private static final int ACTIVATE_DURATION = 28000;    //ms
    private static final int MANUAL_RETRIEVE_DATA_TIMEOUT = 3000; //ms
    private static final int AUTO_RETRIEVE_DATA_TIMEOUT = 3000;
    //private final int AUTO_RETRIEVE_DATA_INTERVAL = 1100;
    private static final int WAIT_ACTIVATE_TIMEOUT = 5000; //ms
    private static final int PICC_TIMEOUT = 1;             //s
    private static final int PICC_CARD_TYPE = 0x8F & 0xFF;
    private static final byte[][] COMMAND_APDU_GROUP = new byte[][] {
            { (byte)0xFF, (byte)0xCA, 0, 0, 0 },
            { (byte)0xFF, (byte)0xB0, 0, 4, 4 }
    };
    private static final int COMMAND_TYPE_UID = 0;
    private static final int COMMAND_TYPE_DATA = 1;
    private static final byte SW_SUCCESS_HIGH = (byte)0x90;
    private static final byte SW_SUCCESS_LOW = 0;
    private static final int ERROR_PICC_POWER_ON = 1;
    private static final int ERROR_PICC_TRANSMIT = 2;
    private static final int ERROR_SET_MAX_VOLUME = 3;
    private static final int ERROR_RESET = 4;

    private AudioManager mAudioManager;
    private AudioJackReader mReader;
    private Object mLocker = new Object();
    private boolean mStarted;
    private boolean mActivated;
    private boolean mOnPower;
    private boolean mPause;
    private boolean mResultReady;
    private boolean mPiccResponseApduReady;
    private byte[] mCommandApdu;
    private byte[] mData;
    private OnErrorPromptListener mOnErrorPromptListener;
    private OnDataReceivedListener mOnDataReceivedListener;
    private Thread mMissionThread;
    //0：手动读取
    //1：自动读取
    //-1：不读取
    private int mReadState = -1;

    public boolean init(Context context) {
        if (context == null)
            return false;
        //mContext = context;
        if (mAudioManager != null && mReader != null)
            return true;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mReader = new AudioJackReader(mAudioManager);
        /* Set the result callback. */
        mReader.setOnResultAvailableListener(this);
        /* Set the PICC ATR callback. */
        mReader.setOnPiccAtrAvailableListener(this);
        /* Set the PICC response APDU callback. */
        mReader.setOnPiccResponseApduAvailableListener(this);
        return true;
    }

    public void start() {
        if (mReader != null && !mStarted) {
            mStarted = true;
            mReader.start();
            activate();
        }
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        mPause = false;
    }

    public void readUid() {
        makeCommand();
        mReadState = 0;
    }

    public void read(int pos, int len) {
        makeCommand(pos, len);
        mReadState = 0;
    }

    public void autoRead(int pos, int len) {
        makeCommand(pos, len);
        mReadState = 1;
    }

    public void stop() {
        if (mReader != null && mStarted) {
            mStarted = false;
            mMissionThread = null;
            mReader.stop();
        }
    }

    private void makeCommand() {
        mCommandApdu = COMMAND_APDU_GROUP[COMMAND_TYPE_UID];
    }

    private void makeCommand(int pos, int len) {
        if (pos < 0 || len <= 0 || pos + len >= 64)
            throw new IndexOutOfBoundsException();
        mCommandApdu = COMMAND_APDU_GROUP[COMMAND_TYPE_DATA];
        mCommandApdu[3] = (byte)(pos + 4);
        mCommandApdu[4] = (byte)len;
    }

    private void activate() {
        if (mMissionThread == null) {
            mMissionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long lastMissionStartTime = 0;
                    long currentTime;
                    while (mStarted) {
                        if (mPause) {
                            sleep();
                        } else {
                            currentTime = System.currentTimeMillis();
                            if (!mActivated || currentTime - lastMissionStartTime >= ACTIVATE_DURATION) {
                                if (ensureMaxVolume()) {
                                    reset();
                                    lastMissionStartTime = currentTime;
                                    promptError(ERROR_RESET);
                                }
                            } else if (mReadState == 0) {
                                if (read(MANUAL_RETRIEVE_DATA_TIMEOUT)) {
                                    mReadState = -1;
                                }
                            } else if (mReadState == 1) {
                                read(AUTO_RETRIEVE_DATA_TIMEOUT);
                            } else {
                                sleep();
                            }
                        }
                    }
                    mActivated = false;
                    mOnPower = false;
                }
            });
            mMissionThread.start();
        }
    }

    private void sleep() {
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (mStarted && mPause);
    }

    private boolean read(long retrieveDataTimeout) {
        if (!mOnPower) {
            powerOn();
        }
        if (mOnPower && transmit(retrieveDataTimeout)) {
            mOnPower = false;
            processDataReceived();
            return true;
        }
        return false;
    }

    private void reset() {
        mReader.reset(this);
    }

    private boolean ensureMaxVolume() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < maxVolume) {
            try {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
            } catch (Exception e) {
                promptError(ERROR_SET_MAX_VOLUME);
                return false;
            }
        }
        return true;
    }

    private void powerOn() {
        if (!mActivated)
            return;
        /* Power on the PICC. */
        mResultReady = false;
        mOnPower = false;
        if (mReader.piccPowerOn(PICC_TIMEOUT, PICC_CARD_TYPE)) {
            synchronized (mLocker) {
                /* Wait for the PICC ATR. */
                while (!mOnPower && !mResultReady) {
                    try {
                        mLocker.wait(WAIT_ACTIVATE_TIMEOUT);
                    } catch (InterruptedException e) {
                    }
                    break;
                }
            }
        } else {
            promptError(ERROR_PICC_POWER_ON);
        }
    }

    private boolean transmit(long retrieveDataTimeout) {
        /* Transmit the command APDU. */
        mPiccResponseApduReady = false;
        mResultReady = false;
        if (mReader.piccTransmit(PICC_TIMEOUT, mCommandApdu)) {
            synchronized (mLocker) {
                /* Wait for the PICC response APDU. */
                while (!mPiccResponseApduReady && !mResultReady) {
                    try {
                        mLocker.wait(retrieveDataTimeout);
                    } catch (InterruptedException e) {
                    }
                    break;
                }
                if (mPiccResponseApduReady) {
                    return true;
                }
            }
        } else {
            promptError(ERROR_PICC_TRANSMIT);
        }
        return false;
    }

    @Override
    public void onResetComplete(AudioJackReader audioJackReader) {
        synchronized (mLocker) {
            mActivated = true;
            mLocker.notifyAll();
        }
    }

    @Override
    public void onResultAvailable(AudioJackReader audioJackReader, Result result) {
        synchronized (mLocker) {
            mResultReady = true;
            mLocker.notifyAll();
        }
    }

    @Override
    public void onPiccAtrAvailable(AudioJackReader audioJackReader, byte[] atr) {
        synchronized (mLocker) {
            mOnPower = true;
            mLocker.notifyAll();
        }
    }

    @Override
    public void onPiccResponseApduAvailable(AudioJackReader audioJackReader, byte[] apdu) {
        synchronized (mLocker) {
            int len = apdu.length;
            if (apdu != null &&
                    len >= 2 &&
                    apdu[len - 2] == SW_SUCCESS_HIGH &&
                    apdu[len - 1] == SW_SUCCESS_LOW) {
                    mData = Arrays.copyOf(apdu, len - 2);
            } else {
                mData = null;
            }
            mPiccResponseApduReady = true;
            mLocker.notifyAll();
        }
    }

    public void setOnErrorPromptListener(OnErrorPromptListener listener) {
        mOnErrorPromptListener = listener;
    }

    private void promptError(int infoCode) {
        if (mOnErrorPromptListener != null) {
            mOnErrorPromptListener.onErrorPrompt(infoCode);
        }
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        mOnDataReceivedListener = listener;
    }

    private void processDataReceived() {
        if (mOnDataReceivedListener != null) {
            mOnDataReceivedListener.onReceived(mData);
        }
    }

    public interface OnDataReceivedListener {
        void onReceived(byte[] data);
    }

    public interface OnErrorPromptListener {
        void onErrorPrompt(int infoCode);
    }
}
