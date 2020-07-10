package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Brand;
import io.basquiat.model.Partner;
import static io.basquiat.model.QBrand.*;

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
        	Brand selectedBrand = query.select(brand)
        					   .from(brand)
        					   .where(brand.enName.eq("Marleaux"))
        					   .fetchOne();
			System.out.println(selectedBrand.toString());
			
			System.out.println("Lazy Loadging, 곧 쿼리가 날아가겠지.");
			Partner partner = selectedBrand.getPartner();
			System.out.println(partner.toString());
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
