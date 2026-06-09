package com.nikoloz.secureapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Validation messages reference keys in messages.properties via {key} syntax.
    // spring's LocalValidatorFactoryBean resolves these through MessageSource automatically.
    @NotBlank(message = "{validation.username.notblank}")
    @Size(min = 3, max = 50, message = "{validation.username.size}")
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean enabled = true;

    private String displayName;

    public AppUser() {}

    public AppUser(String username, String password, String role, String displayName) {
        this.username    = username;
        this.password    = password;
        this.role        = role;
        this.displayName = displayName;
        this.enabled     = true;
    }

    public Long getId()                     { return id; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }

    public String getRole()                 { return role; }
    public void setRole(String r)           { this.role = r; }

    public boolean isEnabled()              { return enabled; }
    public void setEnabled(boolean e)       { this.enabled = e; }

    public String getDisplayName()          { return displayName; }
    public void setDisplayName(String d)    { this.displayName = d; }
}
