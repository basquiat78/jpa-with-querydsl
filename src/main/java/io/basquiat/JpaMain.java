package io.basquiat;

import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
        	JPAQueryFactory query = new JPAQueryFactory(em);
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");
        	List<Tuple> tuple = query.select( partner.id,
											  partner.name,
											  partner.address,
											  brand.count().as("br_tot_count")
											 )
							 	   	  .from(partner)
							 	   	  .join(brand).on(partner.id.eq(brand.partner.id)) 
							 	   	  .groupBy(partner.id)
							 	   	  .fetch();
        	System.out.println(tuple.toString());
        	tx.commit();
        } catch(Exception e) {
        	e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
