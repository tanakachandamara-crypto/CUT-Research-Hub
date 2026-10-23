package model;

import java.io.Serializable;
import java.util.UUID;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private String name, email, passwordHash;

    public User(String name, String email, String pwd) {
        this.name = name;
        this.email = email;
        this.passwordHash = PasswordSupport.hash(pwd);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public boolean checkPwd(String pwd) {
        if (pwd == null) return false;
        if (passwordHash.startsWith("pbkdf2:")) return PasswordSupport.verify(pwd, passwordHash);
        // Upgrade accounts saved by the original application after successful login.
        boolean valid = passwordHash.equals(Integer.toHexString(pwd.hashCode()));
        if (valid) passwordHash = PasswordSupport.hash(pwd);
        return valid;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User && id.equals(((User) other).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return name + " (" + getRole() + ")";
    }

    public abstract String getRole();
    public abstract boolean canCreateRepo();
    public abstract boolean canMerge(PullRequest pr);
    public abstract int getMaxRepos();
}