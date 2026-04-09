package com.beta.FindHome.dto.blog;

import com.beta.FindHome.enums.model.BlogCategory;
import lombok.*;

import java.util.Map;

/**
 * DTO for creating or updating blog posts.
 * This class is used to transfer data between the client and server.
 * It includes fields for the blog's title, content, author, SEO metadata, and more.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogsRequestDTO {

    private String title;           // Blog title
    private String content;         // Main content
    private String author;          // Blog author

    private String pageTitle;       // SEO page title
    private String pageDescription; // Page meta description
    private String blogDescription; // Blog preview/excerpt
    private String metaTitle;       // SEO meta title
    private String metaDescription; // SEO meta description

    private BlogCategory blogCategory; // Enum-based category

    private Map<String, Object> schema; // Structured JSON-LD schema

    private String metaKeywords;       // Comma-separated keywords
    private String slug;               // URL-friendly slug
    private String blogImageURL;       // Featured image URL

}
