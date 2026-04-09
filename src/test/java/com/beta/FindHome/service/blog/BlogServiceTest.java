package com.beta.FindHome.service.blog;

import com.beta.FindHome.dto.blog.BlogsRequestDTO;
import com.beta.FindHome.dto.blog.BlogsResponseDTO;
import com.beta.FindHome.dto.blog.FilterBlogsRequestDTO;
import com.beta.FindHome.enums.model.BlogCategory;
import com.beta.FindHome.exception.BlogException;
import com.beta.FindHome.factory.service.ImageService;
import com.beta.FindHome.model.Blogs;
import com.beta.FindHome.repository.BlogRepository;
import com.beta.FindHome.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock private BlogRepository blogRepository;
    @Mock private ImageService imageService;
    @Mock private RedisUtils redisUtils;

    @InjectMocks
    private BlogServiceImpl blogService;

    private UUID blogId;
    private Blogs blog;


    @BeforeEach
    void setup() {
        blogId = UUID.randomUUID();

        blog = Blogs.builder()
                .title("Test Blog")
                .content("Content")
                .author("Sawan")
                .blogCategory(BlogCategory.TECHNOLOGY)
                .published(true)
                .blogImageURL("test-url")
                .build();
        blog.setId(blogId);
        blog.setCreatedAt(java.time.LocalDateTime.now());
    }

    // =====================================================================
    // getBlogById() - CACHE HIT
    // =====================================================================

    @Test
    @DisplayName("getBlogById: should return from cache when present")
    void getBlogById_shouldReturnFromCache() {

        BlogsResponseDTO cached = BlogsResponseDTO.builder()
                .id(blogId)
                .title("Cached Blog")
                .build();

        when(redisUtils.keyExists(blogId.toString())).thenReturn(true);
        when(redisUtils.get(blogId.toString(), BlogsResponseDTO.class))
                .thenReturn(cached);

        BlogsResponseDTO result = blogService.getBlogById(blogId);

        assertEquals("Cached Blog", result.getTitle());

        verify(blogRepository, never()).findById(any());
    }

    // =====================================================================
    // getBlogById() - CACHE MISS
    // =====================================================================

    @Test
    @DisplayName("getBlogById: should fetch from DB and cache result when cache miss")
    void getBlogById_shouldFetchFromDBAndCache() {

        when(redisUtils.keyExists(blogId.toString())).thenReturn(false);
        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));

        BlogsResponseDTO result = blogService.getBlogById(blogId);

        assertNotNull(result);
        assertEquals(blogId, result.getId());

        verify(redisUtils).save(eq(blogId.toString()), any(BlogsResponseDTO.class));
    }

    // =====================================================================
    // getBlogById() - NOT FOUND
    // =====================================================================

    @Test
    @DisplayName("getBlogById: should throw exception when blog not found")
    void getBlogById_shouldThrowException_whenNotFound() {

        when(redisUtils.keyExists(blogId.toString())).thenReturn(false);
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        BlogException ex = assertThrows(
                BlogException.class,
                () -> blogService.getBlogById(blogId)
        );

        assertTrue(ex.getMessage().contains("Blog not found"));
    }

    // =====================================================================
    // createBlog()
    // =====================================================================

    @Test
    @DisplayName("createBlog: should save blog with image")
    void createBlog_shouldSaveBlog() {

        BlogsRequestDTO dto = new BlogsRequestDTO();
        dto.setTitle("New Blog");

        MultipartFile file = mock(MultipartFile.class);

        when(imageService.saveImage(file)).thenReturn("image-url");

        blogService.createBlog(dto, file);

        ArgumentCaptor<Blogs> captor = ArgumentCaptor.forClass(Blogs.class);
        verify(blogRepository).save(captor.capture());

        Blogs saved = captor.getValue();
        assertEquals("image-url", saved.getBlogImageURL());
        assertEquals("New Blog", saved.getTitle());
    }

    // =====================================================================
    // softDeleteBlog()
    // =====================================================================

    @Test
    @DisplayName("softDeleteBlog: should update publish status when blog exists")
    void softDeleteBlog_shouldUpdateStatus() {

        when(blogRepository.existsById(blogId)).thenReturn(true);

        blogService.softDeleteBlog(blogId, true);

        verify(blogRepository)
                .updatePublishedStatus(blogId, false);
    }

    @Test
    @DisplayName("softDeleteBlog: should throw exception when blog not found")
    void softDeleteBlog_shouldThrowException_whenNotFound() {

        when(blogRepository.existsById(blogId)).thenReturn(false);

        assertThrows(BlogException.class,
                () -> blogService.softDeleteBlog(blogId, true));

        verify(blogRepository, never())
                .updatePublishedStatus(any(), anyBoolean());
    }

    // =====================================================================
    // blogsFilter() - VALIDATION
    // =====================================================================

    @Test
    @DisplayName("blogsFilter: should throw exception for invalid page/size")
    void blogsFilter_shouldThrowException_forInvalidInput() {

        FilterBlogsRequestDTO filter = new FilterBlogsRequestDTO();

        assertThrows(BlogException.class,
                () -> blogService.blogsFilter(filter, "-1", "10"));

        assertThrows(BlogException.class,
                () -> blogService.blogsFilter(filter, "0", "0"));
    }
}