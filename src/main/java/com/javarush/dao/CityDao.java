package com.javarush.dao;

import com.javarush.domain.City;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import java.util.List;

/**
 * DAO для работы с сущностью {@link City} в базе данных.
 * Предоставляет методы для получения списка городов сучетом пагинации и подсчета их общего количества.
 */
public class CityDao {
    private final SessionFactory sessionFactory;

    public CityDao(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    /**
     * Возвращает список городов с учетом пагинации.
     */
    public List<City> getAll(int limit, int offset){
        Query<City> query = sessionFactory.getCurrentSession().createQuery("select c from City c", City.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    /**
     * Возвращает общее количество городов в базе данных.
     */
    public int getTotalCount(){
        Query<Long> query = sessionFactory.getCurrentSession().createQuery("select COUNT(c) FROM City c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }
}
