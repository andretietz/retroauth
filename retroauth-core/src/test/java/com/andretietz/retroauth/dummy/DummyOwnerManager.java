package com.andretietz.retroauth.dummy;

import com.andretietz.retroauth.ChooseOwnerCanceledException;
import com.andretietz.retroauth.OwnerManager;

/**
 * Created by andre on 02.05.2016.
 */
public class DummyOwnerManager implements OwnerManager<String, String> {
    @Override
    public String getOwner(String s) throws ChooseOwnerCanceledException {
        return null;
    }
}
