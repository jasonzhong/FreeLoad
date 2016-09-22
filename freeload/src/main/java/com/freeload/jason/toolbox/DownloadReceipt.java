package com.freeload.jason.toolbox;

import android.text.TextUtils;

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
		FAILED_COMBIN_FILE,     // 13
		TIMEOUT,                // 14
        CONNWRONG,              // 15
        PEPARE_FINISH,          // 16
        FAILED_GET_STREAM,      // 17
        RETRY_DOWNLOAD          // 18
	}

    private int mPosition = 0;
    private STATE mState;

    //当前分区下载进度
    private Long mDownloadedSize = 0l;
    //当前分区总下载容量
    private Long mTotalDownloadSize = 0l;
    //空着
    private Long mStartDownloadPosition = 0l;
    //当前文件写入进度
    private Long mWriteFileSize = 0l;
    //文件总大小
    private Long mWriteFileTotalSize = 0l;
    //文件路径
    private String mDownloadFilePath = "";

    public DownloadReceipt() {
        mState = STATE.NONE;
    }

    public void setDownloadState(STATE state) {
        mState = state;
    }

    public STATE getDownloadState() {
        return mState;
    }

    public void setWriteFileSize(Long size) {
        mWriteFileSize = size;
    }

    public long getWriteFileSize() {
        return mWriteFileSize;
    }

    public void setWriteFileTotalSize(long size) {
        mWriteFileTotalSize = size;
    }

    public long getWriteFileTotalSize() {
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

    public void setTotalDownloadSize(long downloadTotalSize) {
        if (downloadTotalSize == 0) {
            return;
        }
        mTotalDownloadSize = downloadTotalSize;
    }

    public long getDownloadTotalSize() {
        return mTotalDownloadSize;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        if (TextUtils.isEmpty(downloadFilePath)) {
            return;
        }
        mDownloadFilePath = downloadFilePath;
    }

    public String getDownloadFilePath() {
        return mDownloadFilePath;
    }

    @Override
    public String toString() {
        String str = "";

        str += "[" + mPosition + "] downloadSize:" + mDownloadedSize +
                ",downloadTotalSize:" + mTotalDownloadSize +
                ",writeFileSize:" + mWriteFileSize +
                ",writeFileTotalSize:" + mWriteFileTotalSize +
                ",startDownloadPosition:" + mStartDownloadPosition +
                ",downloadState:" + mState + ";";
        return str;
    }
}
