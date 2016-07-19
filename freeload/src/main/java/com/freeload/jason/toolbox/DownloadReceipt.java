package com.freeload.jason.toolbox;

import java.io.Serializable;
import java.util.ArrayList;

public class DownloadReceipt implements Serializable {

    public static enum STATE {
		NONE,                   // 0
        GETSIZE,                // 1
        FAILED_GETSIZE,         // 2
        CREATEFILE,             // 3
        FAILED_CREATEFILE,      // 4
        QUEST_PREPARE,          // 5
        FAILED_QUEST_PREPARE,   // 6
		START,                  // 7
		DOWNLOAD,               // 8
        CANCEL,                 // 9
		SUCCESS_DOWNLOAD,       // 10
        START_COMBIN_FILE,      // 11
        SUCCESS_COMBIN_FILE,    // 12
		FAILED,                 // 13
		TIMEOUT,                // 14
        CONNWRONG               // 15
	}

    private int mPosition = 0;
    private ArrayList<STATE> mListState;

    private Long mDownloadSize = 0l;
    private Long mDownloadTotalSize = 0l;

    public DownloadReceipt() {
        mListState = new ArrayList<STATE>();
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
        mDownloadSize = downloadSize;
    }

    public long getDownloadedSize() {
        return mDownloadSize;
    }

    public long getDownloadedSize(int pos) {
        return mDownloadSize;
    }

    public void setDownloadTotalSize(long downloadTotalSize) {
        if (downloadTotalSize == 0) {
            return;
        }
        mDownloadTotalSize = downloadTotalSize;
    }

    public long getDownloadTotalSize() {
        return mDownloadTotalSize;
    }

    @Override
    public String toString() {
        String str = "";

        str += "downloadSize[" + mPosition + "]:" + mDownloadSize +
                ",downloadTotalSize" + mPosition + ":" + mDownloadTotalSize +
                ",downloadState:" + mListState + ";";
        return str;
    }
}
