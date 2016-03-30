package com.andretietz.retroauth;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is a very optimistic and completely unsecured implementation of
 * storing a token! It's only supposed to show the effect of the retroauth library
 */
public class CredentialStorage implements TokenStorage<String, OAuth2AccessToken> {
    private static final File FILE = new File("tokenstorage.txt");

    public CredentialStorage() {
        createFile();
    }

    private static void createFile() {
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeToken(String type) {
        FILE.delete();
    }

    @Override
    public void saveToken(String type, OAuth2AccessToken token) {
        createFile();
        try {
            FileWriter fw = new FileWriter(FILE);
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(token.getAccessToken());
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public OAuth2AccessToken getToken(String type) {
        createFile();
        BufferedReader reader = null;
        String token = null;
        try {
            reader = new BufferedReader(new FileReader(FILE));
            token = reader.readLine();
            reader.close();
            if(token != null)
                return new OAuth2AccessToken(token);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
