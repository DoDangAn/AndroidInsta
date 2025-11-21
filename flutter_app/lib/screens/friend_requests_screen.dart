import 'package:flutter/material.dart';
import '../services/friend_service.dart';
import '../models/friend_models.dart';

class FriendRequestsScreen extends StatefulWidget {
  const FriendRequestsScreen({super.key});

  @override
  State<FriendRequestsScreen> createState() => _FriendRequestsScreenState();
}

class _FriendRequestsScreenState extends State<FriendRequestsScreen> with SingleTickerProviderStateMixin {
  final FriendService _friendService = FriendService();
  late TabController _tabController;
  
  List<FriendRequest> _receivedRequests = [];
  List<FriendRequest> _sentRequests = [];
  bool _isLoadingReceived = true;
  bool _isLoadingSent = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadReceivedRequests();
    _loadSentRequests();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadReceivedRequests() async {
    setState(() {
      _isLoadingReceived = true;
      _error = null;
    });

    try {
      final requests = await _friendService.getReceivedFriendRequests();
      setState(() {
        _receivedRequests = requests;
        _isLoadingReceived = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoadingReceived = false;
      });
    }
  }

  Future<void> _loadSentRequests() async {
    setState(() {
      _isLoadingSent = true;
      _error = null;
    });

    try {
      final requests = await _friendService.getSentFriendRequests();
      setState(() {
        _sentRequests = requests;
        _isLoadingSent = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoadingSent = false;
      });
    }
  }

  Future<void> _acceptRequest(int requestId) async {
    try {
      await _friendService.acceptFriendRequest(requestId);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request accepted')),
      );
      _loadReceivedRequests();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  Future<void> _rejectRequest(int requestId) async {
    try {
      await _friendService.rejectFriendRequest(requestId);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request rejected')),
      );
      _loadReceivedRequests();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  Future<void> _cancelRequest(int requestId) async {
    try {
      await _friendService.cancelFriendRequest(requestId);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request cancelled')),
      );
      _loadSentRequests();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Friend Requests'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        bottom: TabBar(
          controller: _tabController,
          labelColor: Colors.black,
          indicatorColor: Theme.of(context).primaryColor,
          tabs: const [
            Tab(text: 'Received'),
            Tab(text: 'Sent'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildReceivedTab(),
          _buildSentTab(),
        ],
      ),
    );
  }

  Widget _buildReceivedTab() {
    if (_isLoadingReceived) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_receivedRequests.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.person_add_disabled, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'No friend requests',
              style: TextStyle(fontSize: 18, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadReceivedRequests,
      child: ListView.builder(
        itemCount: _receivedRequests.length,
        itemBuilder: (context, index) {
          final request = _receivedRequests[index];
          return _buildReceivedRequestTile(request);
        },
      ),
    );
  }

  Widget _buildSentTab() {
    if (_isLoadingSent) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_sentRequests.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.send, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'No sent requests',
              style: TextStyle(fontSize: 18, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadSentRequests,
      child: ListView.builder(
        itemCount: _sentRequests.length,
        itemBuilder: (context, index) {
          final request = _sentRequests[index];
          return _buildSentRequestTile(request);
        },
      ),
    );
  }

  Widget _buildReceivedRequestTile(FriendRequest request) {
    return ListTile(
      leading: CircleAvatar(
        radius: 28,
        backgroundImage: request.senderAvatarUrl != null
            ? NetworkImage(request.senderAvatarUrl!)
            : null,
        child: request.senderAvatarUrl == null
            ? Text(
                request.senderUsername[0].toUpperCase(),
                style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              )
            : null,
      ),
      title: Text(
        request.senderFullName ?? request.senderUsername,
        style: const TextStyle(fontWeight: FontWeight.bold),
      ),
      subtitle: Text('@${request.senderUsername}'),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconButton(
            icon: const Icon(Icons.check, color: Colors.green),
            onPressed: () => _acceptRequest(request.id),
          ),
          IconButton(
            icon: const Icon(Icons.close, color: Colors.red),
            onPressed: () => _rejectRequest(request.id),
          ),
        ],
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
    );
  }

  Widget _buildSentRequestTile(FriendRequest request) {
    return ListTile(
      leading: CircleAvatar(
        radius: 28,
        backgroundImage: request.receiverAvatarUrl != null
            ? NetworkImage(request.receiverAvatarUrl!)
            : null,
        child: request.receiverAvatarUrl == null
            ? Text(
                request.receiverUsername[0].toUpperCase(),
                style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              )
            : null,
      ),
      title: Text(
        request.receiverFullName ?? request.receiverUsername,
        style: const TextStyle(fontWeight: FontWeight.bold),
      ),
      subtitle: Text('@${request.receiverUsername}'),
      trailing: TextButton(
        onPressed: () => _cancelRequest(request.id),
        child: const Text('Cancel'),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
    );
  }
}
