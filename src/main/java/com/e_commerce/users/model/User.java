package com.e_commerce.users.model;

import com.e_commerce.users.constraints.PasswordConstraint;
import com.e_commerce.users.constraints.UsernameConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @UsernameConstraint
    @Column(unique = true, nullable = false)
    private String username;

    @NotEmpty(message = "Email must be provided.")
    @Email(message = "Email is invalid.")
    @Size(max = 254, message = "Email must contain between 3 and 254 characters.")
    @Column(nullable = false)
    private String email;

    @Size(min = 1, max = 200, message = "Description must contain between 1 and 200 characters.")
    private String description;

    @PasswordConstraint
    @Column(nullable = false)
    private String password;

    public User() {

    }

    public User(String username, String email, String description, String password) {
        this.username = username;
        this.email = email;
        this.description = description;
        this.password = password;
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username) &&
                Objects.equals(email, user.email) &&
                Objects.equals(description, user.description) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, description, password);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
