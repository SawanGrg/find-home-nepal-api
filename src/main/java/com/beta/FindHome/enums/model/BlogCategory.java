package com.beta.FindHome.enums.model;

public enum BlogCategory {
    TECHNOLOGY("Technology"),
    LIFESTYLE("Lifestyle"),
    TRAVEL("Travel"),
    FOOD("Food"),
    HEALTH("Health"),
    FINANCE("Finance"),
    EDUCATION("Education"),
    ENTERTAINMENT("Entertainment"),
    FASHION("Fashion"),
    SPORTS("Sports"),

    // New additions
    REAL_ESTATE("Real Estate"),
    RENTAL_TIPS("Rental Tips"),
    HOME_IMPROVEMENT("Home Improvement"),
    INTERIOR_DESIGN("Interior Design"),
    NEIGHBORHOOD_GUIDES("Neighborhood Guides"),
    MOVING_GUIDES("Moving Guides"),
    UTILITIES_AND_SERVICES("Utilities and Services"),
    LEGAL_AND_FINANCE("Legal and Finance"),
    SUSTAINABLE_LIVING("Sustainable Living"),
    SECURITY_AND_SAFETY("Security and Safety");

    private final String category;

    BlogCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
