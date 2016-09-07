package com.freeload.jason.toolbox;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.IReceipt;
import com.freeload.jason.core.Response;

public class EssentialInfo {

    /** Unique id for download. */
    public int mId;

    /** URL of this request. */
    public String mUrl;

    /** download file name */
    public String mFileName = "";

    /** downloaded file store path */
    public String mDownloadedFileFolder = "";

    /** download file thread setting */
    public int mThreadType = DownloadThreadType.NORMAL;

    /** download file customer listener */
    public Response.Listener<IReceipt> mListener = null;

    /** download file receipt */
    public EscapeReceipt mCustomerReceipt = new EscapeReceipt();
}
