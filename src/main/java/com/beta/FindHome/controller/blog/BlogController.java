package com.beta.FindHome.controller.blog;

import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.blog.BlogsRequestDTO;
import com.beta.FindHome.dto.blog.BlogsResponseDTO;
import com.beta.FindHome.dto.blog.FilterBlogsRequestDTO;
import com.beta.FindHome.service.blog.BlogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blog")
@Validated
@Slf4j
@Tag(name = "Blog Management", description = "APIs for managing blog information")
@SecurityRequirement(name = "bearerAuth")
public class BlogController {

    private final BlogService blogService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BlogController(BlogService blogService, ObjectMapper objectMapper) {
        this.blogService = blogService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(
            value = "/create",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> createBlog(
            @Validated @RequestPart(value = "blog", required = true) String dtoAsString,
            @RequestPart(value = "blogImage", required = true) MultipartFile image
    )
            throws JsonProcessingException
    {
        BlogsRequestDTO blogsRequestDTO = objectMapper.readValue(dtoAsString, BlogsRequestDTO.class);
        blogService.createBlog(blogsRequestDTO, image);
        return ResponseEntity.ok(new SuccessResponseDTO("Blog created successfully"));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<BlogsResponseDTO> getBlogById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SuccessResponseDTO> updateBlog(
            @PathVariable(required = true) UUID id,
            @RequestBody BlogsRequestDTO dto
    ) {
        blogService.updateBlog(id, dto);
        return ResponseEntity.ok(new SuccessResponseDTO("Blog updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<SuccessResponseDTO> softDeleteBlog(
            @PathVariable(required = true) UUID id,
            @RequestParam(defaultValue = "true") boolean isDeleted
    ) {
        blogService.softDeleteBlog(id, isDeleted);
        return ResponseEntity.ok(new SuccessResponseDTO("Blog deleted successfully"));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<BlogsResponseDTO>> filterBlogs(
            @RequestBody FilterBlogsRequestDTO filter,
            @RequestParam String page,
            @RequestParam String size
    ) {
        return ResponseEntity.ok(blogService.blogsFilter(filter, page, size));
    }
}
