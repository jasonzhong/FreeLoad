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
    private STATE mState;

    private Long mDownloadSize = 0l;
    private Long mDownloadTotalSize = 0l;
    private Long mWriteSize = 0l;
    private Long mWriteTotalSize = 0l;

    public DownloadReceipt() {
        mState = STATE.NONE;
    }

    public void setDownloadState(STATE state) {
        mState = state;
    }

    public STATE getDownloadState() {
        return mState;
    }

    public void setWriteSize(Long size) {
        mWriteSize = size;
    }

    public long getWriteSize() {
        return mWriteSize;
    }

    public void setWriteTotalSize(long size) {
        mWriteTotalSize = size;
    }

    public long getWriteTotalSize() {
        return mWriteTotalSize;
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

        str += "[" + mPosition + "] downloadSize:" + mDownloadSize +
                ",downloadTotalSize" + mPosition + ":" + mDownloadTotalSize +
                ",downloadState:" + mState + ";";
        return str;
    }
}
