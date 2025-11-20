import 'dart:io';
import 'package:flutter/material.dart';
import '../services/post_service.dart';

class CreatePostScreen extends StatefulWidget {
  final String imagePath;

  const CreatePostScreen({Key? key, required this.imagePath}) : super(key: key);

  @override
  State<CreatePostScreen> createState() => _CreatePostScreenState();
}

class _CreatePostScreenState extends State<CreatePostScreen> {
  final TextEditingController _captionController = TextEditingController();
  bool _isUploading = false;
  String _selectedVisibility = 'PUBLIC';
  String _selectedQuality = 'HIGH';

  @override
  void dispose() {
    _captionController.dispose();
    super.dispose();
  }

  Future<void> _uploadPost() async {
    setState(() {
      _isUploading = true;
    });

    try {
      await PostService.uploadPost(
        imagePath: widget.imagePath,
        caption: _captionController.text.trim(),
        visibility: _selectedVisibility,
        quality: _selectedQuality,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Post uploaded successfully!'),
          backgroundColor: Colors.green,
        ),
      );

      // Return true to indicate successful post creation
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error uploading post: $e'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isUploading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.black),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text(
          'New Post',
          style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold),
        ),
        actions: [
          if (_isUploading)
            const Center(
              child: Padding(
                padding: EdgeInsets.all(16.0),
                child: SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
              ),
            )
          else
            TextButton(
              onPressed: _uploadPost,
              child: const Text(
                'Share',
                style: TextStyle(
                  color: Colors.blue,
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
            ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image preview
            SizedBox(
              width: double.infinity,
              height: 400,
              child: Image.file(
                File(widget.imagePath),
                fit: BoxFit.cover,
              ),
            ),
            const SizedBox(height: 16),
            // Caption input
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: TextField(
                controller: _captionController,
                maxLines: 5,
                decoration: const InputDecoration(
                  hintText: 'Write a caption...',
                  border: InputBorder.none,
                  hintStyle: TextStyle(color: Colors.grey),
                ),
                style: const TextStyle(fontSize: 16),
              ),
            ),
            const Divider(),
            // Visibility setting
            ListTile(
              leading: const Icon(Icons.public),
              title: const Text('Visibility'),
              trailing: DropdownButton<String>(
                value: _selectedVisibility,
                underline: const SizedBox(),
                items: ['PUBLIC', 'PRIVATE', 'FRIENDS']
                    .map((String value) => DropdownMenuItem<String>(
                          value: value,
                          child: Text(value),
                        ))
                    .toList(),
                onChanged: (String? newValue) {
                  if (newValue != null) {
                    setState(() {
                      _selectedVisibility = newValue;
                    });
                  }
                },
              ),
            ),
            const Divider(),
            // Quality setting
            ListTile(
              leading: const Icon(Icons.high_quality),
              title: const Text('Image Quality'),
              trailing: DropdownButton<String>(
                value: _selectedQuality,
                underline: const SizedBox(),
                items: ['HIGH', 'MEDIUM', 'LOW']
                    .map((String value) => DropdownMenuItem<String>(
                          value: value,
                          child: Text(value),
                        ))
                    .toList(),
                onChanged: (String? newValue) {
                  if (newValue != null) {
                    setState(() {
                      _selectedQuality = newValue;
                    });
                  }
                },
              ),
            ),
            const Divider(),
            // Additional options
            ListTile(
              leading: const Icon(Icons.location_on),
              title: const Text('Add Location'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Location feature coming soon!')),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.person_add),
              title: const Text('Tag People'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Tag people feature coming soon!')),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}

