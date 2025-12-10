import 'package:flutter/material.dart';
import '../services/reel_service.dart';

class ReelScreen extends StatefulWidget {
  const ReelScreen({Key? key}) : super(key: key);

  @override
  State<ReelScreen> createState() => _ReelScreenState();
}

class _ReelScreenState extends State<ReelScreen> {
  final PageController _pageController = PageController();
  List<Map<String, dynamic>> _reels = [];
  bool _isLoading = true;
  int _currentPage = 0;

  @override
  void initState() {
    super.initState();
    _loadReels();
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  Future<void> _loadReels() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final reels = await ReelService.getReelsFeed(page: _currentPage);
      setState(() {
        _reels.addAll(reels);
        _currentPage++;
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading reels: $e')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _likeReel(int reelId, bool isLiked) async {
    try {
      if (isLiked) {
        await ReelService.unlikeReel(reelId);
      } else {
        await ReelService.likeReel(reelId);
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading && _reels.isEmpty) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      body: PageView.builder(
        controller: _pageController,
        scrollDirection: Axis.vertical,
        itemCount: _reels.length,
        onPageChanged: (index) {
          if (index == _reels.length - 2) {
            _loadReels(); // Load more reels
          }
        },
        itemBuilder: (context, index) {
          final reel = _reels[index];
          return _buildReelItem(reel);
        },
      ),
    );
  }

  Widget _buildReelItem(Map<String, dynamic> reel) {
    return Stack(
      fit: StackFit.expand,
      children: [
        // Video player would go here
        Container(
          color: Colors.black,
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.play_circle_outline,
                    size: 80, color: Colors.white),
                const SizedBox(height: 16),
                Text(
                  reel['videoUrl'] ?? 'Video',
                  style: const TextStyle(color: Colors.white),
                ),
              ],
            ),
          ),
        ),

        // Reel info overlay
        Positioned(
          bottom: 0,
          left: 0,
          right: 80,
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.bottomCenter,
                end: Alignment.topCenter,
                colors: [
                  Colors.black.withOpacity(0.8),
                  Colors.transparent,
                ],
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      radius: 16,
                      child: Text(reel['username']?[0] ?? 'U'),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      reel['username'] ?? 'Unknown',
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                if (reel['caption'] != null) ...[
                  const SizedBox(height: 8),
                  Text(
                    reel['caption'],
                    style: const TextStyle(color: Colors.white),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
                if (reel['musicName'] != null) ...[
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.music_note,
                          color: Colors.white, size: 16),
                      const SizedBox(width: 4),
                      Expanded(
                        child: Text(
                          reel['musicName'],
                          style: const TextStyle(color: Colors.white),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
        ),

        // Action buttons
        Positioned(
          right: 8,
          bottom: 100,
          child: Column(
            children: [
              _buildActionButton(
                icon: reel['isLiked'] == true
                    ? Icons.favorite
                    : Icons.favorite_border,
                label: '${reel['likesCount'] ?? 0}',
                color: reel['isLiked'] == true ? Colors.red : Colors.white,
                onTap: () =>
                    _likeReel(reel['id'], reel['isLiked'] ?? false),
              ),
              const SizedBox(height: 16),
              _buildActionButton(
                icon: Icons.comment,
                label: '${reel['commentsCount'] ?? 0}',
                onTap: () {
                  // Show comments
                },
              ),
              const SizedBox(height: 16),
              _buildActionButton(
                icon: Icons.share,
                label: 'Share',
                onTap: () {
                  // Share reel
                },
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required String label,
    Color color = Colors.white,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Icon(icon, color: color, size: 32),
          const SizedBox(height: 4),
          Text(
            label,
            style: const TextStyle(color: Colors.white, fontSize: 12),
          ),
        ],
      ),
    );
  }
}
