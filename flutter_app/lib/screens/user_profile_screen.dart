import 'package:flutter/material.dart';
import '../services/user_service.dart';
import '../models/user_models.dart';
import '../models/chat_models.dart';
import 'chat_screen.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';

class UserProfileScreen extends StatefulWidget {
  final int userId;
  final String? username;

  const UserProfileScreen({
    super.key,
    required this.userId,
    this.username,
  });

  @override
  State<UserProfileScreen> createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends State<UserProfileScreen> {
  bool _isLoading = true;
  bool _isLoadingAction = false;
  String? _error;
  
  // User info
  UserProfile? _userProfile;
  
  // Follow status
  bool _isFollowing = false;
  int _followersCount = 0;
  int _followingCount = 0;
  int _postsCount = 0;
  
  @override
  void initState() {
    super.initState();
    print('=== UserProfileScreen initState ===');
    print('User ID: ${widget.userId}');
    print('Username: ${widget.username}');
    _loadUserProfile();
  }

  Future<void> _loadUserProfile() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      // Load user profile
      final profile = await UserService.getUserById(widget.userId);
      
      // Load follow status and stats
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('access_token');
      
      bool isFollowing = false;
      int followersCount = 0;
      int followingCount = 0;
      int postsCount = 0;
      
      if (token != null) {
        // Check if following
        try {
          final followResponse = await http.get(
            Uri.parse('${ApiConfig.baseUrl}/api/users/${widget.userId}/follow-status'),
            headers: {
              'Authorization': 'Bearer $token',
              'Content-Type': 'application/json',
            },
          );
          
          print('Follow status response: ${followResponse.statusCode}');
          print('Follow status body: ${followResponse.body}');
          
          if (followResponse.statusCode == 200) {
            final data = json.decode(followResponse.body);
            isFollowing = data['isFollowing'] ?? false;
            print('Is following: $isFollowing');
          }
        } catch (e) {
          print('Error loading follow status: $e');
        }
        
        // Load stats
        try {
          final statsResponse = await http.get(
            Uri.parse('${ApiConfig.baseUrl}/api/users/${widget.userId}/stats'),
            headers: {
              'Authorization': 'Bearer $token',
              'Content-Type': 'application/json',
            },
          );
          
          print('Stats response status: ${statsResponse.statusCode}');
          print('Stats response body type: ${statsResponse.body.runtimeType}');
          print('Stats response body length: ${statsResponse.body.length}');
          print('Stats response body: "${statsResponse.body}"');
          
          if (statsResponse.statusCode == 200) {
            final responseBody = statsResponse.body;
            
            if (responseBody.isEmpty) {
              print('ERROR: Stats response body is empty!');
            } else {
              try {
                print('Attempting to decode JSON...');
                final decoded = json.decode(responseBody);
                print('Decoded type: ${decoded.runtimeType}');
                print('Decoded value: $decoded');
                
                if (decoded == null) {
                  print('ERROR: json.decode returned null!');
                } else if (decoded is Map<String, dynamic>) {
                  followersCount = (decoded['followersCount'] as num?)?.toInt() ?? 0;
                  followingCount = (decoded['followingCount'] as num?)?.toInt() ?? 0;
                  postsCount = (decoded['postsCount'] as num?)?.toInt() ?? 0;
                  print('Stats parsed - Followers: $followersCount, Following: $followingCount, Posts: $postsCount');
                } else {
                  print('ERROR: Decoded is not a Map, it is: ${decoded.runtimeType}');
                }
              } catch (jsonError) {
                print('ERROR decoding JSON: $jsonError');
                print('JSON Error type: ${jsonError.runtimeType}');
              }
            }
          } else {
            print('ERROR: Stats API returned status ${statsResponse.statusCode}');
          }
        } catch (e, stackTrace) {
          print('ERROR loading stats: $e');
          print('Stack trace: $stackTrace');
        }
      }
      
      setState(() {
        _userProfile = profile;
        _isFollowing = isFollowing;
        _followersCount = followersCount;
        _followingCount = followingCount;
        _postsCount = postsCount;
        _isLoading = false;
      });
      
      print('Profile loaded - isFollowing: $_isFollowing, followers: $_followersCount');
    } catch (e) {
      print('Error loading user profile: $e');
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _toggleFollow() async {
    print('=== _toggleFollow CALLED ===');
    print('Current _isFollowing: $_isFollowing');
    print('Current _isLoadingAction: $_isLoadingAction');
    print('User ID: ${widget.userId}');
    
    setState(() {
      _isLoadingAction = true;
    });
    print('Set _isLoadingAction = true');

    try {
      print('Toggle follow for user ${widget.userId}, current status: $_isFollowing');
      
      if (_isFollowing) {
        // Unfollow
        print('=== UNFOLLOW PATH ===');
        print('Attempting to unfollow user ${widget.userId}');
        final success = await UserService.unfollowUser(widget.userId);
        print('Unfollow API returned success: $success');
        
        if (success) {
          print('Unfollow successful');
          // Reload profile to get updated data
          await _loadUserProfile();
        } else {
          print('Unfollow returned false, throwing exception');
          throw Exception('Unfollow failed');
        }
      } else {
        // Follow
        print('=== FOLLOW PATH ===');
        print('Attempting to follow user ${widget.userId}');
        final success = await UserService.followUser(widget.userId);
        print('Follow API returned success: $success');
        
        if (success) {
          print('Follow successful');
          // Reload profile to get updated data
          await _loadUserProfile();
        } else {
          print('Follow returned false, throwing exception');
          throw Exception('Follow failed');
        }
      }
    } catch (e, stackTrace) {
      print('=== ERROR in _toggleFollow ===');
      print('Error: $e');
      print('Stack trace: $stackTrace');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      print('=== _toggleFollow FINALLY ===');
      setState(() {
        _isLoadingAction = false;
      });
      print('Set _isLoadingAction = false');
      print('Final _isFollowing: $_isFollowing');
      print('=== _toggleFollow COMPLETED ===');
    }
  }

  void _openChat() {
    if (_userProfile == null) return;
    
    final userSummary = UserSummary(
      id: widget.userId,
      username: _userProfile!.username,
      fullName: _userProfile!.fullName,
      avatarUrl: _userProfile!.avatarUrl,
    );

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ChatScreen(user: userSummary),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        title: Text(
          _userProfile?.username ?? widget.username ?? 'Profile',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.more_vert),
            onPressed: () {
              // Show options menu
            },
          ),
        ],
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_error != null || _userProfile == null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text('Lỗi: $_error'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _loadUserProfile,
              child: const Text('Thử lại'),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadUserProfile,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        child: Column(
          children: [
            const SizedBox(height: 16),
            // Profile Picture
            CircleAvatar(
              radius: 50,
              backgroundImage: _userProfile!.avatarUrl != null
                  ? NetworkImage(_userProfile!.avatarUrl!)
                  : null,
              backgroundColor: Colors.grey[300],
              child: _userProfile!.avatarUrl == null
                  ? Text(
                      _userProfile!.username[0].toUpperCase(),
                      style: const TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
                    )
                  : null,
            ),
            const SizedBox(height: 16),
            
            // Username
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  _userProfile!.username,
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                if (_userProfile!.isVerified) ...[
                  const SizedBox(width: 4),
                  const Icon(Icons.verified, color: Colors.blue, size: 24),
                ],
              ],
            ),
            
            // Full name
            if (_userProfile!.fullName != null) ...[
              const SizedBox(height: 4),
              Text(
                _userProfile!.fullName!,
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.grey[600],
                ),
              ),
            ],
            
            // Bio
            if (_userProfile!.bio != null && _userProfile!.bio!.isNotEmpty) ...[
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                child: Text(
                  _userProfile!.bio!,
                  textAlign: TextAlign.center,
                  style: const TextStyle(fontSize: 14),
                ),
              ),
            ],
            
            const SizedBox(height: 24),
            
            // Stats
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _buildStatColumn('Posts', _postsCount.toString()),
                  _buildStatColumn('Followers', _followersCount.toString()),
                  _buildStatColumn('Following', _followingCount.toString()),
                ],
              ),
            ),
            
            const SizedBox(height: 24),
            
            // Action Buttons
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                children: [
                  // Follow button
                  Expanded(
                    flex: 2,
                    child: ElevatedButton(
                      onPressed: _isLoadingAction ? null : () {
                        print('=== FOLLOW BUTTON PRESSED ===');
                        print('_isLoadingAction: $_isLoadingAction');
                        print('_isFollowing: $_isFollowing');
                        _toggleFollow();
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: _isFollowing ? Colors.grey[300] : Colors.blue,
                        foregroundColor: _isFollowing ? Colors.black : Colors.white,
                        elevation: 0,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      child: _isLoadingAction
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                              ),
                            )
                          : Text(
                              _isFollowing ? 'Following' : 'Follow',
                              style: const TextStyle(fontWeight: FontWeight.bold),
                            ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  
                  // Message button
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _openChat,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.grey[200],
                        foregroundColor: Colors.black,
                        elevation: 0,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      child: const Text(
                        'Message',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            
            const SizedBox(height: 24),
            const Divider(),
            
            // Posts grid (placeholder)
            Padding(
              padding: const EdgeInsets.all(16),
              child: Center(
                child: Text(
                  'Posts will appear here',
                  style: TextStyle(color: Colors.grey[600]),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatColumn(String label, String value) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            color: Colors.grey[600],
          ),
        ),
      ],
    );
  }
}
