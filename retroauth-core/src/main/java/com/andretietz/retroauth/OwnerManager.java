package com.andretietz.retroauth;

/**
 * Since every token belongs to a specific user, this users have to be managed.
 */
public interface OwnerManager<OWNER, TOKEN_TYPE> {
    /**
     * This method should be used to figure out which user should be authenticate a request.
     * If you're on multi-user systems, you should ask the user to choose which owner
     * he wants to use to authenticate requests. if the user cancels to choose the owner, throw
     * {@link ChooseOwnerCanceledException}
     *
     * @param type type of the token
     * @return the owner of the token of the give token type.
     * @throws ChooseOwnerCanceledException when the user cancels to choose the owner
     */
    OWNER getOwner(TOKEN_TYPE type) throws ChooseOwnerCanceledException;
}
