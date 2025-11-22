import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/search_models.dart';
import '../models/post_models.dart';
import '../services/search_service.dart';
import '../services/post_service.dart';
import '../config/api_config.dart';
import 'user_profile_screen.dart';
import 'post_detail_screen.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({Key? key}) : super(key: key);

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen>
    with SingleTickerProviderStateMixin {
  final SearchService _searchService = SearchService();
  final TextEditingController _searchController = TextEditingController();

  late TabController _tabController;
  String _searchQuery = '';
  bool _isLoading = false;
  bool _showSuggestions = false;

  // Search results
  List<UserSearchResult> _userResults = [];
  List<PostSearchResult> _postResults = [];
  List<TagSearchResult> _tagResults = [];
  List<PostDto> _recentPosts = [];
  bool _isLoadingRecent = true;

  // Suggestions
  SearchSuggestions? _suggestions;

  // Pagination
  int _currentPage = 0;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _tabController.addListener(_onTabChanged);
    _loadRecentPosts();
  }

  @override
  void dispose() {
    _searchController.dispose();
    _tabController.dispose();
    super.dispose();
  }

  void _onTabChanged() {
    if (_tabController.indexIsChanging) {
      setState(() {
        _currentPage = 0;
        _hasMore = true;
      });
      if (_searchQuery.isNotEmpty) {
        _performSearch();
      }
    }
  }

  Future<void> _loadRecentPosts() async {
    print('=== LOADING RECENT POSTS ===');
    setState(() {
      _isLoadingRecent = true;
    });
    
    try {
      print('Calling PostService.getAdvertisePosts...');
      final response = await PostService.getAdvertisePosts(page: 0, size: 20);
      print('Response received: ${response.posts.length} posts');
      setState(() {
        _recentPosts = response.posts;
        _isLoadingRecent = false;
      });
      print('Recent posts loaded successfully: ${_recentPosts.length} posts');
    } catch (e, stackTrace) {
      print('Error loading recent posts: $e');
      print('Stack trace: $stackTrace');
      setState(() {
        _isLoadingRecent = false;
      });
    }
  }

  Future<void> _performSearch() async {
    if (_searchQuery.trim().isEmpty) return;

    setState(() {
      _isLoading = true;
      _showSuggestions = false;
    });

    try {
      final currentTab = _tabController.index;

      switch (currentTab) {
        case 0: // All
          final result = await _searchService.searchAll(keyword: _searchQuery);
          setState(() {
            _userResults = result.users;
            _postResults = result.posts;
            _tagResults = result.tags;
          });
          break;
        case 1: // Users
          final response = await _searchService.searchUsers(
            keyword: _searchQuery,
            page: _currentPage,
          );
          setState(() {
            if (_currentPage == 0) {
              _userResults = response.content;
            } else {
              _userResults.addAll(response.content);
            }
            _hasMore = _currentPage < response.totalPages - 1;
          });
          break;
        case 2: // Posts
          final response = await _searchService.searchPosts(
            keyword: _searchQuery,
            page: _currentPage,
          );
          setState(() {
            if (_currentPage == 0) {
              _postResults = response.content;
            } else {
              _postResults.addAll(response.content);
            }
            _hasMore = _currentPage < response.totalPages - 1;
          });
          break;
        case 3: // Tags
          final response = await _searchService.searchTags(
            keyword: _searchQuery,
            page: _currentPage,
          );
          setState(() {
            if (_currentPage == 0) {
              _tagResults = response.content;
            } else {
              _tagResults.addAll(response.content);
            }
            _hasMore = _currentPage < response.totalPages - 1;
          });
          break;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Search error: $e')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _loadSuggestions(String query) async {
    if (query.length < 2) {
      setState(() {
        _suggestions = null;
        _showSuggestions = false;
      });
      return;
    }

    try {
      final suggestions = await _searchService.getSearchSuggestions(query: query);
      setState(() {
        _suggestions = suggestions;
        _showSuggestions = true;
      });
    } catch (e) {
      print('Error loading suggestions: $e');
    }
  }

  void _onSearchChanged(String value) {
    setState(() {
      _searchQuery = value;
    });
    _loadSuggestions(value);
  }

  void _onSearchSubmitted(String value) {
    setState(() {
      _searchQuery = value;
      _currentPage = 0;
      _hasMore = true;
    });
    _performSearch();
  }

  void _loadMore() {
    if (!_isLoading && _hasMore) {
      setState(() {
        _currentPage++;
      });
      _performSearch();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: Container(
          height: 40,
          decoration: BoxDecoration(
            color: Colors.grey[100],
            borderRadius: BorderRadius.circular(12),
          ),
          child: TextField(
            controller: _searchController,
            onChanged: _onSearchChanged,
            onSubmitted: _onSearchSubmitted,
            decoration: InputDecoration(
              hintText: 'Search...',
              hintStyle: TextStyle(color: Colors.grey[400]),
              prefixIcon: Icon(Icons.search, color: Colors.grey[600], size: 20),
              suffixIcon: _searchQuery.isNotEmpty
                  ? IconButton(
                      icon: Icon(Icons.clear, color: Colors.grey[600], size: 20),
                      onPressed: () {
                        _searchController.clear();
                        setState(() {
                          _searchQuery = '';
                          _showSuggestions = false;
                          _userResults = [];
                          _postResults = [];
                          _tagResults = [];
                        });
                      },
                    )
                  : null,
              border: InputBorder.none,
              contentPadding: const EdgeInsets.symmetric(vertical: 10),
            ),
          ),
        ),
        bottom: _searchQuery.isNotEmpty && !_showSuggestions
            ? TabBar(
                controller: _tabController,
                labelColor: Colors.black,
                unselectedLabelColor: Colors.grey,
                indicatorColor: Colors.black,
                tabs: const [
                  Tab(text: 'All'),
                  Tab(text: 'Accounts'),
                  Tab(text: 'Posts'),
                  Tab(text: 'Tags'),
                ],
              )
            : null,
      ),
      body: _showSuggestions
          ? _buildSuggestions()
          : _searchQuery.isEmpty
              ? _buildTrendingSection()
              : _buildSearchResults(),
    );
  }

  Widget _buildSuggestions() {
    if (_suggestions == null) {
      return const Center(child: CircularProgressIndicator());
    }

    return ListView(
      children: [
        if (_suggestions!.users.isNotEmpty) ...[
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'Accounts',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          ..._suggestions!.users.map((user) => _buildUserSuggestionItem(user)),
        ],
        if (_suggestions!.tags.isNotEmpty) ...[
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'Tags',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          ..._suggestions!.tags.map((tag) => _buildTagSuggestionItem(tag)),
        ],
      ],
    );
  }

  Widget _buildUserSuggestionItem(UserSearchResult user) {
    return ListTile(
      leading: CircleAvatar(
        backgroundImage: user.avatarUrl != null
            ? NetworkImage(user.avatarUrl!)
            : null,
        child: user.avatarUrl == null
            ? Text(user.username[0].toUpperCase())
            : null,
      ),
      title: Row(
        children: [
          Text(user.username),
          if (user.isVerified) ...[
            const SizedBox(width: 4),
            const Icon(Icons.verified, color: Colors.blue, size: 16),
          ],
        ],
      ),
      subtitle: user.fullName != null ? Text(user.fullName!) : null,
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => UserProfileScreen(userId: user.id),
          ),
        );
      },
    );
  }

  Widget _buildTagSuggestionItem(TagSearchResult tag) {
    return ListTile(
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: Colors.grey[200],
          shape: BoxShape.circle,
        ),
        child: const Icon(Icons.tag, color: Colors.grey),
      ),
      title: Text('#${tag.name}'),
      subtitle: Text('${tag.postsCount} posts'),
      onTap: () {
        _searchController.text = tag.name;
        _onSearchSubmitted(tag.name);
      },
    );
  }

  Widget _buildTrendingSection() {
    return RefreshIndicator(
      onRefresh: _loadRecentPosts,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'Recent Posts',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          if (_isLoadingRecent)
            const Expanded(
              child: Center(child: CircularProgressIndicator()),
            )
          else if (_recentPosts.isEmpty)
            Expanded(
              child: ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                children: const [
                  SizedBox(height: 100),
                  Center(
                    child: Text(
                      'No posts available',
                      style: TextStyle(color: Colors.grey),
                    ),
                  ),
                ],
              ),
            )
          else
            Expanded(
              child: GridView.builder(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(2),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  crossAxisSpacing: 2,
                  mainAxisSpacing: 2,
                ),
                itemCount: _recentPosts.length,
                itemBuilder: (context, index) {
                  final post = _recentPosts[index];
                  return GestureDetector(
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => PostDetailScreen(post: post),
                        ),
                      );
                    },
                    child: _buildRecentPostItem(post),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildRecentPostItem(PostDto post) {
    final mediaFile = post.mediaFiles.isNotEmpty ? post.mediaFiles[0] : null;
    
    return Stack(
      fit: StackFit.expand,
      children: [
        if (mediaFile != null)
          Image.network(
            mediaFile.fileUrl,
            fit: BoxFit.cover,
            errorBuilder: (context, error, stackTrace) {
              return Container(
                color: Colors.grey[300],
                child: const Icon(Icons.image, color: Colors.grey),
              );
            },
          )
        else
          Container(
            color: Colors.grey[300],
            child: const Icon(Icons.image, color: Colors.grey),
          ),
        if (post.mediaFiles.length > 1)
          Positioned(
            top: 8,
            right: 8,
            child: Container(
              padding: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                color: Colors.black54,
                borderRadius: BorderRadius.circular(4),
              ),
              child: const Icon(
                Icons.collections,
                color: Colors.white,
                size: 16,
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildSearchResults() {
    if (_isLoading && _currentPage == 0) {
      return const Center(child: CircularProgressIndicator());
    }

    return TabBarView(
      controller: _tabController,
      children: [
        _buildAllResults(),
        _buildUserResults(),
        _buildPostResults(),
        _buildTagResults(),
      ],
    );
  }

  Widget _buildAllResults() {
    return ListView(
      children: [
        if (_userResults.isNotEmpty) ...[
          _buildSectionHeader('Accounts', () {
            _tabController.animateTo(1);
          }),
          ..._userResults.take(3).map((user) => _buildUserItem(user)),
        ],
        if (_postResults.isNotEmpty) ...[
          _buildSectionHeader('Posts', () {
            _tabController.animateTo(2);
          }),
          _buildPostGrid(_postResults.take(6).toList()),
        ],
        if (_tagResults.isNotEmpty) ...[
          _buildSectionHeader('Tags', () {
            _tabController.animateTo(3);
          }),
          ..._tagResults.take(3).map((tag) => _buildTagItem(tag)),
        ],
        if (_userResults.isEmpty && _postResults.isEmpty && _tagResults.isEmpty)
          const Center(
            child: Padding(
              padding: EdgeInsets.all(32),
              child: Text('No results found'),
            ),
          ),
      ],
    );
  }

  Widget _buildSectionHeader(String title, VoidCallback onSeeAll) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          TextButton(
            onPressed: onSeeAll,
            child: const Text('See all'),
          ),
        ],
      ),
    );
  }

  Widget _buildUserResults() {
    return NotificationListener<ScrollNotification>(
      onNotification: (scrollInfo) {
        if (scrollInfo.metrics.pixels == scrollInfo.metrics.maxScrollExtent) {
          _loadMore();
        }
        return false;
      },
      child: ListView.builder(
        itemCount: _userResults.length + (_isLoading ? 1 : 0),
        itemBuilder: (context, index) {
          if (index == _userResults.length) {
            return const Center(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: CircularProgressIndicator(),
              ),
            );
          }
          return _buildUserItem(_userResults[index]);
        },
      ),
    );
  }

  Widget _buildUserItem(UserSearchResult user) {
    return ListTile(
      leading: CircleAvatar(
        radius: 25,
        backgroundImage: user.avatarUrl != null
            ? NetworkImage(user.avatarUrl!)
            : null,
        child: user.avatarUrl == null
            ? Text(user.username[0].toUpperCase())
            : null,
      ),
      title: Row(
        children: [
          Text(
            user.username,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          if (user.isVerified) ...[
            const SizedBox(width: 4),
            const Icon(Icons.verified, color: Colors.blue, size: 16),
          ],
        ],
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (user.fullName != null) Text(user.fullName!),
          Text(
            '${user.followersCount} followers',
            style: TextStyle(color: Colors.grey[600], fontSize: 12),
          ),
        ],
      ),
      trailing: _buildFollowButton(user),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => UserProfileScreen(userId: user.id),
          ),
        );
      },
    );
  }

  Widget _buildFollowButton(UserSearchResult user) {
    // Don't show follow button if it's the current user
    // You can add logic here to check if user.id == currentUserId
    
    return SizedBox(
      width: 100,
      child: ElevatedButton(
        onPressed: () => _toggleFollow(user),
        style: ElevatedButton.styleFrom(
          backgroundColor: user.isFollowing ? Colors.grey[300] : Colors.blue,
          foregroundColor: user.isFollowing ? Colors.black : Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
        child: Text(
          user.isFollowing ? 'Following' : 'Follow',
          style: const TextStyle(fontSize: 12),
        ),
      ),
    );
  }

  Future<void> _toggleFollow(UserSearchResult user) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('access_token');
      
      if (token == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Please login')),
        );
        return;
      }

      final url = user.isFollowing
          ? '${ApiConfig.baseUrl}/api/users/${user.id}/follow'
          : '${ApiConfig.baseUrl}/api/users/${user.id}/follow';
      
      final response = user.isFollowing
          ? await http.delete(
              Uri.parse(url),
              headers: {
                'Authorization': 'Bearer $token',
                'Content-Type': 'application/json',
              },
            )
          : await http.post(
              Uri.parse(url),
              headers: {
                'Authorization': 'Bearer $token',
                'Content-Type': 'application/json',
              },
            );

      if (response.statusCode == 200) {
        setState(() {
          // Update the follow status in the results
          final index = _userResults.indexWhere((u) => u.id == user.id);
          if (index != -1) {
            _userResults[index] = UserSearchResult(
              id: user.id,
              username: user.username,
              fullName: user.fullName,
              avatarUrl: user.avatarUrl,
              isVerified: user.isVerified,
              followersCount: user.isFollowing 
                  ? user.followersCount - 1 
                  : user.followersCount + 1,
              isFollowing: !user.isFollowing,
            );
          }
        });
        
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(user.isFollowing ? 'Unfollowed' : 'Followed'),
          ),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  Widget _buildPostResults() {
    return NotificationListener<ScrollNotification>(
      onNotification: (scrollInfo) {
        if (scrollInfo.metrics.pixels == scrollInfo.metrics.maxScrollExtent) {
          _loadMore();
        }
        return false;
      },
      child: _postResults.isEmpty
          ? const Center(child: Text('No posts found'))
          : GridView.builder(
              padding: const EdgeInsets.all(2),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                crossAxisSpacing: 2,
                mainAxisSpacing: 2,
              ),
              itemCount: _postResults.length + (_isLoading ? 1 : 0),
              itemBuilder: (context, index) {
                if (index == _postResults.length) {
                  return const Center(child: CircularProgressIndicator());
                }
                return _buildPostGridItem(_postResults[index]);
              },
            ),
    );
  }

  Widget _buildPostGrid(List<PostSearchResult> posts) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      padding: const EdgeInsets.all(2),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 2,
        mainAxisSpacing: 2,
      ),
      itemCount: posts.length,
      itemBuilder: (context, index) => _buildPostGridItem(posts[index]),
    );
  }

  Widget _buildPostGridItem(PostSearchResult post) {
    final mediaFile = post.mediaFiles.isNotEmpty ? post.mediaFiles[0] : null;

    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => PostDetailScreen(postId: post.id),
          ),
        );
      },
      child: Stack(
        fit: StackFit.expand,
        children: [
          if (mediaFile != null)
            Image.network(
              mediaFile.thumbnailUrl ?? mediaFile.fileUrl,
              fit: BoxFit.cover,
              errorBuilder: (context, error, stackTrace) {
                return Container(
                  color: Colors.grey[300],
                  child: const Icon(Icons.image, color: Colors.grey),
                );
              },
            )
          else
            Container(
              color: Colors.grey[300],
              child: const Icon(Icons.image, color: Colors.grey),
            ),
          if (mediaFile?.fileType == 'VIDEO')
            Positioned(
              top: 8,
              right: 8,
              child: Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: Colors.black54,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Icon(
                  Icons.play_arrow,
                  color: Colors.white,
                  size: 16,
                ),
              ),
            ),
          if (post.mediaFiles.length > 1)
            Positioned(
              top: 8,
              right: 8,
              child: Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: Colors.black54,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Icon(
                  Icons.collections,
                  color: Colors.white,
                  size: 16,
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildTagResults() {
    return NotificationListener<ScrollNotification>(
      onNotification: (scrollInfo) {
        if (scrollInfo.metrics.pixels == scrollInfo.metrics.maxScrollExtent) {
          _loadMore();
        }
        return false;
      },
      child: ListView.builder(
        itemCount: _tagResults.length + (_isLoading ? 1 : 0),
        itemBuilder: (context, index) {
          if (index == _tagResults.length) {
            return const Center(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: CircularProgressIndicator(),
              ),
            );
          }
          return _buildTagItem(_tagResults[index]);
        },
      ),
    );
  }

  Widget _buildTagItem(TagSearchResult tag) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.all(16),
        leading: Container(
          width: 50,
          height: 50,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [Colors.blue, Colors.purple],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            shape: BoxShape.circle,
          ),
          child: const Icon(Icons.tag, color: Colors.white),
        ),
        title: Text(
          '#${tag.name}',
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        subtitle: Text('${tag.postsCount} bài viết'),
        trailing: const Icon(Icons.arrow_forward_ios, size: 16),
        onTap: () {
          // TODO: Navigate to tag posts screen
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Xem bài viết với tag #${tag.name}')),
          );
        },
      ),
    );
  }
}
