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
        	
        	List<Brand> selectBrand = query.select(brand)
						 	   	     .from(brand)
						 	   	     .innerJoin(brand.partner, partner)
						 	   	     .fetch();
        	System.out.println(selectBrand.size());
        	System.out.println(selectBrand.toString());
        	selectBrand.stream().map(br -> br.getPartner().toString())
        						.forEach(System.out::println);
        	
        	System.out.println("========================================");
        	
        	List<Brand> fetchBrand = query.select(brand)
							 	   	      .from(brand)
								 	   	  .leftJoin(brand.partner).fetchJoin()
			 					 	   	  .orderBy(brand.partner.name.asc().nullsFirst())
								 	   	  .fetch();
			System.out.println(fetchBrand.size());
			System.out.println(fetchBrand.toString());
			fetchBrand.stream().map(br -> br.getPartner() == null ? "null": br.getPartner().toString())
							   .forEach(System.out::println);
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
