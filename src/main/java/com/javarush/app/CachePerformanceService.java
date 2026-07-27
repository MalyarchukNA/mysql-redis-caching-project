package com.javarush.app;

import com.javarush.config.HibernateUtil;
import com.javarush.config.RedisUtil;
import com.javarush.dao.CityDao;
import com.javarush.dao.CountryDao;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.domain.CountryLanguage;
import com.javarush.redis.CityDetail;
import com.javarush.redis.Language;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Сервис для подготовки данных, кэширования сущностей {@link City} в Redis
 * и замера производительности чтения из MySQL по сравнению с Redis.
 */
public class CachePerformanceService {
    private final SessionFactory sessionFactory;
    private final CityDao cityDao;
    private final CountryDao countryDao;
    private final RedisClient redisClient;
    private final ObjectMapper mapper;

    public CachePerformanceService(){
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.cityDao = new CityDao(sessionFactory);
        this.countryDao = new CountryDao(sessionFactory);
        this.redisClient =  RedisUtil.getRedisClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Замерят скорость чтения данных из Redis.
     * Извлекает данные городов из Redis по списку идентификаторов
     * и десериализует их из формата JSON обратно в объекты {@link CityDetail}.
     *
     * @param ids список идентификаторов городов для поиска в кэше
     * @return время работы в миллисекундах
     */
    public long measureRedisPerformance(List<Integer> ids){
        long startTime = System.currentTimeMillis();
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id: ids){
                String key = "city:" + String.valueOf(id);
                String value = sync.get(key);
                mapper.readValue(value, CityDetail.class);
            }
        } catch (JacksonException e){
            //TODO: добавить в логгер
            e.printStackTrace();
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Замерят скорость чтения данных из MySQL.
     * Извлекает данные городов из базы данных по списку идентификаторов
     *
     * @param ids список идентификаторов городов для поиска в базе данных
     * @return время работы в миллисекундах
     */
    public long measureMySqlPerformance(List<Integer> ids){
        long startTime = System.currentTimeMillis();
        try(Session session = sessionFactory.getCurrentSession()){
            session.beginTransaction();
            for (Integer id: ids){
                City city = cityDao.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();

            }
            session.getTransaction().commit();
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Выполняет полный цикл подготовки данных: выгрузка из MySQL, маппинг и кэширование в Redis.
     */
    public void prepareAndCacheData(){
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        List<City> cities = fetchAllCities();
        System.out.println("Количество SQL-запросов к БД: " + stats.getPrepareStatementCount());

        List<CityDetail> preparedCities = prepareData(cities);
        pushToRedis(preparedCities);
    }

    /**
     * Извлекает из базы данных список всех сущностей {@link City}.
     * Предварительно подгружает список стран {@link Country}.
     * @return список объектов {@link City}
     */
    private List<City> fetchAllCities(){
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
     * @param cities список городов из базы данных
     * @return список объектов {@link CityDetail}
     */
    private List<CityDetail> prepareData(List<City> cities){
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
     * Записывает в Redis сущности из списка {@link CityDetail}
     * @param preparedCities список подготовленных городов в формате частого запроса для сохранения
     */
    private void pushToRedis(List<CityDetail> preparedCities){
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityDetail city: preparedCities){
                try {
                    String key = "city:" + String.valueOf(city.getId());
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
     */
    public List<Integer> generateRandomIds(int count) {
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

    /**
     * Корректно завершает работу sessionFactory, Hibernate и Redis
     */
    public void shutdown(){
        if (sessionFactory != null && !sessionFactory.isClosed()){
            sessionFactory.close();
        }
        HibernateUtil.shutdown();
        RedisUtil.shutdown();
    }

}
