import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class FeedScreen extends StatefulWidget {
  const FeedScreen({Key? key}) : super(key: key);

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> {
  List<Post> _posts = [];
  bool _isLoading = false;
  int _currentPage = 0;
  bool _hasMore = true;
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _loadFeed();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >= 
        _scrollController.position.maxScrollExtent * 0.9) {
      if (!_isLoading && _hasMore) {
        _loadMorePosts();
      }
    }
  }

  Future<void> _loadFeed() async {
    setState(() {
      _isLoading = true;
      _currentPage = 0;
    });

    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt_token');

    if (token == null) {
      Navigator.pushReplacementNamed(context, '/login');
      return;
    }

    try {
      final response = await http.get(
        Uri.parse('http://localhost:8081/api/posts/feed?page=0&size=20'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        setState(() {
          _posts = (data['posts'] as List)
              .map((post) => Post.fromJson(post))
              .toList();
          _hasMore = _currentPage < data['totalPages'] - 1;
          _isLoading = false;
        });
      } else {
        _showError('Failed to load feed');
      }
    } catch (e) {
      _showError('Error: $e');
    }
  }

  Future<void> _loadMorePosts() async {
    if (_isLoading || !_hasMore) return;

    setState(() {
      _isLoading = true;
    });

    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt_token');
    final nextPage = _currentPage + 1;

    try {
      final response = await http.get(
        Uri.parse('http://localhost:8081/api/posts/feed?page=$nextPage&size=20'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        setState(() {
          _posts.addAll((data['posts'] as List)
              .map((post) => Post.fromJson(post))
              .toList());
          _currentPage = nextPage;
          _hasMore = _currentPage < data['totalPages'] - 1;
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
    }
  }

  void _showError(String message) {
    setState(() {
      _isLoading = false;
    });
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Feed'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_photo_alternate),
            onPressed: () {
              // TODO: Navigate to create post screen
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _loadFeed,
        child: _posts.isEmpty && _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _posts.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.photo_library, size: 64, color: Colors.grey),
                        const SizedBox(height: 16),
                        const Text('No posts yet', style: TextStyle(fontSize: 18)),
                        const SizedBox(height: 8),
                        const Text('Follow people to see their posts'),
                        const SizedBox(height: 16),
                        ElevatedButton(
                          onPressed: () {
                            // TODO: Navigate to explore/search
                          },
                          child: const Text('Find People'),
                        ),
                      ],
                    ),
                  )
                : ListView.builder(
                    controller: _scrollController,
                    itemCount: _posts.length + (_hasMore ? 1 : 0),
                    itemBuilder: (context, index) {
                      if (index == _posts.length) {
                        return const Center(
                          child: Padding(
                            padding: EdgeInsets.all(16.0),
                            child: CircularProgressIndicator(),
                          ),
                        );
                      }
                      return PostCard(post: _posts[index]);
                    },
                  ),
      ),
    );
  }
}

class PostCard extends StatelessWidget {
  final Post post;

  const PostCard({Key? key, required this.post}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: Avatar + Username
          ListTile(
            leading: CircleAvatar(
              backgroundImage: post.user.avatarUrl != null
                  ? NetworkImage(post.user.avatarUrl!)
                  : null,
              child: post.user.avatarUrl == null
                  ? Text(post.user.username[0].toUpperCase())
                  : null,
            ),
            title: Text(
              post.user.username,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            subtitle: post.user.fullName != null
                ? Text(post.user.fullName!)
                : null,
            trailing: IconButton(
              icon: const Icon(Icons.more_vert),
              onPressed: () {
                // TODO: Show post options
              },
            ),
          ),
          // Media (Images/Videos)
          if (post.mediaFiles.isNotEmpty)
            SizedBox(
              height: 400,
              child: PageView.builder(
                itemCount: post.mediaFiles.length,
                itemBuilder: (context, index) {
                  final media = post.mediaFiles[index];
                  if (media.fileType == 'IMAGE') {
                    return Image.network(
                      media.fileUrl,
                      fit: BoxFit.cover,
                      errorBuilder: (context, error, stackTrace) {
                        return Container(
                          color: Colors.grey[300],
                          child: const Icon(Icons.broken_image, size: 64),
                        );
                      },
                    );
                  } else {
                    // TODO: Video player
                    return Container(
                      color: Colors.black,
                      child: const Center(
                        child: Icon(Icons.play_circle_outline, 
                            size: 64, color: Colors.white),
                      ),
                    );
                  }
                },
              ),
            ),
          // Action buttons
          Row(
            children: [
              IconButton(
                icon: const Icon(Icons.favorite_border),
                onPressed: () {
                  // TODO: Like post
                },
              ),
              Text('${post.likesCount}'),
              const SizedBox(width: 16),
              IconButton(
                icon: const Icon(Icons.comment_outlined),
                onPressed: () {
                  // TODO: Open comments
                },
              ),
              Text('${post.commentsCount}'),
              const Spacer(),
              IconButton(
                icon: const Icon(Icons.bookmark_border),
                onPressed: () {
                  // TODO: Save post
                },
              ),
            ],
          ),
          // Caption
          if (post.caption.isNotEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: RichText(
                text: TextSpan(
                  style: DefaultTextStyle.of(context).style,
                  children: [
                    TextSpan(
                      text: '${post.user.username} ',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    TextSpan(text: post.caption),
                  ],
                ),
              ),
            ),
          // Time
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
            child: Text(
              _formatTime(post.createdAt),
              style: TextStyle(color: Colors.grey[600], fontSize: 12),
            ),
          ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }

  String _formatTime(String timestamp) {
    try {
      final dateTime = DateTime.parse(timestamp);
      final now = DateTime.now();
      final difference = now.difference(dateTime);

      if (difference.inDays > 0) {
        return '${difference.inDays}d ago';
      } else if (difference.inHours > 0) {
        return '${difference.inHours}h ago';
      } else if (difference.inMinutes > 0) {
        return '${difference.inMinutes}m ago';
      } else {
        return 'Just now';
      }
    } catch (e) {
      return timestamp;
    }
  }
}

// Models
class Post {
  final int id;
  final String caption;
  final String visibility;
  final String createdAt;
  final UserSummary user;
  final List<MediaFile> mediaFiles;
  final int likesCount;
  final int commentsCount;

  Post({
    required this.id,
    required this.caption,
    required this.visibility,
    required this.createdAt,
    required this.user,
    required this.mediaFiles,
    required this.likesCount,
    required this.commentsCount,
  });

  factory Post.fromJson(Map<String, dynamic> json) {
    return Post(
      id: json['id'],
      caption: json['caption'] ?? '',
      visibility: json['visibility'],
      createdAt: json['createdAt'],
      user: UserSummary.fromJson(json['user']),
      mediaFiles: (json['mediaFiles'] as List)
          .map((m) => MediaFile.fromJson(m))
          .toList(),
      likesCount: json['likesCount'] ?? 0,
      commentsCount: json['commentsCount'] ?? 0,
    );
  }
}

class UserSummary {
  final int id;
  final String username;
  final String? fullName;
  final String? avatarUrl;

  UserSummary({
    required this.id,
    required this.username,
    this.fullName,
    this.avatarUrl,
  });

  factory UserSummary.fromJson(Map<String, dynamic> json) {
    return UserSummary(
      id: json['id'],
      username: json['username'],
      fullName: json['fullName'],
      avatarUrl: json['avatarUrl'],
    );
  }
}

class MediaFile {
  final String fileUrl;
  final String fileType;
  final int orderIndex;

  MediaFile({
    required this.fileUrl,
    required this.fileType,
    required this.orderIndex,
  });

  factory MediaFile.fromJson(Map<String, dynamic> json) {
    return MediaFile(
      fileUrl: json['fileUrl'],
      fileType: json['fileType'],
      orderIndex: json['orderIndex'],
    );
  }
}
