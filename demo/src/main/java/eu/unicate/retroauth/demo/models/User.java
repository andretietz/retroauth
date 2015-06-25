package eu.unicate.retroauth.demo.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("username")
    public final String name;
    @SerializedName("first_name")
    public final String firstname;
    @SerializedName("last_name")
    public final String lastname;
    @SerializedName("email")
    public final String email;

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
