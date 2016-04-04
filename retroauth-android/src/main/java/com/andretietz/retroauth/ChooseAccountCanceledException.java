package com.andretietz.retroauth;

public class ChooseAccountCanceledException extends Exception {
    public ChooseAccountCanceledException() {
        super();
    }

    public ChooseAccountCanceledException(String detailMessage) {
        super(detailMessage);
    }

    public ChooseAccountCanceledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ChooseAccountCanceledException(Throwable throwable) {
        super(throwable);
    }
}
