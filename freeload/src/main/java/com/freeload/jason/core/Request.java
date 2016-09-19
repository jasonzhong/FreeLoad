package com.freeload.jason.core;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.File;

public abstract class Request<T> implements Comparable<Request<T>> {

    /** Sequence number of this request, used to enforce FIFO ordering. */
    private Integer mSequence = 0;

    /** The request queue this request is associated with. */
    private RequestQueue mRequestQueue;

    /** Whether or not this request has been canceled. */
    private boolean mCanceled = false;

    /** Unique id for download. */
    private int mId;

    /** download start position. */
    private long mDownloadStart = 0;

    /** download end position. */
    private long mDownloadEnd = 0;

    /** write file start position. */
    private long mWriteFileStart = 0;

    /** write file end position. */
    private long mWriteFileEnd = 0;

    /** URL of this request. */
    private String mUrl;

    /** save file in this request. */
    private File mSaveFile = null;

    /** save file size. */
    private long mDownloadFileSize = 0;

    /** save file size. */
    private long mDownloadFilePerSize = 0;

    /** download file name. */
    private String mFileName;

    /** download file parent folder. */
    private String mFileFolder;

    /** download file thread setting */
    private int mThreadType = DownloadThreadType.NORMAL;

    /** download file thread position */
    private int mThreadPosition = 0;

    /** download file receipt */
    private DownloadReceipt mDownloadReceipt = null;

    private final static String fileSaveDir = Environment.getExternalStorageDirectory() + "/freeload/downloadfile";

    protected Request() {
        this.mFileFolder = parseFileFolder(null);
    }

    public void setId(int id) {
        this.mId = id;
    }

    public void setUrl(String url) {
        this.mUrl = url;
        if (!TextUtils.isEmpty(url)) {
            this.mFileName = setDefaultFileName(url);
        }
    }

    public void setFileFolder(String fileFolder) {
        this.mFileFolder = parseFileFolder(fileFolder);
    }

    public void setDownloadReceipt(DownloadReceipt downloadReceipt) {
        this.mDownloadReceipt = downloadReceipt;
    }

    public DownloadReceipt getDownloadReceipt() {
        return this.mDownloadReceipt;
    }

    private String parseFileFolder(String fileFolder) {
        String downloadFileFolder;
        if (TextUtils.isEmpty(fileFolder)) {
            downloadFileFolder = fileSaveDir;
        } else {
            downloadFileFolder = fileFolder;
        }
        return downloadFileFolder;
    }

    private String setDefaultFileName(String Url) {
        String downloadFileName;

        Uri uri = Uri.parse(Url);
        String strPath = uri.getPath();
        downloadFileName = strPath.substring(strPath.lastIndexOf('/') + 1, strPath.length());

        downloadFileName += ".tmp";
        return downloadFileName;
    }

    protected void setThreadPosition(int position) {
        this.mThreadPosition = position;
    }

    public int getThreadPosition() {
        return this.mThreadPosition;
    }

    protected void setThreadType(int type) {
        this.mThreadType = type;
    }

    public int getThreadType() {
        return this.mThreadType;
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public Priority getPriority() {
        return Priority.NORMAL;
    }

    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    /**
     * Mark this request as canceled.  No callback will be delivered.
     */
    protected void cancel() {
        mCanceled = true;
    }

    /**
     * Returns true if this request has been canceled.
     */
    public boolean isCanceled() {
        return mCanceled;
    }

    /**
     * Returns the sequence number of this request.
     */
    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }

    /**
     * Sets the sequence number of this request.
     *
     * @return This Request object to allow for chaining.
     */
    public final Request<?> setSequence(int sequence) {
        mSequence = sequence;
        return this;
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     * @param response
     */
    //protected abstract void deliverResponse(T response);

    /**
     * Returns the URL of this request.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Returns the id of this request.
     */
    public int getId() {
        return mId;
    }

    /**
     * Returns the start position of this request download.
     */
    public long getDownloadStart() {
        return mDownloadStart;
    }

    public void setDownloadStart(long downloadStart) {
        this.mDownloadStart = downloadStart;
    }

    public long getWriteFileStart() {
        return mWriteFileStart;
    }

    public void setWriteFileStart(long writeFileStart) {
        this.mWriteFileStart = writeFileStart;
    }

    /**
     * Returns the end position of this request download.
     */
    public long getDownloadEnd() {
        return mDownloadEnd;
    }

    public void setDownloadEnd(long downloadEnd) {
        this.mDownloadEnd = downloadEnd;
    }

    public long getWriteFileEnd() {
        return mWriteFileEnd;
    }

    public void setWriteFileEnd(long writeFileEnd) {
        this.mWriteFileEnd = writeFileEnd;
    }

    /**
     * @return the save file objet of this request.
     */
    public File getSaveFile() {
        return mSaveFile;
    }

    /**
     * @return the download file has been download length.
     */
    public long getDownloadFileSize() {
        return mDownloadFileSize;
    }

    /**
     * @param downloadFileSize
     */
    public void setFileSize(long downloadFileSize) {
        mDownloadFileSize = downloadFileSize;
    }

    public long getDownloadFilePerSize() {
        return mDownloadFilePerSize;
    }

    public void setDownloadFilePerSize(long downloadFileperSize) {
        mDownloadFilePerSize = downloadFileperSize;
    }

    /**
     * @return download file name.
     */
    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    /**
     * @return download folder name.
     */
    public String getFolderName() {
        return mFileFolder;
    }

    /**
     * @param saveFile the download file object;
     */
    public void setDownloadFile(File saveFile) {
        mSaveFile = saveFile;
    }

    @Override
    public int compareTo(Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO ordering.
        return left == right ?
                this.mSequence - other.mSequence :
                right.ordinal() - left.ordinal();
    }

    /**
     * Notifies the request queue that this request has finished (successfully or with error).
     */
    public void finish() {
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }
    }

    /** Delivers when download request pepare to the Listener. */
    public abstract void deliverDownloadPepare(T response);

    /** Delivers when download request progress change to the Listener. */
    public abstract void deliverDownloadProgress(T response);
}
