package edu.hcmut.datn.productstorage.security.portable;

public class AuthenticatedUser {
    private final Long id;
    private final String email;

    public AuthenticatedUser(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AuthenticatedUser{");
        sb.append("id=").append(id);
        sb.append(", email=").append(email);
        sb.append('}');
        return sb.toString();
    }
}
