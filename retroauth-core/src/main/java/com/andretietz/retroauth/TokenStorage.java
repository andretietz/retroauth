package com.andretietz.retroauth;

/**
 * Created by andre on 23.03.2016.
 */
public interface TokenStorage<OWNER, TOKEN_TYPE, TOKEN> {

    TOKEN_TYPE createType(String[] annotationValues);

    TOKEN getToken(OWNER owner, TOKEN_TYPE type) throws AuthenticationCanceledException;

    void removeToken(OWNER owner, TOKEN_TYPE type, TOKEN token);

    void saveToken(OWNER owner, TOKEN_TYPE type, TOKEN token);
}
