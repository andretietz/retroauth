package com.andretietz.retroauth;

/**
 * Created by andre on 15/04/16.
 */
public interface OwnerManager<OWNER, TOKEN_TYPE> {
    OWNER getOwner(TOKEN_TYPE type) throws ChooseOwnerCanceledException;
}
