import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/user_service.dart';
import '../services/post_service.dart';
import '../models/user_models.dart';
import '../models/post_models.dart';

class ProfileScreen extends StatefulWidget {
  final int? userId;

  const ProfileScreen({Key? key, this.userId}) : super(key: key);

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late Future<UserProfile> _userProfileFuture;
  late Future<UserStats> _userStatsFuture;
  late Future<FeedResponse> _userPostsFuture;
  late int _effectiveUserId;

  @override
  void initState() {
    super.initState();
    _initUserIdAndLoad();
  }

  Future<void> _initUserIdAndLoad() async {
    int? id = widget.userId;
    if (id == null) {
      final prefs = await SharedPreferences.getInstance();
      id = prefs.getInt('user_id');
    }

    if (id == null) {
      // No user id available — show error by setting futures to throw
      _userProfileFuture = Future<UserProfile>.error('No user id available');
      _userStatsFuture = Future<UserStats>.error('No user id available');
      _userPostsFuture = Future<FeedResponse>.error('No user id available');
      return;
    }

    _effectiveUserId = id;
    _userProfileFuture = UserService.getUserById(_effectiveUserId);
    _userStatsFuture = UserService.getUserStats(_effectiveUserId);
    _userPostsFuture = PostService.getUserPosts(_effectiveUserId);
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
        backgroundColor: Colors.purple,
        foregroundColor: Colors.white,
      ),
      body: FutureBuilder<List<dynamic>>(
        future: Future.wait([
          _userProfileFuture,
          _userStatsFuture,
          _userPostsFuture,
        ]),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text('Error: ${snapshot.error}'),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      _initUserIdAndLoad();
                    },
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          } else if (snapshot.hasData) {
            final user = snapshot.data![0] as UserProfile;
            final stats = snapshot.data![1] as UserStats;
            final postsData = snapshot.data![2] as FeedResponse;
            
            return ListView(
              children: [
                _buildProfileHeader(user, stats, postsData.totalItems),
                _buildActionButtons(stats),
                const Divider(height: 1),
                _buildPostGrid(postsData.posts),
              ],
            );
          } else {
            return const Center(child: Text('User not found.'));
          }
        },
      ),
    );
  }

  Widget _buildProfileHeader(UserProfile user, UserStats stats, int postsCount) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 40,
                backgroundColor: Colors.grey[300],
                backgroundImage: user.avatarUrl != null
                    ? NetworkImage(user.avatarUrl!)
                    : null,
                child: user.avatarUrl == null
                    ? Text(
                        user.username[0].toUpperCase(),
                        style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
                      )
                    : null,
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _buildStatItem('Posts', postsCount),
                    _buildStatItem('Followers', stats.followersCount),
                    _buildStatItem('Following', stats.followingCount),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Align(
            alignment: Alignment.centerLeft,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      user.username,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    if (user.isVerified) ...[
                      const SizedBox(width: 4),
                      const Icon(
                        Icons.verified,
                        size: 16,
                        color: Colors.blue,
                      ),
                    ],
                  ],
                ),
                if (user.fullName != null) ...[
                  const SizedBox(height: 2),
                  Text(
                    user.fullName!,
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.grey[700],
                    ),
                  ),
                ],
                if (user.bio != null) ...[
                  const SizedBox(height: 4),
                  Text(
                    user.bio!,
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.grey[800],
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatItem(String label, int count) {
    return Column(
      children: [
        Text(
          count.toString(),
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        Text(label, style: TextStyle(color: Colors.grey[700])),
      ],
    );
  }

  Widget _buildActionButtons(UserStats stats) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          Expanded(
            child: ElevatedButton(
              onPressed: () async {
                if (stats.isFollowing) {
                  await UserService.unfollowUser(_effectiveUserId);
                } else {
                  await UserService.followUser(_effectiveUserId);
                }
                // Refresh data
                setState(() {
                  _userStatsFuture = UserService.getUserStats(_effectiveUserId);
                });
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: stats.isFollowing ? Colors.grey[300] : Colors.purple,
                foregroundColor: stats.isFollowing ? Colors.black : Colors.white,
              ),
              child: Text(stats.isFollowing ? 'Unfollow' : 'Follow'),
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: ElevatedButton(
              onPressed: () {
                // Navigate to chat
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Chat feature coming soon!')),
                );
              },
              child: const Text('Message'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPostGrid(List<PostDto> posts) {
    if (posts.isEmpty) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(32.0),
          child: Column(
            children: [
              Icon(Icons.photo_library_outlined, size: 64, color: Colors.grey),
              SizedBox(height: 16),
              Text(
                'No posts yet.',
                style: TextStyle(fontSize: 18, color: Colors.grey),
              ),
            ],
          ),
        ),
      );
    }
    
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 2,
        mainAxisSpacing: 2,
      ),
      itemCount: posts.length,
      itemBuilder: (context, index) {
        final post = posts[index];
        if (post.mediaFiles.isEmpty) {
          return Container(
            color: Colors.grey[300],
            child: const Icon(Icons.image, color: Colors.grey),
          );
        }
        return Image.network(
          post.mediaFiles.first.fileUrl,
          fit: BoxFit.cover,
          errorBuilder: (context, error, stackTrace) {
            return Container(
              color: Colors.grey[300],
              child: const Icon(Icons.broken_image, color: Colors.grey),
            );
          },
        );
      },
    );
  }
}
