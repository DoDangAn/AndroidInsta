import 'package:flutter/material.dart';
import '../services/profile_service.dart';
import '../models/user_profile.dart';

class ProfileScreen extends StatefulWidget {
  final int userId;

  const ProfileScreen({Key? key, required this.userId}) : super(key: key);

  @override
  _ProfileScreenState createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late Future<UserProfile> _userProfileFuture;

  @override
  void initState() {
    super.initState();
    _userProfileFuture = ProfileService.fetchUserProfile(widget.userId);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
      ),
      body: FutureBuilder<UserProfile>(
        future: _userProfileFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          } else if (snapshot.hasData) {
            final userProfile = snapshot.data!;
            return ListView(
              children: [
                _buildProfileHeader(userProfile),
                _buildProfileStats(userProfile),
                _buildActionButtons(),
                _buildPostGrid(userProfile),
              ],
            );
          } else {
            return const Center(child: Text('User not found.'));
          }
        },
      ),
    );
  }

  Widget _buildProfileHeader(UserProfile user) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        children: [
          CircleAvatar(
            radius: 40,
            backgroundImage: NetworkImage(user.avatarUrl),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  user.username,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                if (user.fullName != null) Text(user.fullName!),
                if (user.bio != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 4.0),
                    child: Text(
                      user.bio!,
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileStats(UserProfile user) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        _buildStatItem('Posts', user.posts.length),
        _buildStatItem('Followers', user.followers.length),
        _buildStatItem('Following', user.following.length),
      ],
    );
  }

  Widget _buildStatItem(String label, int count) {
    return Column(
      children: [
        Text(
          count.toString(),
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        Text(label, style: TextStyle(color: Colors.grey[700])),
      ],
    );
  }

  Widget _buildActionButtons() {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          ElevatedButton(onPressed: () {}, child: const Text('Follow')),
          ElevatedButton(onPressed: () {}, child: const Text('Message')),
        ],
      ),
    );
  }

  Widget _buildPostGrid(UserProfile user) {
    if (user.posts.isEmpty) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(20.0),
          child: Text('No posts yet.'),
        ),
      );
    }
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 2,
        mainAxisSpacing: 2,
      ),
      itemCount: user.posts.length,
      itemBuilder: (context, index) {
        return Image.network(
          user.posts[index].mediaFiles.first.fileUrl,
          fit: BoxFit.cover,
        );
      },
    );
  }
}
