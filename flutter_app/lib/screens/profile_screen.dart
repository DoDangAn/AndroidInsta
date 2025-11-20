import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/user_service.dart';
import '../services/post_service.dart';
import '../models/user_models.dart';
import '../models/post_models.dart';
import 'edit_profile_screen.dart';

class ProfileScreen extends StatefulWidget {
  final int? userId;

  const ProfileScreen({Key? key, this.userId}) : super(key: key);

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  int? _effectiveUserId;
  String _username = '';
  String _email = '';
  bool _isLoading = true;
  String? _errorMessage;
  UserProfile? _userProfile;
  UserStats? _userStats;
  List<PostDto> _userPosts = [];

  @override
  void initState() {
    super.initState();
    _initUserIdAndLoad();
  }

  Future<void> _initUserIdAndLoad() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      int? id = widget.userId;
      if (id == null) {
        final prefs = await SharedPreferences.getInstance();
        id = prefs.getInt('user_id');
        _username = prefs.getString('username') ?? '';
        _email = prefs.getString('email') ?? '';
      }

      if (id == null) {
        setState(() {
          _errorMessage = 'No user id available';
          _isLoading = false;
        });
        return;
      }

      _effectiveUserId = id;

      // Load user data
      try {
        // If viewing own profile (no userId passed), use getCurrentProfile
        if (widget.userId == null) {
          _userProfile = await UserService.getCurrentProfile();
        } else {
          _userProfile = await UserService.getUserById(_effectiveUserId!);
        }
      } catch (e) {
        print('Error loading user profile: $e');
        // Fallback: create basic profile from SharedPreferences if viewing own profile
        if (widget.userId == null && _username.isNotEmpty) {
          _userProfile = UserProfile(
            id: _effectiveUserId!,
            username: _username,
            email: _email,
            fullName: null,
            bio: null,
            avatarUrl: null,
            isVerified: false,
            isActive: true,
            createdAt: DateTime.now().toIso8601String(),
            updatedAt: DateTime.now().toIso8601String(),
          );
        }
      }

      try {
        _userStats = await UserService.getUserStats(_effectiveUserId!);
      } catch (e) {
        print('Error loading user stats: $e');
      }

      try {
        final feedResponse = await PostService.getUserPosts(_effectiveUserId!);
        _userPosts = feedResponse.posts;
      } catch (e) {
        print('Error loading user posts: $e');
      }

      setState(() {
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: Text(
          _userProfile?.username ?? _username,
          style: const TextStyle(
            color: Colors.black,
            fontWeight: FontWeight.w600,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.menu, color: Colors.black),
            onPressed: () {},
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline, size: 64, color: Colors.grey),
                      const SizedBox(height: 16),
                      Text(
                        'Error loading profile',
                        style: TextStyle(color: Colors.grey[600], fontSize: 16),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        _errorMessage!,
                        style: TextStyle(color: Colors.grey[500], fontSize: 12),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _initUserIdAndLoad,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : SingleChildScrollView(
                  child: Column(
                    children: [
                      _buildProfileHeader(),
                      const SizedBox(height: 16),
                      _buildBioSection(),
                      const SizedBox(height: 16),
                      _buildActionButtons(),
                      const Divider(height: 1),
                      _buildPostGrid(),
                    ],
                  ),
                ),
    );
  }

  Widget _buildProfileHeader() {
    final username = _userProfile?.username ?? _username;
    final postsCount = _userPosts.length; // Use actual posts count
    final followersCount = _userStats?.followersCount ?? 0;
    final followingCount = _userStats?.followingCount ?? 0;

    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        children: [
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              CircleAvatar(
                radius: 40,
                backgroundImage: _userProfile?.avatarUrl != null
                    ? NetworkImage(_userProfile!.avatarUrl!)
                    : null,
                backgroundColor: Colors.grey[300],
                child: _userProfile?.avatarUrl == null
                    ? Text(
                        username.isNotEmpty ? username[0].toUpperCase() : 'U',
                        style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
                      )
                    : null,
              ),
              const SizedBox(height: 8),
              Text(
                username.isNotEmpty ? username : 'User',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
          const SizedBox(width: 24),
          // Stats
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildStatColumn(postsCount.toString(), 'Posts'),
                _buildStatColumn(followersCount.toString(), 'Followers'),
                _buildStatColumn(followingCount.toString(), 'Following'),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatColumn(String count, String label) {
    return Column(
      children: [
        Text(
          count,
          style: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            color: Colors.grey[700],
          ),
        ),
      ],
    );
  }

  Widget _buildBioSection() {
    final fullName = _userProfile?.fullName;
    final bio = _userProfile?.bio;

    if (fullName == null && bio == null) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (fullName != null)
            Text(
              fullName,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 14,
              ),
            ),
          if (bio != null) ...[
            const SizedBox(height: 4),
            Text(
              bio,
              style: const TextStyle(fontSize: 14),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildActionButtons() {
    // Check if this is the current user's profile
    final isOwnProfile = widget.userId == null || widget.userId == _effectiveUserId;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Row(
        children: [
          Expanded(
            child: OutlinedButton(
              onPressed: isOwnProfile ? _openEditProfile : _toggleFollow,
              style: OutlinedButton.styleFrom(
                side: BorderSide(color: Colors.grey[300]!),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: Text(
                isOwnProfile ? 'Edit Profile' : (_userStats?.isFollowing ?? false) ? 'Unfollow' : 'Follow',
                style: const TextStyle(
                  color: Colors.black,
                  fontWeight: FontWeight.w600,
                  fontSize: 14,
                ),
              ),
            ),
          ),
          const SizedBox(width: 8),
          OutlinedButton(
            onPressed: () {},
            style: OutlinedButton.styleFrom(
              side: BorderSide(color: Colors.grey[300]!),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 16),
            ),
            child: const Icon(Icons.person_add_outlined, color: Colors.black, size: 20),
          ),
        ],
      ),
    );
  }

  Future<void> _openEditProfile() async {
    print('Opening edit profile...');
    print('User profile null? ${_userProfile == null}');
    print('Username: $_username, Email: $_email');
    
    if (_userProfile == null) {
      if (!mounted) return;
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Unable to load profile data. Please try again.'),
          backgroundColor: Colors.red,
        ),
      );
      
      // Try to reload profile
      await _initUserIdAndLoad();
      
      if (_userProfile == null) {
        if (!mounted) return;
        print('Cannot create profile - missing data');
        return;
      }
    }

    final updatedProfile = await Navigator.push<UserProfile>(
      context,
      MaterialPageRoute(
        builder: (context) => EditProfileScreen(userProfile: _userProfile!),
      ),
    );

    // Refresh profile if it was updated
    if (updatedProfile != null) {
      setState(() {
        _userProfile = updatedProfile;
      });
    }
  }

  Future<void> _toggleFollow() async {
    if (_effectiveUserId == null || _userStats == null) return;

    try {
      bool success;
      if (_userStats!.isFollowing) {
        success = await UserService.unfollowUser(_effectiveUserId!);
      } else {
        success = await UserService.followUser(_effectiveUserId!);
      }

      if (success) {
        // Refresh stats
        final newStats = await UserService.getUserStats(_effectiveUserId!);
        setState(() {
          _userStats = newStats;
        });
      }
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Widget _buildPostGrid() {
    if (_userPosts.isEmpty) {
      return Padding(
        padding: const EdgeInsets.all(32.0),
        child: Center(
          child: Column(
            children: [
              Icon(Icons.photo_library_outlined, size: 64, color: Colors.grey[400]),
              const SizedBox(height: 16),
              Text(
                'No Posts Yet',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: Colors.grey[700],
                ),
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
      itemCount: _userPosts.length,
      itemBuilder: (context, index) {
        final post = _userPosts[index];
        return Container(
          color: Colors.grey[300],
          child: post.mediaFiles.isNotEmpty
              ? Image.network(
                  post.mediaFiles[0].fileUrl,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) => const Center(
                    child: Icon(Icons.image_not_supported, color: Colors.grey),
                  ),
                )
              : const Center(
                  child: Icon(Icons.image, size: 32, color: Colors.grey),
                ),
        );
      },
    );
  }
}
