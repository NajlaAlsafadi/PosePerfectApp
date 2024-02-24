package com.example.poseperfect.homeNav;

public class Exercise {

    private String name;
    private int imageResId;
    private String level;



    private String category;

    public Exercise(String name, int imageResId, String level, String category) {
        this.name = name;
        this.imageResId = imageResId;
        this.level = level;
        this.category = category;
    }

    public String getName() {
        return name;
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

}