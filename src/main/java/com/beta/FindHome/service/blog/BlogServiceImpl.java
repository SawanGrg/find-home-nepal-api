package com.beta.FindHome.service.blog;

import com.beta.FindHome.dto.blog.BlogsRequestDTO;
import com.beta.FindHome.dto.blog.BlogsResponseDTO;
import com.beta.FindHome.dto.blog.FilterBlogsRequestDTO;
import com.beta.FindHome.enums.model.BlogCategory;
import com.beta.FindHome.exception.BlogException;
import com.beta.FindHome.factory.service.ImageService;
import com.beta.FindHome.model.Blogs;
import com.beta.FindHome.repository.BlogRepository;
import com.beta.FindHome.repository.specification.BlogSpecification;
import com.beta.FindHome.utils.RedisUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final ImageService imageService;
    private final RedisUtils redisUtils;

    @Autowired
    public BlogServiceImpl(
            BlogRepository blogRepository,
            ImageService imageService,
            RedisUtils redisUtils
    ) {
        this.blogRepository = blogRepository;
        this.imageService = imageService;
        this.redisUtils = redisUtils;
    }

    // === CREATE ===
    @Transactional
    public void createBlog(BlogsRequestDTO dto, MultipartFile file) {
        Blogs blog = convertIntoBlogsEntity(dto);
        String imageUrl = imageService.saveImage(file);
        blog.setBlogImageURL(imageUrl);
        blogRepository.save(blog);
    }

    // === READ ===
    public BlogsResponseDTO getBlogById(UUID id) {
        String cacheKey = id.toString();

        if (redisUtils.keyExists(cacheKey)) {
            return redisUtils.get(cacheKey, BlogsResponseDTO.class);
        }

        Blogs blog = blogRepository.findById(id)
                .orElseThrow(() -> new BlogException("Blog not found with id: " + id));

        BlogsResponseDTO responseDTO = mapToBlogsResponseDTO(blog);
        redisUtils.save(cacheKey, responseDTO);
        return responseDTO;
    }

    // === UPDATE ===
    @Transactional
    public void updateBlog(UUID id, BlogsRequestDTO dto) {
        Blogs existing = blogRepository.findById(id)
                .orElseThrow(() -> new BlogException("Blog not found with id: " + id));

        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setAuthor(dto.getAuthor());
        existing.setPageTitle(dto.getPageTitle());
        existing.setPageDescription(dto.getPageDescription());
        existing.setBlogDescription(dto.getBlogDescription());
        existing.setMetaTitle(dto.getMetaTitle());
        existing.setMetaDescription(dto.getMetaDescription());
        existing.setBlogCategory(dto.getBlogCategory() != null
                ? Enum.valueOf(BlogCategory.class, dto.getBlogCategory().toString())
                : null);
        existing.setSchema(dto.getSchema());
        existing.setMetaKeywords(dto.getMetaKeywords());
        existing.setSlug(dto.getSlug());

        blogRepository.save(existing);
    }

    // === SOFT DELETE ===
    @Transactional
    public void softDeleteBlog(UUID id, boolean isDeleted) {
        if (!blogRepository.existsById(id)) {
            throw new BlogException("Blog not found with id: " + id);
        }
        blogRepository.updatePublishedStatus(id, !isDeleted);
    }

    // === FILTER ===
    public Page<BlogsResponseDTO> blogsFilter(
            FilterBlogsRequestDTO filter,
            String pageInt,
            String sizeInt
    ) {
        int page = Integer.parseInt(pageInt);
        int size = Integer.parseInt(sizeInt);

        if (page < 0 || size <= 0) {
            throw new BlogException("Invalid page or size");
        }

        String cacheKey = generateCacheKey(filter, page, size);
        if (isCacheSuitable(filter, page, size)) {
            Page<BlogsResponseDTO> cached = redisUtils.get(cacheKey, Page.class);
            if (cached != null) return cached;
        }

        Sort sort = Sort.unsorted();
        if ("latest".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("oldest".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BlogsResponseDTO> responseDTO = blogRepository
                .findAll(BlogSpecification.filterByCriteria(filter), pageable)
                .map(this::mapToBlogsResponseDTO);

        redisUtils.save(cacheKey, responseDTO);
        return responseDTO;
    }

    // === PRIVATE HELPERS ===
    private String generateCacheKey(FilterBlogsRequestDTO filter, int page, int size) {
        return String.format(
                "blogs:category=%s:sortBy=%s:page=%d:size=%d",
                filter.getCategory() != null ? filter.getCategory() : "all",
                filter.getSortBy() != null ? filter.getSortBy() : "unsorted",
                page,
                size
        );
    }

    private boolean isCacheSuitable(FilterBlogsRequestDTO filter, int page, int size) {
        return (filter.getCategory() == null || filter.getCategory().isEmpty())
                && "latest".equalsIgnoreCase(filter.getSortBy())
                && page == 0
                && size == 10;
    }

    private BlogsResponseDTO mapToBlogsResponseDTO(Blogs blog) {
        return BlogsResponseDTO.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .content(blog.getContent())
                .author(blog.getAuthor())
                .pageTitle(blog.getPageTitle())
                .pageDescription(blog.getPageDescription())
                .blogDescription(blog.getBlogDescription())
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .blogCategory(blog.getBlogCategory())
                .schema(blog.getSchema())
                .metaKeywords(blog.getMetaKeywords())
                .slug(blog.getSlug())
                .blogImageURL(blog.getBlogImageURL())
                .published(blog.getPublished())
                .build();
    }

    private Blogs convertIntoBlogsEntity(BlogsRequestDTO dto) {
        return Blogs.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .pageTitle(dto.getPageTitle())
                .pageDescription(dto.getPageDescription())
                .blogDescription(dto.getBlogDescription())
                .metaTitle(dto.getMetaTitle())
                .metaDescription(dto.getMetaDescription())
                .blogCategory(dto.getBlogCategory() != null
                        ? Enum.valueOf(BlogCategory.class, dto.getBlogCategory().toString())
                        : null)
                .schema(dto.getSchema())
                .metaKeywords(dto.getMetaKeywords())
                .slug(dto.getSlug())
                .blogImageURL(dto.getBlogImageURL())
                .published(true)
                .build();
    }
}

//package com.beta.FindHome.service.blog;
//
//import com.beta.FindHome.dao.BlogDAO;
//import com.beta.FindHome.dto.blog.BlogsRequestDTO;
//import com.beta.FindHome.dto.blog.BlogsResponseDTO;
//import com.beta.FindHome.dto.blog.FilterBlogsRequestDTO;
//import com.beta.FindHome.enums.model.BlogCategory;
//import com.beta.FindHome.exception.BlogException;
//import com.beta.FindHome.factory.service.ImageService;
//import com.beta.FindHome.model.Blogs;
//import com.beta.FindHome.repository.BlogRepository;
//import com.beta.FindHome.utils.RedisUtils;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.UUID;
//
//
//@Service
//public class BlogServiceImpl implements BlogService {
//
//    private final BlogRepository blogRepository;
//    private final BlogDAO blogDAO;
//    private final ImageService imageService;
//    private final RedisUtils redisUtils;
//
//    @Autowired
//    public BlogServiceImpl(
//            BlogRepository blogRepository,
//            BlogDAO blogDAO,
//            ImageService imageService,
//            RedisUtils redisUtils
//    ) {
//        this.blogRepository = blogRepository;
//        this.blogDAO = blogDAO;
//        this.imageService = imageService;
//        this.redisUtils = redisUtils;
//    }
//
//    // === CREATE ===
//    @Transactional
//    public void createBlog(BlogsRequestDTO dto, MultipartFile file) {
//        Blogs blog = convertIntoBlogsEntity(dto);
//        String imageUrl = imageService.saveImage(file);
//        System.out.println("Image URL: " + imageUrl);
//        blog.setBlogImageURL(imageUrl);
//        Blogs saved = blogDAO.save(blog);
//        if (saved == null) {
//            throw new BlogException("Failed to create blog");
//        }
//    }
//
//    // === READ ===
//    public BlogsResponseDTO getBlogById(UUID id) {
//        if(redisUtils.keyExists(id.toString())) {
//            return redisUtils.get(id.toString(), BlogsResponseDTO.class);
//        }
//        Blogs blog = blogDAO.findById(id)
//                .orElseThrow(() -> new BlogException("Blog not found with id: " + id));
//        BlogsResponseDTO responseDTO =  mapToBlogsRequestListDTO(blog);
//
//        if(!redisUtils.keyExists(id.toString())) {
//            redisUtils.save(id.toString(), responseDTO);
//        }
//        return responseDTO;
//    }
//
//    // === UPDATE ===
//    @Transactional
//    public void updateBlog(UUID id, BlogsRequestDTO dto) {
//        Blogs existing = blogDAO.findById(id)
//                .orElseThrow(() -> new BlogException("Blog not found with id: " + id));
//        if (existing == null) {
//            throw new BlogException("Cannot update, blog not found with id: " + id);
//        }
//        Blogs updatedBlog = convertIntoBlogsEntity(dto);
//        blogDAO.update(updatedBlog);
//    }
//
//    // === SOFT DELETE ===
//    @Transactional
//    public void softDeleteBlog( UUID id, boolean isDeleted) {
//        blogDAO.softDelete(id, isDeleted);
//    }
//
//    private String generateCacheKey(FilterBlogsRequestDTO filter, int page, int size) {
//        return String.format(
//                "blogs:category=%s:sortBy=%s:page=%d:size=%d",
//                filter.getCategory() != null ? filter.getCategory() : "all",
//                filter.getSortBy() != null ? filter.getSortBy() : "unsorted",
//                page,
//                size
//        );
//    }
//
//    // === FILTER ===
//    public Page<BlogsResponseDTO> blogsFilter(
//            FilterBlogsRequestDTO filterBlogsRequestDTO,
//            String pageInt,
//            String sizeInt
//    ) {
//        int page = Integer.parseInt(pageInt);
//        int size = Integer.parseInt(sizeInt);
//
//        if (page < 0 || size <= 0) {
//            throw new BlogException("Invalid page or size");
//        }
//
//        String cacheKey = generateCacheKey(filterBlogsRequestDTO, page, size);
//        if (isCacheSuitable(filterBlogsRequestDTO, page, size)) {
//            Page<BlogsResponseDTO> cached = redisUtils.get(cacheKey, Page.class);
//            if (cached != null) return cached;
//        }
//
//        Sort sort = Sort.unsorted();
//        String sortBy = filterBlogsRequestDTO.getSortBy();
//        if ("latest".equalsIgnoreCase(sortBy)) {
//            sort = Sort.by(Sort.Direction.DESC, "createdAt");
//        } else if ("oldest".equalsIgnoreCase(sortBy)) {
//            sort = Sort.by(Sort.Direction.ASC, "createdAt");
//        }
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//        Page<Blogs> blogPage = blogRepository.findAll(
//                blogDAO.blogFilterByCriteria(filterBlogsRequestDTO),
//                pageable
//        );
//         Page<BlogsResponseDTO> responseDTO = blogPage.map(this::mapToBlogsRequestListDTO);
//        if (responseDTO != null) {
//            redisUtils.save(cacheKey, responseDTO);
//        }
//        return responseDTO;
//    }
//
//    private boolean isCacheSuitable(FilterBlogsRequestDTO blog, int page, int size) {
//        return (
//                blog.getCategory() == null || blog.getCategory().isEmpty() &&
//                blog.getSortBy() == "latest" &&
//                page == 0 &&
//                size == 10
//        );
//    }
//
//    // === Mapping ===
//    private BlogsResponseDTO mapToBlogsRequestListDTO(Blogs blog) {
//        return BlogsResponseDTO
//                .builder()
//                .id(blog.getId())
//                .title(blog.getTitle())
//                .content(blog.getContent())
//                .author(blog.getAuthor())
//                .pageTitle(blog.getPageTitle())
//                .pageDescription(blog.getPageDescription())
//                .blogDescription(blog.getBlogDescription())
//                .metaTitle(blog.getMetaTitle())
//                .metaDescription(blog.getMetaDescription())
//                .blogCategory(blog.getBlogCategory())
//                .schema(blog.getSchema())
//                .metaKeywords(blog.getMetaKeywords())
//                .slug(blog.getSlug())
//                .blogImageURL(blog.getBlogImageURL())
//                .published(blog.getPublished())
//                .build();
//    }
//
//    private Blogs convertIntoBlogsEntity(BlogsRequestDTO dto) {
//        return Blogs.builder()
//                .title(dto.getTitle())
//                .content(dto.getContent())
//                .author(dto.getAuthor())
//                .pageTitle(dto.getPageTitle())
//                .pageDescription(dto.getPageDescription())
//                .blogDescription(dto.getBlogDescription())
//                .metaTitle(dto.getMetaTitle())
//                .metaDescription(dto.getMetaDescription())
//                .blogCategory(dto.getBlogCategory() != null
//                        ? Enum.valueOf(BlogCategory.class, dto.getBlogCategory().toString())
//                        : null)
//                .schema(dto.getSchema())
//                .metaKeywords(dto.getMetaKeywords())
//                .slug(dto.getSlug())
//                .blogImageURL(dto.getBlogImageURL())
//                .published(true)
//                .build();
//    }
//}
