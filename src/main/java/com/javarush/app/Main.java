package com.javarush.app;

import com.javarush.dao.CityDao;
import com.javarush.dao.CountryDao;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.redis.CityDetail;
import com.javarush.redis.Language;
import config.HibernateUtil;
import config.RedisUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private final SessionFactory sessionFactory;
    private final CityDao cityDao;
    private final CountryDao countryDao;
    private final RedisClient redisClient;
    private final ObjectMapper mapper;

    public Main(){
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.cityDao = new CityDao(sessionFactory);
        this.countryDao = new CountryDao(sessionFactory);
        this.redisClient =  RedisUtil.getRedisClient();
        this.mapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        Main main = new Main();

        org.hibernate.stat.Statistics stats = main.sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        List<City> cities = main.fetchAllCities();
        System.out.println("Количество SQL-запросов к БД: " + stats.getPrepareStatementCount());
        List<CityDetail> preparedCities = main.prepareData(cities);
        main.pushToRedis(preparedCities);
        main.shutdown();
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

    private void pushToRedis(List<CityDetail> preparedCities){
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityDetail city: preparedCities){
                try {
                    sync.set( String.valueOf(city.getId()), mapper.writeValueAsString(city));
                } catch (JacksonException e) {
                    //TODO: добавить в логгер
                    e.printStackTrace();
                }
            }
        }
    }

    private void shutdown(){
        HibernateUtil.shutdown();
        RedisUtil.shutdown();
    }
}
