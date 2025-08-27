package com.stockexchange.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import java.io.IOException;

/* 테스트용 Embedded Redis 설정
 * 실제 Redis 서버 없이 메모리에서 Redis 동작을 시뮬레이션 */
@TestConfiguration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port:6370}")
    private int redisPort;
    private RedisServer redisServer;

    /* Embedded Redis 서버 시작
     * - 테스트 시작 시 자동으로 메모리에서 Redis 서버 실행
     * - 6370 포트에서 실행 (기본 Redis 포트 6379와 충돌 방지)
     * - 최대 메모리 128MB로 제한*/
    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
    }

    /* Embedded Redis 서버 종료 */
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    /* 테스트용 Redis Connection Factory
     * - Embedded Redis 서버(localhost:6370)에 연결
     * - @Primary로 운영환경 설정보다 우선순위 높음*/
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", redisPort);
        factory.afterPropertiesSet(); // 연결 설정 초기화
        return factory;
    }

    /* 테스트용 RedisTemplate 설정
     * - key: String으로 직렬화
     * - Value: JSON으로 직렬화
     * @Primary로 기본 RedisTemplate 보다 우선 사용 */

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

//        직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setEnableTransactionSupport(true); // 트랜잭션 지원
        template.afterPropertiesSet(); // 설정 초기화
        return template;
    }

}
