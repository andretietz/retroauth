package com.andretietz.retroauth;

public class ChooseOwnerCanceledException extends Exception {
    public ChooseOwnerCanceledException(String detailMessage) {
        super(detailMessage);
    }
    public ChooseOwnerCanceledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public ChooseOwnerCanceledException(Throwable throwable) {
        super(throwable);
    }
}
