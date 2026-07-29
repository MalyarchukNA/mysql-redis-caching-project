package com.javarush.config;

import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.domain.CountryLanguage;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Утилитарный класс для работы с Hibernate SessionFactory.
 * Инициализирует конфигурацию, регистрирует сущности и предоставляет
 * доступ к сессиям для работы с базой данных.
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    static {
        try{
            sessionFactory = new Configuration()
                    .addAnnotatedClass(City.class)
                    .addAnnotatedClass(Country.class)
                    .addAnnotatedClass(CountryLanguage.class)
                    .buildSessionFactory();
            logger.info("Hibernate SessionFactory создана.");
        } catch (Throwable e) {
            logger.error("Ошибка при инициализации SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown(){
        if (sessionFactory != null && !sessionFactory.isClosed()){
            sessionFactory.close();
            logger.info("Hibernate SessionFactory закрыта.");
        }
    }

}
