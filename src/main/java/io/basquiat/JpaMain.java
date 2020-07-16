package io.basquiat;

import static io.basquiat.model.QBrand.brand;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.QueryResults;
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
        	QueryResults<Brand> qResult = query.select(brand)
							       		 	   .from(brand)
							       		 	   .join(brand.partner)
							       		 	   .limit(10)
							       		 	   .fetchResults();
        	System.out.println("paging start");
        	System.out.println("qResult offset : " +  qResult.getOffset());
        	System.out.println("qResult limit : " +  qResult.getLimit());
        	System.out.println("qResult totalCount : " +  qResult.getTotal());
        	qResult.getResults().stream().map(s -> s.toString())
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
