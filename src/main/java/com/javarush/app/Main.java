package com.javarush.app;

import com.javarush.dao.CityDao;
import com.javarush.dao.CountryDao;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private final SessionFactory sessionFactory;
    private final CityDao cityDao;
    private final CountryDao countryDao;

    public Main(){
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.cityDao = new CityDao(sessionFactory);
        this.countryDao = new CountryDao(sessionFactory);
    }

    public static void main(String[] args) {
        Main main = new Main();

        org.hibernate.stat.Statistics stats = main.sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        List<City> cities = main.fetchAllCities();
        System.out.println("Количество SQL-запросов к БД: " + stats.getPrepareStatementCount());
        main.shutdown();
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

    private void shutdown(){
        if (sessionFactory != null){
            sessionFactory.close();
        }
    }
}
