package com.andretietz.retroauth;

/**
 * Created by andre.tietz on 29/03/16.
 */
public final class AndroidTokenType {

    public final String tokenType;
    public final String accountType;

    private AndroidTokenType(Builder builder) {
        this.accountType = builder.accountType;
        this.tokenType = builder.tokenType;
    }

    public static class Builder {
        private String accountType;
        private String tokenType;

        public Builder accountType(String accountType) {
            this.accountType = accountType;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public AndroidTokenType build() {
            if (accountType == null || tokenType == null) {
                throw new RuntimeException("You must set a valid account and token type");
            }
            return new AndroidTokenType(this);
        }
    }
}
