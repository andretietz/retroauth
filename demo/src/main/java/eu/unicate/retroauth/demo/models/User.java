package eu.unicate.retroauth.demo.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by André on 11.04.2015.
 */
public class User {
    @SerializedName("username")
    private String name;
    @SerializedName("first_name")
    private String firstname;
    @SerializedName("last_name")
    private String lastname;
    @SerializedName("email")
    private String email;

    public User(String name, String firstname, String lastname, String email) {
        this.name = name;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
