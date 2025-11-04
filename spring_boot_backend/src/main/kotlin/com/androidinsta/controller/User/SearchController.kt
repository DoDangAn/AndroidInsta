package com.androidinsta.controller.User

import com.androidinsta.Service.SearchService
import com.androidinsta.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val searchService: SearchService
) {

    /**
     * Tìm kiếm users
     * GET /api/search/users?keyword=john&page=0&size=20
     */
    @GetMapping("/users")
    fun searchUsers(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<UserSearchResult>> {
        if (keyword.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val pageable = PageRequest.of(page, size)
        val results = searchService.searchUsers(keyword.trim(), pageable)
        return ResponseEntity.ok(results)
    }

    /**
     * Tìm kiếm posts
     * GET /api/search/posts?keyword=vacation&page=0&size=20
     */
    @GetMapping("/posts")
    fun searchPosts(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostSearchResult>> {
        if (keyword.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val results = searchService.searchPosts(keyword.trim(), pageable)
        return ResponseEntity.ok(results)
    }

    /**
     * Tìm kiếm reels (video posts)
     * GET /api/search/reels?keyword=dance&page=0&size=20
     */
    @GetMapping("/reels")
    fun searchReels(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostSearchResult>> {
        if (keyword.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val results = searchService.searchReels(keyword.trim(), pageable)
        return ResponseEntity.ok(results)
    }

    /**
     * Tìm kiếm tags
     * GET /api/search/tags?keyword=travel&page=0&size=20
     */
    @GetMapping("/tags")
    fun searchTags(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<TagSearchResult>> {
        if (keyword.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        val results = searchService.searchTags(keyword.trim(), pageable)
        return ResponseEntity.ok(results)
    }

    /**
     * Tìm kiếm tổng hợp (all) - Top 10 mỗi loại
     * GET /api/search/all?keyword=john
     */
    @GetMapping("/all")
    fun searchAll(@RequestParam keyword: String): ResponseEntity<SearchAllResult> {
        if (keyword.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val results = searchService.searchAll(keyword.trim())
        return ResponseEntity.ok(results)
    }

    /**
     * Lấy trending tags
     * GET /api/search/trending/tags?page=0&size=20
     */
    @GetMapping("/trending/tags")
    fun getTrendingTags(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<TagSearchResult>> {
        val pageable = PageRequest.of(page, size)
        val results = searchService.getTrendingTags(pageable)
        return ResponseEntity.ok(results)
    }

    /**
     * Search suggestions (auto-complete)
     * GET /api/search/suggestions?q=joh
     */
    @GetMapping("/suggestions")
    fun getSearchSuggestions(
        @RequestParam("q") query: String,
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<Map<String, List<*>>> {
        if (query.isBlank() || query.length < 2) {
            return ResponseEntity.ok(mapOf(
                "users" to emptyList<UserSearchResult>(),
                "tags" to emptyList<TagSearchResult>()
            ))
        }
        
        val pageable = PageRequest.of(0, limit)
        val users = searchService.searchUsers(query.trim(), pageable).content
        val tags = searchService.searchTags(query.trim(), pageable).content
        
        return ResponseEntity.ok(mapOf(
            "users" to users,
            "tags" to tags
        ))
    }
}
