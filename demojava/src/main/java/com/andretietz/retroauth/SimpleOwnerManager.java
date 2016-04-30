package com.andretietz.retroauth;

/**
 * Created by andre on 30.04.2016.
 */
public class SimpleOwnerManager implements OwnerManager<String, String> {

    @Override
    public String getOwner(String tokenType) throws ChooseOwnerCanceledException {
        // since we don't care about multiuser here, we return the same thing
        return "retroauth";
    }
}
