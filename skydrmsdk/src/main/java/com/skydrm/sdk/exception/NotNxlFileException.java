package com.skydrm.sdk.exception;

import java.io.IOException;

/**
 * Created by oye on 12/2/2016.
 */

public class NotNxlFileException extends IOException {
    public NotNxlFileException(String detailMessage) {
        super(detailMessage);
    }
}
