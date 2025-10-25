class UserProfile {
  final int id;
  final String username;
  final String? fullName;
  final String? bio;
  final String avatarUrl;
  final List<Post> posts;
  final List<UserSummary> followers;
  final List<UserSummary> following;

  UserProfile({
    required this.id,
    required this.username,
    this.fullName,
    this.bio,
    required this.avatarUrl,
    required this.posts,
    required this.followers,
    required this.following,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'],
      username: json['username'],
      fullName: json['fullName'],
      bio: json['bio'],
      avatarUrl: json['avatarUrl'] ?? 'https://via.placeholder.com/150',
      posts: (json['posts'] as List).map((i) => Post.fromJson(i)).toList(),
      followers: (json['followers'] as List)
          .map((i) => UserSummary.fromJson(i))
          .toList(),
      following: (json['following'] as List)
          .map((i) => UserSummary.fromJson(i))
          .toList(),
    );
  }
}
