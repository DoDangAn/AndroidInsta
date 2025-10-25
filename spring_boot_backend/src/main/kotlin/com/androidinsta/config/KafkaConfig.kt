package com.androidinsta.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder

@Configuration
@EnableKafka
class KafkaConfig {

    companion object {
        const val POST_CREATED_TOPIC = "post.created"
        const val POST_LIKED_TOPIC = "post.liked"
        const val POST_COMMENTED_TOPIC = "post.commented"
        const val USER_REGISTERED_TOPIC = "user.registered"
        const val USER_FOLLOWED_TOPIC = "user.followed"
        const val NOTIFICATION_TOPIC = "notification.send"
    }

    @Bean
    fun postCreatedTopic(): NewTopic = TopicBuilder
        .name(POST_CREATED_TOPIC)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun postLikedTopic(): NewTopic = TopicBuilder
        .name(POST_LIKED_TOPIC)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun postCommentedTopic(): NewTopic = TopicBuilder
        .name(POST_COMMENTED_TOPIC)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun userRegisteredTopic(): NewTopic = TopicBuilder
        .name(USER_REGISTERED_TOPIC)
        .partitions(2)
        .replicas(1)
        .build()

    @Bean
    fun userFollowedTopic(): NewTopic = TopicBuilder
        .name(USER_FOLLOWED_TOPIC)
        .partitions(2)
        .replicas(1)
        .build()

    @Bean
    fun notificationTopic(): NewTopic = TopicBuilder
        .name(NOTIFICATION_TOPIC)
        .partitions(3)
        .replicas(1)
        .build()
}
