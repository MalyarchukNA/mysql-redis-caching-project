package com.javarush.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Утилитный класс для управления подключением к Redis.
 * Реализует паттерн Singleton, чтобы на всё приложение был создан
 * только один экземпляр RedisClient.
 */
public class RedisUtil {
    private static final RedisClient redisClient;

    static {
        Properties properties = new Properties();
        try(InputStream inputStream = RedisUtil.class.getClassLoader().getResourceAsStream("redis.properties")) {
            if (inputStream != null){
                properties.load(inputStream);
            }
        } catch (IOException e) {
            //TODO: добавить в логгер
            e.printStackTrace();
        }

        String host = properties.getProperty("redis.host", "localhost");
        Integer port = Integer.parseInt(properties.getProperty("redis.port", "6379"));

        RedisURI redisURI = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .build();

        redisClient = RedisClient.create(redisURI);
    }

    public static RedisClient getRedisClient() {
        return redisClient;
    }

    public static void shutdown(){
        if (redisClient != null){
            redisClient.shutdown();
        }
    }
}
