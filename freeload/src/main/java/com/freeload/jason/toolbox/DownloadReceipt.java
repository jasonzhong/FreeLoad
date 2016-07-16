package com.freeload.jason.toolbox;

import java.io.Serializable;
import java.util.ArrayList;

public class DownloadReceipt implements Serializable {

    public static enum STATE {
		NONE, // 0
        GETSIZE, // 1
        FAILED_GETSIZE, // 2
        CREATEFILE, // 3
        FAILED_CREATEFILE, // 4
        QUEST_PREPARE, // 5
        FAILED_QUEST_PREPARE, // 6
		START, // 7
		DOWNLOAD, // 8
        CANCEL, // 9
		SUCCESS_DOWNLOAD, // 10
        START_COMBIN_FILE, // 11
        SUCCESS_COMBIN_FILE, // 12
		FAILED, // 13
		TIMEOUT, // 14
        CONNWRONG // 15
	}

    private int mPosition = 0;
    private ArrayList<STATE> mListState;

    private ArrayList<Long> mListDownloadSize;
    private ArrayList<Long> mListDownloadTotalSize;

    public DownloadReceipt() {
        mListState = new ArrayList<STATE>();
        mListDownloadSize = new ArrayList<Long>();
        mListDownloadTotalSize = new ArrayList<Long>();
        Long l = Long.valueOf(0);
        mListDownloadSize.add(l);
        mListDownloadTotalSize.add(l);
        mListState.add(STATE.NONE);
    }

    public void setDownloadState(STATE state) {
        mListState.clear();
        mListState.add(state);
    }

    public STATE getDownloadState() {
        return mListState.get(0);
    }

    public void setDownloadPosition(int position) {
        mPosition = position;
    }

    public int getDownloadPosition() {
        return mPosition;
    }

    public void setDownloadedSize(long downloadSize) {
        if (downloadSize == 0) {
            return;
        }
        Long size = downloadSize;
        mListDownloadSize.clear();
        mListDownloadSize.add(size);
    }

    public long getDownloadedSize() {
        if (mListDownloadSize.size() <= 0) {
            return 0;
        }

        return mListDownloadSize.get(0);
    }

    public long getDownloadedSize(int pos) {
        if (mListDownloadSize.size() <= 0 || mListDownloadSize.size() < pos) {
            return 0;
        }
        return mListDownloadSize.get(0);
    }

    public void setDownloadTotalSize(long downloadTotalSize) {
        if (downloadTotalSize == 0) {
            return;
        }
        Long size = downloadTotalSize;
        mListDownloadTotalSize.clear();
        mListDownloadTotalSize.add(size);
    }

    public long getDownloadTotalSize() {
        if (mListDownloadTotalSize.size() <= 0) {
            return 0;
        }

        return mListDownloadTotalSize.get(0);
    }

    @Override
    public String toString() {
        String str = "";

        str += "downloadSize" + mPosition + ":" + mListDownloadSize.get(0) +
                ",downloadTotalSize" + mPosition + ":" + mListDownloadTotalSize.get(0) +
                ",downloadState:" + mListState + ";";
        return str;
    }
}
