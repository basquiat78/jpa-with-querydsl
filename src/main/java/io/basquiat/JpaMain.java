package io.basquiat;

import static io.basquiat.model.QBrand.brand;

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
        	List<Brand> brandLikeList = query.select(brand)
							       		 	 .from(brand)
							       		 	 .where(brand.enName.like("%end"))
							       		 	 .fetch();
        	System.out.println("like() start");
        	brandLikeList.stream().map(s -> s.toString())
    				 		  	  .forEach(System.out::println);
        	
        	List<Brand> brandStartsWithList = query.select(brand)
									       		   .from(brand)
									       		   .where(brand.enName.startsWith("end"))
									       		   .fetch();
			System.out.println("startsWith() start");
			brandStartsWithList.stream().map(s -> s.toString())
					  	 				.forEach(System.out::println);
			
			List<Brand> brandEndsWithList = query.select(brand)
											.from(brand)
											.where(brand.enName.endsWith("end"))
											.fetch();
			System.out.println("endsWith() start");
			brandEndsWithList.stream().map(s -> s.toString())
									  .forEach(System.out::println);
			
			List<Brand> brandContainsList = query.select(brand)
											.from(brand)
											.where(brand.enName.contains("end"))
											.fetch();
			System.out.println("contains() start");
			brandContainsList.stream().map(s -> s.toString())
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
