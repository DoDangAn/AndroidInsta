-- ===================================================================
-- DATABASE OPTIMIZATION - PROFESSIONAL APPROACH
-- ===================================================================
-- Instead of caching complex DTOs in Redis (which causes serialization issues),
-- professional apps (Netflix, Uber, LinkedIn) optimize database queries with proper indexes.
--
-- This approach:
-- 1. Makes queries fast enough that caching DTOs is unnecessary
-- 2. Avoids Jackson serialization compatibility issues with Flutter
-- 3. Keeps data consistent (no stale cache problems)
-- 4. Leverages MySQL's built-in query cache
-- ===================================================================

-- ===================================================================
-- POSTS TABLE INDEXES (Feed, User Posts, Reels)
-- ===================================================================

-- Index for feed queries (sorted by created_at)
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);

-- Index for user posts (filter by user_id, sort by created_at)
CREATE INDEX IF NOT EXISTS idx_posts_user_created ON posts(user_id, created_at DESC);

-- Index for visibility filter (public, private, advertise)
CREATE INDEX IF NOT EXISTS idx_posts_visibility ON posts(visibility);

-- Composite index for user + visibility queries
CREATE INDEX IF NOT EXISTS idx_posts_user_visibility ON posts(user_id, visibility, created_at DESC);

-- ===================================================================
-- FOLLOWS TABLE INDEXES (Friend Requests, Followers, Following)
-- ===================================================================

-- Index for counting followers (who follows me)
CREATE INDEX IF NOT EXISTS idx_follows_followed ON follows(followed_id);

-- Index for counting following (who I follow)
CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id);

-- Composite index for checking if A follows B
CREATE INDEX IF NOT EXISTS idx_follows_pair ON follows(follower_id, followed_id);

-- ===================================================================
-- LIKES TABLE INDEXES (Post Like Count)
-- ===================================================================

-- Index for counting likes per post
CREATE INDEX IF NOT EXISTS idx_likes_post ON likes(post_id);

-- Index for checking if user liked a post
CREATE INDEX IF NOT EXISTS idx_likes_user_post ON likes(user_id, post_id);

-- ===================================================================
-- COMMENTS TABLE INDEXES (Post Comment Count)
-- ===================================================================

-- Index for counting comments per post
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id);

-- Index for nested comments (replies)
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments(parent_comment_id);

-- ===================================================================
-- MESSAGES TABLE INDEXES (Chat Conversations, Unread Count)
-- ===================================================================

-- Index for finding conversations (messages between two users)
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON messages(receiver_id, created_at DESC);

-- Composite index for conversation queries
CREATE INDEX IF NOT EXISTS idx_messages_conversation ON messages(sender_id, receiver_id, created_at DESC);

-- Index for unread messages
CREATE INDEX IF NOT EXISTS idx_messages_unread ON messages(receiver_id, is_read, created_at DESC);

-- ===================================================================
-- USERS TABLE INDEXES (Admin Stats, User Search)
-- ===================================================================

-- Index for active users count
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- Index for verified users count
CREATE INDEX IF NOT EXISTS idx_users_verified ON users(is_verified);

-- Index for new users today (created_at filtering)
CREATE INDEX IF NOT EXISTS idx_users_created ON users(created_at DESC);

-- Index for username search (admin user search)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- ===================================================================
-- MEDIA_FILES TABLE INDEXES (Reels Filter)
-- ===================================================================

-- Index for filtering videos (reels)
CREATE INDEX IF NOT EXISTS idx_media_type ON media_files(file_type);

-- Composite index for post + media type
CREATE INDEX IF NOT EXISTS idx_media_post_type ON media_files(post_id, file_type);

-- ===================================================================
-- PERFORMANCE VERIFICATION QUERIES
-- ===================================================================
-- Run these EXPLAIN queries to verify indexes are being used:

-- Example 1: Feed query (should use idx_posts_created_at)
-- EXPLAIN SELECT * FROM posts WHERE created_at > NOW() - INTERVAL 7 DAY ORDER BY created_at DESC LIMIT 20;

-- Example 2: User posts query (should use idx_posts_user_created)
-- EXPLAIN SELECT * FROM posts WHERE user_id = 1 ORDER BY created_at DESC LIMIT 20;

-- Example 3: Follower count (should use idx_follows_followed)
-- EXPLAIN SELECT COUNT(*) FROM follows WHERE followed_id = 1;

-- Example 4: Like count (should use idx_likes_post)
-- EXPLAIN SELECT COUNT(*) FROM likes WHERE post_id = 1;

-- Example 5: Unread messages (should use idx_messages_unread)
-- EXPLAIN SELECT * FROM messages WHERE receiver_id = 1 AND is_read = false ORDER BY created_at DESC;

-- ===================================================================
-- MYSQL QUERY CACHE CONFIGURATION
-- ===================================================================
-- Add these to my.cnf or my.ini:
--
-- [mysqld]
-- query_cache_type = 1
-- query_cache_size = 128M
-- query_cache_limit = 4M
--
-- MySQL will automatically cache SELECT query results.
-- When data changes (INSERT/UPDATE/DELETE), cache is invalidated automatically.
-- ===================================================================

-- ===================================================================
-- PROFESSIONAL BEST PRACTICES
-- ===================================================================
-- 1. ✅ Database indexes for fast queries (THIS FILE)
-- 2. ✅ MySQL query cache for repeated queries
-- 3. ✅ Redis for SIMPLE data only (counters, flags, IDs)
-- 4. ❌ DO NOT cache complex DTOs via @Cacheable + Jackson
-- 5. ❌ DO NOT use GenericJackson2JsonRedisSerializer for objects
--
-- Netflix, Uber, LinkedIn approach:
-- - Database is PRIMARY source of truth
-- - Indexes make queries fast (< 50ms typical)
-- - Redis for sessions, rate limiting, pub/sub ONLY
-- - Complex responses built fresh from indexed queries
-- ===================================================================
