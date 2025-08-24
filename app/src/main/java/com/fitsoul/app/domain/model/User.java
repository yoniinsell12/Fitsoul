package com.fitsoul.app.domain.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private List<String> goals;

    public User() {
        // Default constructor for Firebase
        this.uid = "";
        this.email = "";
        this.displayName = "";
        this.goals = new ArrayList<>();
    }

    public User(String uid, String email, String displayName, List<String> goals) {
        this.uid = uid != null ? uid : "";
        this.email = email != null ? email : "";
        this.displayName = displayName != null ? displayName : "";
        this.goals = goals != null ? goals : new ArrayList<>();
    }

    // Getters
    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public List<String> getGoals() { return goals != null ? goals : new ArrayList<>(); }

    // Setters
    public void setUid(String uid) { this.uid = uid != null ? uid : ""; }
    public void setEmail(String email) { this.email = email != null ? email : ""; }
    public void setDisplayName(String displayName) { this.displayName = displayName != null ? displayName : ""; }
    public void setGoals(List<String> goals) { this.goals = goals != null ? goals : new ArrayList<>(); }

    // Builder pattern
    public static class Builder {
        private String uid = "";
        private String email = "";
        private String displayName = "";
        private List<String> goals = new ArrayList<>();

        public Builder setUid(String uid) { this.uid = uid; return this; }
        public Builder setEmail(String email) { this.email = email; return this; }
        public Builder setDisplayName(String displayName) { this.displayName = displayName; return this; }
        public Builder setGoals(List<String> goals) { this.goals = goals; return this; }
        public Builder setProfile(Object profile) { /* TODO: Add profile support */ return this; }

        public User build() {
            return new User(uid, email, displayName, goals);
        }
    }
}
