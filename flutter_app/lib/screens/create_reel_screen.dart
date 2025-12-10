import 'package:flutter/material.dart';
import 'dart:io' show File;

class CreateReelScreen extends StatefulWidget {
  const CreateReelScreen({Key? key}) : super(key: key);

  @override
  State<CreateReelScreen> createState() => _CreateReelScreenState();
}

class _CreateReelScreenState extends State<CreateReelScreen> {
  final TextEditingController _captionController = TextEditingController();
  final TextEditingController _musicNameController = TextEditingController();
  final TextEditingController _musicArtistController = TextEditingController();
  
  File? _selectedVideo;
  bool _isUploading = false;

  @override
  void dispose() {
    _captionController.dispose();
    _musicNameController.dispose();
    _musicArtistController.dispose();
    super.dispose();
  }

  Future<void> _pickVideo() async {
    // TODO: Implement video picker
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Video picker not implemented yet')),
    );
  }

  Future<void> _recordVideo() async {
    // TODO: Implement video recording
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Video recording not implemented yet')),
    );
  }

  Future<void> _uploadReel() async {
    if (_selectedVideo == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a video')),
      );
      return;
    }

    setState(() {
      _isUploading = true;
    });

    try {
      // TODO: Upload reel to backend
      await Future.delayed(const Duration(seconds: 2)); // Simulate upload
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Reel uploaded successfully!')),
      );
      Navigator.pop(context, true);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error uploading reel: $e')),
      );
    } finally {
      setState(() {
        _isUploading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Reel'),
        actions: [
          TextButton(
            onPressed: _isUploading ? null : _uploadReel,
            child: _isUploading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text(
                    'Post',
                    style: TextStyle(
                      color: Colors.purple,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Video preview
            Container(
              width: double.infinity,
              height: 400,
              decoration: BoxDecoration(
                color: Colors.black,
                borderRadius: BorderRadius.circular(8),
              ),
              child: _selectedVideo == null
                  ? Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(
                          Icons.videocam,
                          size: 80,
                          color: Colors.white54,
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'No video selected',
                          style: TextStyle(color: Colors.white54),
                        ),
                        const SizedBox(height: 24),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            ElevatedButton.icon(
                              onPressed: _isUploading ? null : _pickVideo,
                              icon: const Icon(Icons.photo_library),
                              label: const Text('Pick Video'),
                            ),
                            const SizedBox(width: 16),
                            ElevatedButton.icon(
                              onPressed: _isUploading ? null : _recordVideo,
                              icon: const Icon(Icons.videocam),
                              label: const Text('Record'),
                            ),
                          ],
                        ),
                      ],
                    )
                  : const Center(
                      child: Icon(
                        Icons.play_circle_outline,
                        size: 80,
                        color: Colors.white,
                      ),
                    ),
            ),
            const SizedBox(height: 24),

            // Caption
            TextField(
              controller: _captionController,
              decoration: const InputDecoration(
                labelText: 'Caption',
                hintText: 'Write a caption...',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.edit),
              ),
              maxLines: 3,
              enabled: !_isUploading,
            ),
            const SizedBox(height: 16),

            // Music section
            const Text(
              'Add Music',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _musicNameController,
              decoration: const InputDecoration(
                labelText: 'Music Name',
                hintText: 'Enter music name...',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.music_note),
              ),
              enabled: !_isUploading,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _musicArtistController,
              decoration: const InputDecoration(
                labelText: 'Artist',
                hintText: 'Enter artist name...',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.person),
              ),
              enabled: !_isUploading,
            ),
            const SizedBox(height: 24),

            // Effects and filters (placeholder)
            Card(
              child: Column(
                children: [
                  ListTile(
                    leading: const Icon(Icons.filter),
                    title: const Text('Add Filter'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: _isUploading ? null : () {
                      // Add filter
                    },
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.auto_fix_high),
                    title: const Text('Add Effects'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: _isUploading ? null : () {
                      // Add effects
                    },
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.speed),
                    title: const Text('Adjust Speed'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: _isUploading ? null : () {
                      // Adjust speed
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
