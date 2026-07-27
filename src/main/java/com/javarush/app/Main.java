package com.javarush.app;

import java.util.List;


public class Main {

    public static void main(String[] args) {
        CachePerformanceService service = new CachePerformanceService();

        service.prepareAndCacheData();
        List<Integer> ids = service.generateRandomIds(1000);

        long redisDuration = service.measureRedisPerformance(ids);
        long mySqlDuration = service.measureMySqlPerformance(ids);

        System.out.printf("%s:\t%d ms\n", "Redis", redisDuration);
        System.out.printf("%s:\t%d ms\n", "MySQL", mySqlDuration);

        service.shutdown();
    }

}
