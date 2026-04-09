package com.beta.FindHome.service.blog;

import com.beta.FindHome.dto.blog.BlogsRequestDTO;
import com.beta.FindHome.dto.blog.BlogsResponseDTO;
import com.beta.FindHome.dto.blog.FilterBlogsRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BlogService {
    void createBlog(BlogsRequestDTO dto, MultipartFile file);

    BlogsResponseDTO getBlogById(UUID id);

    void updateBlog(UUID id, BlogsRequestDTO dto);

    void softDeleteBlog(UUID id, boolean isDeleted);

    Page<BlogsResponseDTO> blogsFilter(FilterBlogsRequestDTO filter, String page, String size);
}
