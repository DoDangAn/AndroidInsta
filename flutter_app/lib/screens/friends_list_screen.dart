import 'package:flutter/material.dart';
import '../services/friend_service.dart';
import '../models/friend_models.dart';
import 'chat_screen.dart';
import '../models/chat_models.dart';

class FriendsListScreen extends StatefulWidget {
  const FriendsListScreen({super.key});

  @override
  State<FriendsListScreen> createState() => _FriendsListScreenState();
}

class _FriendsListScreenState extends State<FriendsListScreen> {
  final FriendService _friendService = FriendService();
  List<Friend> _friends = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadFriends();
  }

  Future<void> _loadFriends() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final friends = await _friendService.getFriends();
      setState(() {
        _friends = friends;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _unfriend(Friend friend) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Unfriend'),
        content: Text('Are you sure you want to unfriend ${friend.fullName ?? friend.username}?'),
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
      try {
        await _friendService.unfriend(friend.userId);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Unfriended successfully')),
        );
        _loadFriends();
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      }
    }
  }

  void _openChat(Friend friend) {
    final userSummary = UserSummary(
      id: friend.userId,
      username: friend.username,
      fullName: friend.fullName,
      avatarUrl: friend.avatarUrl,
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
      appBar: AppBar(
        title: const Text('Friends'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_error != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Error: $_error'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _loadFriends,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    if (_friends.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.people_outline, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'No friends yet',
              style: TextStyle(fontSize: 18, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadFriends,
      child: ListView.builder(
        itemCount: _friends.length,
        itemBuilder: (context, index) {
          final friend = _friends[index];
          return _buildFriendTile(friend);
        },
      ),
    );
  }

  Widget _buildFriendTile(Friend friend) {
    return ListTile(
      leading: CircleAvatar(
        radius: 28,
        backgroundImage: friend.avatarUrl != null
            ? NetworkImage(friend.avatarUrl!)
            : null,
        child: friend.avatarUrl == null
            ? Text(
                friend.username[0].toUpperCase(),
                style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              )
            : null,
      ),
      title: Text(
        friend.fullName ?? friend.username,
        style: const TextStyle(fontWeight: FontWeight.bold),
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('@${friend.username}'),
          if (friend.mutualFriendsCount > 0)
            Text(
              '${friend.mutualFriendsCount} mutual friends',
              style: TextStyle(fontSize: 12, color: Colors.grey[600]),
            ),
        ],
      ),
      trailing: PopupMenuButton<String>(
        onSelected: (value) {
          if (value == 'message') {
            _openChat(friend);
          } else if (value == 'unfriend') {
            _unfriend(friend);
          }
        },
        itemBuilder: (context) => [
          const PopupMenuItem(
            value: 'message',
            child: Row(
              children: [
                Icon(Icons.message, size: 20),
                SizedBox(width: 8),
                Text('Send Message'),
              ],
            ),
          ),
          const PopupMenuItem(
            value: 'unfriend',
            child: Row(
              children: [
                Icon(Icons.person_remove, size: 20, color: Colors.red),
                SizedBox(width: 8),
                Text('Unfriend', style: TextStyle(color: Colors.red)),
              ],
            ),
          ),
        ],
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
    );
  }
}
