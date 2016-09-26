package com.freeload.jason.toolbox;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.IReceipt;
import com.freeload.jason.core.response.Response;

public class EssentialInfo {

    /** Unique id for download. */
    public int mId;

    /** URL of this request. */
    public String mUrl;

    /** download file name */
    public String mFileName = "";

    /** download file size */
    public long mFileSize = 0;

    /** downloaded file store path */
    public String mDownloadedFileFolder = "";

    /** download file thread setting */
    public int mThreadType = DownloadThreadType.NORMAL;

    /** download file customer listener */
    public Response.Listener<IReceipt> mListener = null;

    /** download file customer pepare listener */
    public Response.PepareListener<IReceipt> mPepareListener = null;

    /** download file receipt */
    public EscapeReceipt mCustomerReceipt = new EscapeReceipt();
}
