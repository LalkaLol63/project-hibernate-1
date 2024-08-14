package com.game.repository;

import com.fasterxml.jackson.databind.util.Named;
import com.game.entity.Player;
import jakarta.persistence.NamedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "my-secret-pw");
        properties.put(Environment.HBM2DDL_AUTO, "update");

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(com.game.entity.Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        List<Player> players = new ArrayList<>();
        try ( Session session = sessionFactory.openSession()) {
            String sql = "SELECT * FROM player";
            NativeQuery<Player> query = session.createNativeQuery(sql, Player.class);

            int firstIndex = pageNumber * pageSize;
            query.setFirstResult(firstIndex);
            query.setMaxResults(pageSize);

            players = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public int getAllCount() {
        Session session = sessionFactory.openSession();
        Query<Long> query = session.createNamedQuery("Player_getAllCount", Long.class);
        Long count = query.uniqueResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Player save(Player player) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Long id = (Long) session.save(player);
            player.setId(id);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        Player newPlayer = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            newPlayer = (Player) session.merge(player);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return newPlayer;
    }

    @Override
    public Optional<Player> findById(long id) {
        Optional<Player> result = Optional.empty();
        try(Session session = sessionFactory.openSession()) {
            result = Optional.ofNullable(session.get(Player.class, id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void delete(Player player) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}