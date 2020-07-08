package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Item;
import io.basquiat.model.QItem;

/**
 * 
 * created by basquiat
 *
 */
public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
        	
        	Item item = Item.builder().name("Fodera").price(15000000).build();
        	em.persist(item);
        	em.flush();
        	em.clear();
        	System.out.println("일단 DB에 날렸어~");

        	JPAQueryFactory query = new JPAQueryFactory(em);
        	QItem qItem = QItem.item;
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");
        	Item selected = query.selectFrom(qItem).fetchFirst();
        	System.out.println("queryDSL로 일단 조회했어~");
        	System.out.println(selected.toString());

        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
