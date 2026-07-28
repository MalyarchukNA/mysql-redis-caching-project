package com.javarush.service;

import com.javarush.config.HibernateUtil;
import com.javarush.config.RedisUtil;
import com.javarush.dao.CityDao;
import com.javarush.dao.CountryDao;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.redis.CityDetail;
import com.javarush.redis.Language;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Интеграционный тест для замера производительности чтения сущностей {@link City}
 * из базы данных MySQL по сравнению скэшем в  Redis.
 */
public class CachePerformanceTest {
    private static SessionFactory sessionFactory;
    private static CityDao cityDao;
    private static CountryDao countryDao;
    private static RedisClient redisClient;
    private static ObjectMapper mapper;

    private static List<Integer> ids;

    /**
     * Инициализирует окружение, выгружает данные из MySQL, преобразует их,
     * кэширует в Redis и генерирует набор случайных идентификаторов перед выполнением тестов.
     */
    @BeforeAll
    static void setup(){
        sessionFactory = HibernateUtil.getSessionFactory();
        cityDao = new CityDao(sessionFactory);
        countryDao = new CountryDao(sessionFactory);
        redisClient =  RedisUtil.getRedisClient();
        mapper = new ObjectMapper();

        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        List<City> cities = fetchAllCities();
        System.out.println("Количество SQL-запросов к БД: " + stats.getPrepareStatementCount());

        List<CityDetail> preparedCities = prepareData(cities);
        pushToRedis(preparedCities);

        ids = generateRandomIds(1000);
    }

    /**
     * Тест производительности чтения данных из Redis.
     * Последовательно запрашивает случайные города по списку идентификаторов
     * и десериализует JSON в {@link CityDetail}.
     */
    @Test
    void measureRedisPerformance(){
        long startTime = System.currentTimeMillis();
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()){
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id: ids){
                String key = "city:" + id;
                String value = sync.get(key);
                mapper.readValue(value, CityDetail.class);
            }
        } catch (JacksonException e){
            //TODO: добавить в логгер
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("%s:\t%d ms\n", "Redis", duration);
    }

    /**
     * Тест производительности чтения данных из MySQL.
     * Последовательно запрашивает случайные города по списку идентификаторов из базы данных вместе со связанными языками.
     */
    @Test
    void measureMySqlPerformance() {
        long startTime = System.currentTimeMillis();
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDao.getById(id);
                city.getCountry().getLanguages().size();

            }
            session.getTransaction().commit();
        }
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("%s:\t%d ms\n", "MySQL", duration);
    }

    /**
     * Корректно завершает работу sessionFactory, Hibernate и Redis после завершения всех тестов.
     */
    @AfterAll
    static void tearDown(){
        if (sessionFactory != null && !sessionFactory.isClosed()){
            sessionFactory.close();
        }
        HibernateUtil.shutdown();
        RedisUtil.shutdown();
    }

    /**
     * Извлекает из базы данных список всех сущностей {@link City}.
     * Предварительно подгружает список стран {@link Country}.
     *
     * @return список объектов {@link City}
     */
    private static List<City> fetchAllCities(){
        try(Session session = sessionFactory.getCurrentSession()){
            session.beginTransaction();
            List<Country> countries = countryDao.getAll();
            List<City> cities = new ArrayList<>();
            long totalCount = cityDao.getTotalCount();
            int limit = 500;
            for (int i = 0; i < totalCount; i+= limit){
                cities.addAll(cityDao.getAll(limit, i));
            }
            session.getTransaction().commit();
            return cities;
        }
    }

    /**
     * Мапит список сущностей {@link City} в список объектов {@link CityDetail} для передачи в Redis.
     *
     * @param cities список городов из базы данных
     * @return список объектов {@link CityDetail}
     */
    private static List<CityDetail> prepareData(List<City> cities){
        return cities.stream().map(city -> {
                    CityDetail cityDetail = new CityDetail();
                    cityDetail.setId(city.getId());
                    cityDetail.setCityName(city.getName());
                    cityDetail.setDistrict(city.getDistrict());
                    cityDetail.setCityPopulation(city.getPopulation());

                    Country country = city.getCountry();
                    cityDetail.setCountryCode(country.getCode());
                    cityDetail.setCountryAltCode(country.getCode2());
                    cityDetail.setCountryName(country.getName());
                    cityDetail.setContinent(country.getContinent());
                    cityDetail.setCountryRegion(country.getRegion());
                    cityDetail.setCountrySurfaceArea(country.getSurfaceArea());
                    cityDetail.setCountryPopulation(country.getPopulation());

                    Set<Language> languages = country.getLanguages().stream()
                            .map(countryLanguage -> new Language(
                                            countryLanguage.getLanguage(),
                                            countryLanguage.getIsOfficial(),
                                            countryLanguage.getPercentage()
                                    )
                            )
                            .collect(Collectors.toSet());
                    cityDetail.setLanguages(languages);
                    return cityDetail;
                })
                .collect(Collectors.toList());
    }


    /**
     * Сериализует подготовленные объекты в формат JSON и записывает их в Redis.
     * @param preparedCities список подготовленных городов в формате частого запроса для сохранения
     */
    private static void pushToRedis(List<CityDetail> preparedCities){
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityDetail city: preparedCities){
                try {
                    String key = "city:" + city.getId();
                    String value = mapper.writeValueAsString(city);
                    sync.set(key, value);
                } catch (JacksonException e) {
                    //TODO: добавить в логгер
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Генерирует список из count случайных ID на основе реально существующих в базе.
     *
     * @param count размер генерируемого списка ID
     * @return список случайных числовых идентификаторов
     */
    public static List<Integer> generateRandomIds(int count) {
        List<Integer> result = new ArrayList<>(count);

        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<Integer> allIds = cityDao.getAllIds();

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < count; i++) {
                int randomIndex = random.nextInt(allIds.size());
                result.add(allIds.get(randomIndex));
            }

            session.getTransaction().commit();
        }
        return result;
    }

}
