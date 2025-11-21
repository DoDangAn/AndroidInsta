import 'package:flutter/material.dart';
import '../services/friend_service.dart';
import '../models/friend_models.dart';
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
  final FriendService _friendService = FriendService();
  
  bool _isLoading = true;
  bool _isLoadingAction = false;
  String? _error;
  
  // User info
  Map<String, dynamic>? _userInfo;
  
  // Friendship status
  FriendshipStatus? _friendshipStatus;
  
  @override
  void initState() {
    super.initState();
    _loadUserProfile();
  }

  Future<void> _loadUserProfile() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      // Load user info
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('access_token');
      
      if (token == null) throw Exception('Not authenticated');
      
      final response = await http.get(
        Uri.parse('${ApiConfig.baseUrl}/api/users/${widget.userId}'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );
      
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        if (data['success'] == true) {
          _userInfo = data['data'];
        }
      }
      
      // Load friendship status
      final status = await _friendService.getFriendshipStatus(widget.userId);
      
      setState(() {
        _friendshipStatus = status;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _sendFriendRequest() async {
    setState(() {
      _isLoadingAction = true;
    });

    try {
      await _friendService.sendFriendRequest(widget.userId);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request sent')),
      );
      await _loadUserProfile();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() {
        _isLoadingAction = false;
      });
    }
  }

  Future<void> _cancelFriendRequest() async {
    if (_friendshipStatus?.friendRequestId == null) return;
    
    setState(() {
      _isLoadingAction = true;
    });

    try {
      await _friendService.cancelFriendRequest(_friendshipStatus!.friendRequestId!);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request cancelled')),
      );
      await _loadUserProfile();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() {
        _isLoadingAction = false;
      });
    }
  }

  Future<void> _acceptFriendRequest() async {
    if (_friendshipStatus?.friendRequestId == null) return;
    
    setState(() {
      _isLoadingAction = true;
    });

    try {
      await _friendService.acceptFriendRequest(_friendshipStatus!.friendRequestId!);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request accepted')),
      );
      await _loadUserProfile();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() {
        _isLoadingAction = false;
      });
    }
  }

  Future<void> _unfriend() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Unfriend'),
        content: Text('Are you sure you want to unfriend ${_userInfo?['fullName'] ?? _userInfo?['username']}?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Unfriend', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      setState(() {
        _isLoadingAction = true;
      });

      try {
        await _friendService.unfriend(widget.userId);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Unfriended successfully')),
        );
        await _loadUserProfile();
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      } finally {
        setState(() {
          _isLoadingAction = false;
        });
      }
    }
  }

  void _openChat() {
    if (_userInfo == null) return;
    
    final userSummary = UserSummary(
      id: widget.userId,
      username: _userInfo!['username'] ?? '',
      fullName: _userInfo!['fullName'],
      avatarUrl: _userInfo!['avatarUrl'],
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
          widget.username ?? 'Profile',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_error != null || _userInfo == null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text('Error: $_error'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _loadUserProfile,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    return SingleChildScrollView(
      child: Column(
        children: [
          const SizedBox(height: 16),
          // Profile Picture
          CircleAvatar(
            radius: 50,
            backgroundImage: _userInfo!['avatarUrl'] != null
                ? NetworkImage(_userInfo!['avatarUrl'])
                : null,
            backgroundColor: Colors.grey[300],
            child: _userInfo!['avatarUrl'] == null
                ? Text(
                    (_userInfo!['username'] ?? 'U')[0].toUpperCase(),
                    style: const TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
                  )
                : null,
          ),
          const SizedBox(height: 16),
          
          // Username
          Text(
            _userInfo!['fullName'] ?? _userInfo!['username'] ?? '',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '@${_userInfo!['username'] ?? ''}',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
            ),
          ),
          
          // Bio
          if (_userInfo!['bio'] != null && _userInfo!['bio'].toString().isNotEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
              child: Text(
                _userInfo!['bio'],
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 14),
              ),
            ),
          
          const SizedBox(height: 24),
          
          // Action Buttons
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: _buildActionButtons(),
          ),
          
          const SizedBox(height: 24),
          const Divider(),
          
          // Stats (placeholder - can be enhanced)
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _buildStatColumn('Posts', '0'),
                _buildStatColumn('Friends', '0'),
                _buildStatColumn('Followers', '0'),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButtons() {
    if (_friendshipStatus == null) {
      return const SizedBox.shrink();
    }

    return Row(
      children: [
        // Friend button
        Expanded(
          flex: 2,
          child: _buildFriendButton(),
        ),
        const SizedBox(width: 8),
        
        // Message button (only show if friends)
        if (_friendshipStatus!.isFriend)
          Expanded(
            child: ElevatedButton.icon(
              onPressed: _isLoadingAction ? null : _openChat,
              icon: const Icon(Icons.message, size: 18),
              label: const Text('Message'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.grey[200],
                foregroundColor: Colors.black,
                elevation: 0,
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildFriendButton() {
    if (_friendshipStatus!.isFriend) {
      // Already friends
      return ElevatedButton.icon(
        onPressed: _isLoadingAction ? null : _unfriend,
        icon: const Icon(Icons.check, size: 18),
        label: const Text('Friends'),
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.grey[200],
          foregroundColor: Colors.black,
          elevation: 0,
          padding: const EdgeInsets.symmetric(vertical: 12),
        ),
      );
    } else if (_friendshipStatus!.hasPendingRequest) {
      if (_friendshipStatus!.pendingRequestSentByMe) {
        // Request sent by me - show cancel button
        return ElevatedButton.icon(
          onPressed: _isLoadingAction ? null : _cancelFriendRequest,
          icon: const Icon(Icons.person_remove, size: 18),
          label: const Text('Cancel Request'),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.grey[300],
            foregroundColor: Colors.black,
            elevation: 0,
            padding: const EdgeInsets.symmetric(vertical: 12),
          ),
        );
      } else {
        // Request received - show accept button
        return ElevatedButton.icon(
          onPressed: _isLoadingAction ? null : _acceptFriendRequest,
          icon: const Icon(Icons.person_add, size: 18),
          label: const Text('Accept Request'),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.blue,
            foregroundColor: Colors.white,
            elevation: 0,
            padding: const EdgeInsets.symmetric(vertical: 12),
          ),
        );
      }
    } else {
      // No relationship - show add friend button
      return ElevatedButton.icon(
        onPressed: _isLoadingAction ? null : _sendFriendRequest,
        icon: const Icon(Icons.person_add, size: 18),
        label: const Text('Add Friend'),
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.blue,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(vertical: 12),
        ),
      );
    }
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
