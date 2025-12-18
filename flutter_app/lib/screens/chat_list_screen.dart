import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/user_service.dart';
import '../models/user_models.dart';
import '../models/chat_models.dart';
import 'chat_screen.dart';

class ChatListScreen extends StatefulWidget {
  const ChatListScreen({super.key});

  @override
  State<ChatListScreen> createState() => _ChatListScreenState();
}

class _ChatListScreenState extends State<ChatListScreen> {
  final TextEditingController _searchController = TextEditingController();
  
  List<UserProfile> _allChatableUsers = [];
  List<UserProfile> _filteredUsers = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadChatableUsers();
    _searchController.addListener(_onSearchChanged);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      if (query.isEmpty) {
        _filteredUsers = _allChatableUsers;
      } else {
        _filteredUsers = _allChatableUsers.where((user) {
          final username = user.username.toLowerCase();
          final fullName = (user.fullName ?? '').toLowerCase();
          return username.contains(query) || fullName.contains(query);
        }).toList();
      }
    });
  }

  Future<void> _loadChatableUsers() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final prefs = await SharedPreferences.getInstance();
      final userId = prefs.getInt('user_id') ?? 0;

      final results = await Future.wait([
        UserService.getFollowers(userId),
        UserService.getFollowing(userId),
      ]);

      final followers = results[0];
      final following = results[1];

      // Use a map to deduplicate by ID
      final userMap = <int, UserProfile>{};
      for (var user in followers) {
        userMap[user.id] = user;
      }
      for (var user in following) {
        userMap[user.id] = user;
      }

      final uniqueUsers = userMap.values.toList();
      // Sort alphabetically by username
      uniqueUsers.sort((a, b) => a.username.compareTo(b.username));

      setState(() {
        _allChatableUsers = uniqueUsers;
        _filteredUsers = uniqueUsers;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          'Chat',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadChatableUsers,
          ),
        ],
      ),
      body: Column(
        children: [
          // Search Bar
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Search friends...',
                prefixIcon: const Icon(Icons.search),
                filled: true,
                fillColor: Colors.grey[100],
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide.none,
                ),
                contentPadding: const EdgeInsets.symmetric(vertical: 0),
              ),
            ),
          ),
          Expanded(child: _buildBody()),
        ],
      ),
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
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 16),
            Text('Error: $_error'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _loadChatableUsers,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    if (_filteredUsers.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.people_outline, size: 64, color: Colors.grey[300]),
            const SizedBox(height: 16),
            Text(
              _searchController.text.isEmpty
                  ? 'No friends found yet.\nFollow someone to start chatting!'
                  : 'No friends matching your search.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey[600]),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadChatableUsers,
      child: ListView.separated(
        itemCount: _filteredUsers.length,
        separatorBuilder: (context, index) => const Divider(height: 1, indent: 80),
        itemBuilder: (context, index) {
          final user = _filteredUsers[index];
          return _buildUserTile(user);
        },
      ),
    );
  }

  Widget _buildUserTile(UserProfile user) {
    return ListTile(
      onTap: () {
        // Convert UserProfile to UserSummary
        final userSummary = UserSummary(
          id: user.id,
          username: user.username,
          fullName: user.fullName,
          avatarUrl: user.avatarUrl,
        );

        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => ChatScreen(user: userSummary),
          ),
        );
      },
      leading: CircleAvatar(
        radius: 30,
        backgroundImage: user.avatarUrl != null
            ? NetworkImage(user.avatarUrl!)
            : null,
        backgroundColor: Colors.grey[200],
        child: user.avatarUrl == null
            ? Text(
                user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Colors.black54,
                ),
              )
            : null,
      ),
      title: Row(
        children: [
          Expanded(
            child: Text(
              user.fullName ?? user.username,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
          if (user.isVerified) ...[
            const SizedBox(width: 4),
            const Icon(Icons.verified, color: Colors.blue, size: 16),
          ],
        ],
      ),
      subtitle: Text(
        '@${user.username}',
        style: TextStyle(color: Colors.grey[600]),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
    );
  }
}
