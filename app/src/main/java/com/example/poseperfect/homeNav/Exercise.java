package com.example.poseperfect.homeNav;

public class Exercise {

    private String name;
    private String description;
    private String healthBenefits;
    private int imageResId;
    private String level;
    private String category;
    private String youtubeUrl;

    public Exercise(String name, String description, String healthBenefits, int imageResId,
                    String level, String category, String youtubeUrl)  {
        this.name = name;
        this.description = description;
        this.healthBenefits = healthBenefits;
        this.imageResId = imageResId;
        this.level = level;
        this.category = category;
        this.youtubeUrl = youtubeUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHealthBenefits() {
        return healthBenefits;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getLevel() {
        return level;
    }

    public String getCategory() {
        return category;
    }
    public String getYoutubeUrl() {
        return youtubeUrl;
    }
}