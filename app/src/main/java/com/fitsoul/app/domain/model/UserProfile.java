package com.fitsoul.app.domain.model;

public class UserProfile {
    private int age;
    private String gender;
    private int heightCm;
    private float weightKg;
    private String fitnessLevel;

    public UserProfile() {
        // Default constructor for Firebase
    }

    public UserProfile(int age, String gender, int heightCm, float weightKg, String fitnessLevel) {
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.fitnessLevel = fitnessLevel;
    }

    // Getters
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public int getHeightCm() { return heightCm; }
    public float getWeightKg() { return weightKg; }
    public String getFitnessLevel() { return fitnessLevel; }

    // Setters
    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeightCm(int heightCm) { this.heightCm = heightCm; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }
}
