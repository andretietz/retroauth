package com.andretietz.retroauth;

public class SimpleOwnerManager implements OwnerManager<String, String> {
    @Override
    public String getOwner(String tokenType) throws ChooseOwnerCanceledException {
        // since we don't care about multiuser here, we return the same thing
        return "retroauth";
    }
}
