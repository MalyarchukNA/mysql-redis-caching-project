package com.javarush.dao;

import com.javarush.domain.Country;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO для работы с сущностью {@link Country} в базе данных
 * Предоставляет методы для получения списка стран.
 */
public class CountryDao {
    private final SessionFactory sessionFactory;

    public CountryDao(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    /**
     * Возвращает список всех стран в базе данных.
     */
    public List<Country> getAll(){
        Query<Country> query = sessionFactory.getCurrentSession().createQuery("select distinct c from Country c left join fetch c.languages", Country.class);
        return query.list();
    }
}
