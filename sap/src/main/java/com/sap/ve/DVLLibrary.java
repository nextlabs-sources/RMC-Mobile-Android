/*
 (C) 2015 SAP SE or an SAP affiliate company. All rights reserved.
*/
package com.sap.ve;

import com.sap.ve.DVLTypes.*;

public class DVLLibrary
{
    private long m_handle = 0;

    public DVLLibrary(long handle)
    {
        m_handle = handle;
    }

    public DVLRESULT RetrieveThumbnail(String filename, SDVLImage image)
    {
        return DVLRESULT.fromInt(nativeRetrieveThumbnail(m_handle, filename, image));
    }

    // native stuff

    static private native int nativeRetrieveThumbnail(long hLibrary, String filename, Object image);
}
