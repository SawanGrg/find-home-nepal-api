package com.beta.FindHome.repository.specification;

import com.beta.FindHome.dto.blog.FilterBlogsRequestDTO;
import com.beta.FindHome.enums.model.BlogCategory;
import com.beta.FindHome.model.Blogs;
import org.springframework.data.jpa.domain.Specification;

public class BlogSpecification {

    private BlogSpecification() {}

    public static Specification<Blogs> filterByCriteria(FilterBlogsRequestDTO filter) {
        return (root, query, cb) -> {
            if (filter.getCategory() == null) {
                return cb.conjunction();
            }
            try {
                BlogCategory category = BlogCategory.valueOf(filter.getCategory().toUpperCase());
                return cb.equal(root.get("blogCategory"), category);
            } catch (IllegalArgumentException e) {
                // Invalid category value — return no results rather than blowing up
                return cb.disjunction();
            }
        };
    }
}