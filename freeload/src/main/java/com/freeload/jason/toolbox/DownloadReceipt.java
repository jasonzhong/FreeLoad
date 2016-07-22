package com.freeload.jason.toolbox;

import java.io.Serializable;

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

    private Long mDownloadedSize = 0l;
    private Long mTotalDownloadSize = 0l;
    private Long mStartDownloadPosition = 0l;

    private Long mWriteFileSize = 0l;
    private Long mWriteFileTotalSize = 0l;

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
        mWriteFileSize = size;
    }

    public long getWriteSize() {
        return mWriteFileSize;
    }

    public void setWriteTotalSize(long size) {
        mWriteFileTotalSize = size;
    }

    public long getWriteTotalSize() {
        return mWriteFileTotalSize;
    }

    public void setStartDownloadPosition(long size) {
        mStartDownloadPosition = size;
    }

    public long getStartDownloadPosition() {
        return mStartDownloadPosition;
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
        mDownloadedSize = downloadSize;
    }

    public long getDownloadedSize() {
        return mDownloadedSize;
    }

    public void setDownloadTotalSize(long downloadTotalSize) {
        if (downloadTotalSize == 0) {
            return;
        }
        mTotalDownloadSize = downloadTotalSize;
    }

    public long getDownloadTotalSize() {
        return mTotalDownloadSize;
    }

    @Override
    public String toString() {
        String str = "";

        str += "[" + mPosition + "] downloadSize:" + mDownloadedSize +
                ",downloadTotalSize" + mPosition + ":" + mTotalDownloadSize +
                ",downloadState:" + mState + ";";
        return str;
    }
}
