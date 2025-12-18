import 'package:flutter/material.dart';
import 'package:timeago/timeago.dart' as timeago;
import '../models/notification_models.dart';
import '../services/notification_service.dart';
import 'profile_screen.dart';

class NotificationScreen extends StatefulWidget {
  const NotificationScreen({super.key});

  @override
  State<NotificationScreen> createState() => _NotificationScreenState();
}

class _NotificationScreenState extends State<NotificationScreen> {
  final NotificationService _notificationService = NotificationService();
  List<NotificationModel> _notifications = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadNotifications();
  }

  Future<void> _loadNotifications() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final notifications = await _notificationService.getNotifications();
      setState(() {
        _notifications = notifications;
        _isLoading = false;
      });
      
      // Mark all as read when opening screen and wait for completion
      await _notificationService.markAllAsRead();
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
      appBar: AppBar(
        title: const Text(
          'Notifications',
          style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold),
        ),
        backgroundColor: Colors.white,
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
              onPressed: _loadNotifications,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    if (_notifications.isEmpty) {
      return const Center(
        child: Text('No notifications yet'),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadNotifications,
      child: ListView.builder(
        itemCount: _notifications.length,
        itemBuilder: (context, index) {
          final notification = _notifications[index];
          return _buildNotificationItem(notification);
        },
      ),
    );
  }

  Widget _buildNotificationItem(NotificationModel notification) {
    return ListTile(
      onTap: () => _handleNotificationTap(notification),
      leading: GestureDetector(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => ProfileScreen(userId: notification.senderId),
            ),
          );
        },
        child: CircleAvatar(
          backgroundImage: notification.senderAvatarUrl != null
              ? NetworkImage(notification.senderAvatarUrl!)
              : null,
          child: notification.senderAvatarUrl == null
              ? Text(notification.senderUsername[0].toUpperCase())
              : null,
        ),
      ),
      title: RichText(
        text: TextSpan(
          style: const TextStyle(color: Colors.black),
          children: [
            TextSpan(
              text: notification.senderUsername,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            TextSpan(text: ' ${notification.message ?? _getDefaultMessage(notification.type)}'),
          ],
        ),
      ),
      subtitle: Text(
        timeago.format(DateTime.parse(notification.createdAt)),
        style: TextStyle(color: Colors.grey[600], fontSize: 12),
      ),
      trailing: notification.type == 'FOLLOW'
          ? OutlinedButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ProfileScreen(userId: notification.senderId),
                  ),
                );
              },
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: const Text('View'),
            )
          : null,
    );
  }

  String _getDefaultMessage(String type) {
    switch (type) {
      case 'FOLLOW':
        return 'started following you.';
      case 'LIKE':
        return 'liked your post.';
      case 'COMMENT':
        return 'commented on your post.';
      case 'NEW_POST':
        return 'posted a new photo.';
      case 'MESSAGE':
        return 'sent you a message.';
      default:
        return 'interacted with you.';
    }
  }

  void _handleNotificationTap(NotificationModel notification) {
    // Navigate based on notification type
    if (notification.type == 'LIKE' || 
        notification.type == 'COMMENT' || 
        notification.type == 'NEW_POST') {
      // Navigate to post detail if entityId exists
      if (notification.entityId != null) {
        Navigator.pushNamed(
          context,
          '/post-detail',
          arguments: notification.entityId,
        );
      } else {
        // Fallback to profile
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => ProfileScreen(userId: notification.senderId),
          ),
        );
      }
    } else if (notification.type == 'MESSAGE') {
      // Navigate to chat screen (implement if needed)
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => ProfileScreen(userId: notification.senderId),
        ),
      );
    } else {
      // Default: Navigate to profile
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => ProfileScreen(userId: notification.senderId),
        ),
      );
    }
  }
}
