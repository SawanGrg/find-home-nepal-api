package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.BlogCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Blogs")
@Table(name = "blogs", indexes = {
        @Index(name = "idx_slug",     columnList = "slug"),
        @Index(name = "idx_author",   columnList = "author"),
        @Index(name = "idx_title",    columnList = "title"),
        @Index(name = "idx_category", columnList = "blog_category")
})
public class Blogs extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private String title;

    // Large text — lazy loaded, only fetched when content is explicitly needed
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "page_description")
    private String pageDescription;

    @Column(name = "blog_description")
    private String blogDescription;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "blog_category", nullable = false)
    private BlogCategory blogCategory;

    // JSONB — structured SEO schema
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> schema;

    @Column(name = "meta_keywords", length = 1000)
    private String metaKeywords;

    @Column(name = "slug")
    private String slug;

    @Column(nullable = false)
    private Boolean published = false;

    @Column(name = "blog_image_url", nullable = false)
    private String blogImageURL;
}

//package com.beta.FindHome.model;
//
//import com.beta.FindHome.enums.model.BlogCategory;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.*;
//
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import java.util.Map;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity(name = "Blogs")
//@Table(name = "blogs", indexes = {
//        @Index(name = "idx_slug", columnList = "slug"),
//        @Index(name = "idx_author", columnList = "author"),
//        @Index(name = "idx_title", columnList = "title"),
//        @Index(name = "idx_category", columnList = "blogCategory")
//})
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Blogs.class
//)
//public class Blogs extends BaseEntity {
//
//    @Column(nullable = false)
//    private String title;            // Blog title
//
//    // For content - use TEXT if it's HTML/markup, JSONB if structured
//    @Column(columnDefinition = "TEXT")
//    @Basic(fetch = FetchType.LAZY)
//    private String content;
//
//    @Column(nullable = false)
//    private String author;           // Blog author
//
//    private String pageTitle;        // Title shown on the page
//    private String pageDescription;  // Page meta description
//    private String blogDescription;  // Blog preview/excerpt
//
//    private String metaTitle;        // SEO meta title
//    private String metaDescription;  // SEO meta description
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "blog_category", nullable = false) // Explicit column name
//    private BlogCategory blogCategory; // Enum-based category
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = "jsonb")
//    private Map<String, Object> schema;  // ← Best practice          // JSON-LD schema for SEO
//
//    @Column(length = 1000)
//    private String metaKeywords;     // Optional meta keywords
//
//    @Column(unique = false)
//    private String slug;             // URL-friendly title version
//
//    @Column(nullable = false)
//    private Boolean published = false;
//
//    @Column(nullable = false, name = "blog_image_url")
//    private String blogImageURL;
//}
