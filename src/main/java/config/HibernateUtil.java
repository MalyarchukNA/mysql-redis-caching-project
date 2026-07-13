package config;

import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.domain.CountryLanguage;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try{
            sessionFactory = new Configuration()
                    .addAnnotatedClass(City.class)
                    .addAnnotatedClass(Country.class)
                    .addAnnotatedClass(CountryLanguage.class)
                    .buildSessionFactory();
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
