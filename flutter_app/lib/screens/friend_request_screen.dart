import 'package:flutter/material.dart';
import '../services/friend_service.dart';
import '../models/friend_models.dart';

class FriendRequestScreen extends StatefulWidget {
  const FriendRequestScreen({Key? key}) : super(key: key);

  @override
  State<FriendRequestScreen> createState() => _FriendRequestScreenState();
}

class _FriendRequestScreenState extends State<FriendRequestScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<FriendRequest> _receivedRequests = [];
  List<FriendRequest> _sentRequests = [];
  bool _isLoadingReceived = true;
  bool _isLoadingSent = true;

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
    });

    try {
      final requests = await FriendService.getReceivedRequests();
      setState(() {
        _receivedRequests = requests
            .map((r) => FriendRequest.fromJson(r))
            .toList();
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading requests: $e')),
      );
    } finally {
      setState(() {
        _isLoadingReceived = false;
      });
    }
  }

  Future<void> _loadSentRequests() async {
    setState(() {
      _isLoadingSent = true;
    });

    try {
      final requests = await FriendService.getSentRequests();
      setState(() {
        _sentRequests = requests
            .map((r) => FriendRequest.fromJson(r))
            .toList();
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading sent requests: $e')),
      );
    } finally {
      setState(() {
        _isLoadingSent = false;
      });
    }
  }

  Future<void> _acceptRequest(int requestId) async {
    try {
      await FriendService.acceptRequest(requestId);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Friend request accepted!')),
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
      await FriendService.rejectRequest(requestId);
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
      await FriendService.cancelRequest(requestId);
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
        bottom: TabBar(
          controller: _tabController,
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
      return const Center(child: Text('No friend requests'));
    }

    return ListView.builder(
      itemCount: _receivedRequests.length,
      itemBuilder: (context, index) {
        final request = _receivedRequests[index];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          child: ListTile(
            leading: CircleAvatar(
              backgroundImage: request.sender.avatarUrl != null
                  ? NetworkImage(request.sender.avatarUrl!)
                  : null,
              child: request.sender.avatarUrl == null
                  ? Text(request.sender.username[0].toUpperCase())
                  : null,
            ),
            title: Row(
              children: [
                Text(request.sender.username),
                if (request.sender.isVerified)
                  const Padding(
                    padding: EdgeInsets.only(left: 4),
                    child: Icon(Icons.verified, size: 16, color: Colors.blue),
                  ),
              ],
            ),
            subtitle: request.sender.fullName != null
                ? Text(request.sender.fullName!)
                : null,
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
            onTap: () {
              Navigator.pushNamed(context, '/user-profile',
                  arguments: request.sender.id);
            },
          ),
        );
      },
    );
  }

  Widget _buildSentTab() {
    if (_isLoadingSent) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_sentRequests.isEmpty) {
      return const Center(child: Text('No sent requests'));
    }

    return ListView.builder(
      itemCount: _sentRequests.length,
      itemBuilder: (context, index) {
        final request = _sentRequests[index];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          child: ListTile(
            leading: CircleAvatar(
              backgroundImage: request.receiver.avatarUrl != null
                  ? NetworkImage(request.receiver.avatarUrl!)
                  : null,
              child: request.receiver.avatarUrl == null
                  ? Text(request.receiver.username[0].toUpperCase())
                  : null,
            ),
            title: Row(
              children: [
                Text(request.receiver.username),
                if (request.receiver.isVerified)
                  const Padding(
                    padding: EdgeInsets.only(left: 4),
                    child: Icon(Icons.verified, size: 16, color: Colors.blue),
                  ),
              ],
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (request.receiver.fullName != null)
                  Text(request.receiver.fullName!),
                Text(
                  'Status: ${request.status}',
                  style: TextStyle(
                    fontSize: 12,
                    color: request.isPending ? Colors.orange : Colors.grey,
                  ),
                ),
              ],
            ),
            trailing: IconButton(
              icon: const Icon(Icons.cancel, color: Colors.red),
              onPressed: () => _cancelRequest(request.id),
            ),
            onTap: () {
              Navigator.pushNamed(context, '/user-profile',
                  arguments: request.receiver.id);
            },
          ),
        );
      },
    );
  }
}
