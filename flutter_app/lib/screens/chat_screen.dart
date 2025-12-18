import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import '../models/chat_models.dart';
import '../services/chat_service.dart';

class ChatScreen extends StatefulWidget {
  final UserSummary user;

  const ChatScreen({super.key, required this.user});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final ChatService _chatService = ChatService();
  
  List<ChatMessage> _messages = [];
  bool _isLoading = true;
  String? _error;
  int? _currentUserId;
  StompClient? _stompClient;
  bool _isTyping = false;
  bool _isOtherUserTyping = false;
  bool _isConnected = false;

  @override
  void initState() {
    super.initState();
    // Ensure current user id is loaded before fetching messages or connecting
    _loadCurrentUser().then((_) {
      _loadMessages();
      _connectWebSocket();
    });
    _messageController.addListener(_onTyping);
  }

  void _onTyping() {
    if (_messageController.text.isNotEmpty && !_isTyping) {
      _sendTypingIndicator(true);
    } else if (_messageController.text.isEmpty && _isTyping) {
      _sendTypingIndicator(false);
    }
  }

  void _sendTypingIndicator(bool isTyping) {
    setState(() {
      _isTyping = isTyping;
    });

    if (_stompClient != null && _stompClient!.connected) {
      _stompClient!.send(
        destination: '/app/typing',
        body: json.encode({
          'receiverId': widget.user.id,
          'isTyping': isTyping,
          'senderId': _currentUserId,
        }),
      );
    }
  }

  Future<void> _loadCurrentUser() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _currentUserId = prefs.getInt('user_id');
    });
  }

  Future<void> _loadMessages() async {
    try {
      print('📥 Loading messages for user ${widget.user.id}...');
      setState(() {
        _isLoading = true;
        _error = null;
      });

      final history = await _chatService.getChatHistory(widget.user.id);
      print('✅ Loaded ${history.messages.length} messages from history');
      
      if (mounted) {
        setState(() {
          // Keep optimistic messages (id == 0)
          final optimisticMessages = _messages.where((m) => m.id == 0).toList();
          final historyMessages = history.messages.reversed.toList();
          
          _messages = [...historyMessages, ...optimisticMessages];
          _isLoading = false;
        });
        
        // Mark messages as read
        _markAsRead();
        
        // Scroll to bottom after loading
        WidgetsBinding.instance.addPostFrameCallback((_) {
          _scrollToBottom();
        });
      }
    } catch (e) {
      print('❌ Error loading messages: $e');
      if (mounted) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  void _connectWebSocket() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('access_token');
    
    if (token == null) {
      print('❌ No access token found, skipping WebSocket connection');
      return;
    }

    // Use 10.0.2.2 for Android emulator, localhost for iOS simulator
    const wsUrl = 'ws://10.0.2.2:8081/ws/websocket';
    print('🔌 Connecting to WebSocket at $wsUrl...');

    _stompClient = StompClient(
      config: StompConfig(
        url: wsUrl,
        onConnect: (frame) {
          print('✅ Connected to WebSocket');
          if (mounted) {
            setState(() {
              _isConnected = true;
            });
          }
          
          // Subscribe to personal message queue
          _stompClient?.subscribe(
            destination: '/user/queue/messages',
            callback: (frame) {
              print('📩 Received message via WebSocket: ${frame.body}');
              if (frame.body != null) {
                try {
                  final data = json.decode(frame.body!);
                  final message = ChatMessage.fromJson(data);
                  
                  // Avoid duplicates by checking ID or content/timestamp
                  bool isDuplicate = _messages.any((m) => 
                      (m.id == message.id && message.id != 0) || 
                      (m.content == message.content && m.sender.id == message.sender.id && 
                       m.id == 0)); // Match optimistic messages
                      
                  if (!isDuplicate && (message.sender.id == widget.user.id || 
                      message.receiver.id == widget.user.id ||
                      message.sender.id == _currentUserId)) {
                    if (mounted) {
                      setState(() {
                        // Replace optimistic message if it exists
                        int optimisticIndex = _messages.indexWhere((m) => 
                          m.id == 0 && m.content == message.content && m.sender.id == message.sender.id);
                        
                        if (optimisticIndex != -1) {
                          _messages[optimisticIndex] = message;
                        } else {
                          _messages.add(message);
                        }
                      });
                      _scrollToBottom();
                      
                      if (message.sender.id == widget.user.id) {
                        _markAsRead();
                      }
                    }
                  }
                } catch (e) {
                  print('❌ Error parsing WebSocket message: $e');
                }
              }
            },
          );
          
          // Subscribe to typing indicators
          _stompClient?.subscribe(
            destination: '/user/queue/typing',
            callback: (frame) {
              if (frame.body != null && mounted) {
                try {
                  final data = json.decode(frame.body!);
                  final senderId = data['senderId'];
                  final isTyping = data['isTyping'] ?? false;
                  
                  // Only show typing if it's from the current chat partner
                  if (senderId == widget.user.id) {
                    setState(() {
                      _isOtherUserTyping = isTyping;
                    });
                    print('${widget.user.username} is ${isTyping ? "typing" : "not typing"}');
                  }
                } catch (e) {
                  print('❌ Error parsing typing indicator: $e');
                }
              }
            },
          );
        },
        onDisconnect: (frame) {
          print('❌ Disconnected from WebSocket');
          if (mounted) {
            setState(() {
              _isConnected = false;
            });
          }
        },
        onWebSocketError: (dynamic error) {
          print('⚠️ WebSocket Error: $error');
          if (mounted) {
            setState(() {
              _isConnected = false;
            });
          }
        },
        stompConnectHeaders: {'Authorization': 'Bearer $token'},
        webSocketConnectHeaders: {'Authorization': 'Bearer $token'},
        // Auto-reconnect on disconnect
        reconnectDelay: const Duration(seconds: 5),
      ),
    );

    _stompClient?.activate();
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  Future<void> _markAsRead() async {
    try {
      await _chatService.markAsRead(widget.user.id);
    } catch (e) {
      print('⚠️ Error marking as read: $e');
    }
  }

  Future<void> _sendMessage() async {
    final content = _messageController.text.trim();
    if (content.isEmpty) return;

    // Clear input immediately
    _messageController.clear();

    // Optimistically add message to UI
    final optimisticMessage = ChatMessage(
      id: 0,
      content: content,
      messageType: 'TEXT',
      sender: UserSummary(
        id: _currentUserId ?? 0,
        username: 'Me',
      ),
      receiver: widget.user,
      isRead: false,
      createdAt: DateTime.now().toIso8601String(),
    );

    setState(() {
      _messages.add(optimisticMessage);
    });
    _scrollToBottom();


    try {
      // Always use REST API for sending (more reliable than WebSocket STOMP)
      // WebSocket is only used for receiving real-time messages
      print('📤 Sending message via REST API...');
      final message = await _chatService.sendMessage(widget.user.id, content);
      setState(() {
        // Replace optimistic message with real message from server
        int index = _messages.indexOf(optimisticMessage);
        if (index != -1) {
          _messages[index] = message;
        }
      });
      _scrollToBottom();
    } catch (e) {
      print('❌ Error sending message: $e');
      if (mounted) {
        // remove optimistic message on error
        setState(() {
          _messages.remove(optimisticMessage);
        });

        String errorMessage = 'Không thể gửi tin nhắn';
        if (e.toString().contains('Exception:')) {
          errorMessage = e.toString().replaceFirst('Exception: ', '');
        } else {
          errorMessage = e.toString();
        }
        
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(errorMessage),
            backgroundColor: Colors.red,
            duration: const Duration(seconds: 3),
          ),
        );
        // Restore text if failed
        _messageController.text = content;
      }
    }
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    _stompClient?.deactivate();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            CircleAvatar(
              radius: 18,
              backgroundImage: widget.user.avatarUrl != null
                  ? NetworkImage(widget.user.avatarUrl!)
                  : null,
              child: widget.user.avatarUrl == null
                  ? Text(
                      widget.user.username.isNotEmpty ? widget.user.username[0].toUpperCase() : '?',
                      style: const TextStyle(fontSize: 16),
                    )
                  : null,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    widget.user.fullName ?? widget.user.username,
                    style: const TextStyle(fontSize: 16),
                  ),
                  Row(
                    children: [
                      Text(
                        '@${widget.user.username}',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[600],
                        ),
                      ),
                      if (_isOtherUserTyping) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.blue[100],
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              SizedBox(
                                width: 10,
                                height: 10,
                                child: CircularProgressIndicator(
                                  strokeWidth: 1.5,
                                  color: Colors.blue[700],
                                ),
                              ),
                              const SizedBox(width: 4),
                              Text(
                                'typing...',
                                style: TextStyle(
                                  fontSize: 9,
                                  color: Colors.blue[700],
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ] else if (!_isConnected) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.orange[100],
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.cloud_off, size: 10, color: Colors.orange[700]),
                              const SizedBox(width: 4),
                              Text(
                                'Offline',
                                style: TextStyle(
                                  fontSize: 9,
                                  color: Colors.orange[700],
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(4),
            color: Colors.yellow[100],
            child: Text(
              'Debug: Me=$_currentUserId, Target=${widget.user.id}, Conn=$_isConnected',
              style: const TextStyle(fontSize: 10, color: Colors.black54),
            ),
          ),
          Expanded(child: _buildMessageList()),
          _buildMessageInput(),
        ],
      ),
    );
  }

  Widget _buildMessageList() {
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
              onPressed: _loadMessages,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    if (_messages.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.chat_bubble_outline, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text(
              'No messages yet (${_messages.length} total)',
              style: TextStyle(fontSize: 18, color: Colors.grey[600]),
            ),
            const SizedBox(height: 8),
            Text(
              'Send a message to start the conversation',
              style: TextStyle(fontSize: 14, color: Colors.grey[500]),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.all(16),
      itemCount: _messages.length,
      itemBuilder: (context, index) {
        final message = _messages[index];
        return _buildMessageBubble(message);
      },
    );
  }

  Widget _buildMessageBubble(ChatMessage message) {
    final isMe = message.sender.id == _currentUserId;

    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.7,
        ),
        decoration: BoxDecoration(
          color: isMe ? Theme.of(context).primaryColor : Colors.grey[200],
          borderRadius: BorderRadius.circular(18),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (message.content != null)
              Text(
                message.content!,
                style: TextStyle(
                  color: isMe ? Colors.white : Colors.black,
                  fontSize: 15,
                ),
              ),
            if (message.mediaUrl != null)
              Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Image.network(
                  message.mediaUrl!,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) {
                    return const Text('Image failed to load');
                  },
                ),
              ),
            const SizedBox(height: 4),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  _formatTimestamp(message.createdAt),
                  style: TextStyle(
                    color: isMe ? Colors.white70 : Colors.grey[600],
                    fontSize: 11,
                  ),
                ),
                if (isMe && message.isRead) ...[
                  const SizedBox(width: 4),
                  const Icon(
                    Icons.done_all,
                    size: 14,
                    color: Colors.white70,
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageInput() {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            spreadRadius: 1,
            blurRadius: 3,
            offset: const Offset(0, -1),
          ),
        ],
      ),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.image_outlined),
            onPressed: () {
              // TODO: Image picker
            },
          ),
          Expanded(
            child: TextField(
              controller: _messageController,
              decoration: const InputDecoration(
                hintText: 'Message...',
                border: InputBorder.none,
              ),
              textCapitalization: TextCapitalization.sentences,
              onSubmitted: (_) => _sendMessage(),
            ),
          ),
          IconButton(
            icon: Icon(
              Icons.send,
              color: Theme.of(context).primaryColor,
            ),
            onPressed: _sendMessage,
          ),
        ],
      ),
    );
  }

  String _formatTimestamp(String timestamp) {
    try {
      final dateTime = DateTime.parse(timestamp);
      return '${dateTime.hour}:${dateTime.minute.toString().padLeft(2, '0')}';
    } catch (e) {
      return '';
    }
  }
}
