package com.example.worker_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.worker_service.service.ImageVariantWorker;
import com.example.worker_service.service.VideoThumbnailWorker;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    
    private final ImageVariantWorker imageVariantWorker;
    private final VideoThumbnailWorker videoThumbnailWorker;
    private static final String IMAGE_QUEUE = "image:variant:queue";
    private static final String PDF_QUEUE = "pdf:preview:queue";
    private static final String DOC_QUEUE = "doc:preview:queue";
    private static final String VIDEO_QUEUE = "video:thumbnail:queue";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis pub/sub listener configuration
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(imageVariantWorker, new PatternTopic(IMAGE_QUEUE));
        container.addMessageListener(videoThumbnailWorker, new PatternTopic(VIDEO_QUEUE));

        return container;
    }

    @Bean
    public MessageListenerAdapter imagelistenerAdapter(ImageVariantWorker worker) {
        return new MessageListenerAdapter(worker, "onMessage");
    }

    @Bean
    public MessageListenerAdapter videoListenerAdapter(VideoThumbnailWorker worker) {
        return new MessageListenerAdapter(worker, "onMessage");
    }
}
