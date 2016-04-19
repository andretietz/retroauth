package com.andretietz.retroauth;

/**
 * This is the interface of a token storage.
 */
public interface TokenStorage<OWNER, TOKEN_TYPE, TOKEN> {

    /**
     * Creates a token type object. This doe
     *
     * @param annotationValues The values from the {@link Authenticated} annotation
     * @return a token type.
     */
    TOKEN_TYPE createType(String[] annotationValues);

    /**
     * This method returns an authentication token. If there's no token, you should try
     * authenticating your user.
     *
     * @param owner The owner type of the token you want to get
     * @param type the type of the token you want to get
     * @return the token to authenticate your request or {@code null}
     * @throws AuthenticationCanceledException
     */
    TOKEN getToken(OWNER owner, TOKEN_TYPE type) throws AuthenticationCanceledException;

    /**
     * Removes the token of a specific type and owner from the token storage.
     *
     * @param owner Owner of the token
     * @param type Type of the token
     * @param token Token to remove
     */
    void removeToken(OWNER owner, TOKEN_TYPE type, TOKEN token);

    /**
     * Stores a token of a specific type and owner to the token storage.
     *
     * @param owner Owner of the token
     * @param type Type of the token
     * @param token Token to store
     */
    void storeToken(OWNER owner, TOKEN_TYPE type, TOKEN token);
}
