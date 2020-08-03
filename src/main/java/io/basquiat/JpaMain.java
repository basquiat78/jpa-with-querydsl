package io.basquiat;

import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Brand;
import io.basquiat.model.Partner;

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
        	
        	long goodbyeBrand = query.delete(brand)
        					   		 .execute();
        	
        	long goodbyePartner = query.delete(partner)
			   		 				 .execute();
        	
        	System.out.println(goodbyeBrand);
        	System.out.println(goodbyePartner);
        		
        	List<Brand> brandList = query.selectFrom(brand).fetch();
        	List<Partner> partnerList = query.selectFrom(partner).fetch();
        	System.out.println(brandList);
        	System.out.println(partnerList);
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
