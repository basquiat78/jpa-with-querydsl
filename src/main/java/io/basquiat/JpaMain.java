package io.basquiat;

import static io.basquiat.model.QProduct.product;

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
        	
        	List<Tuple> tuple = query.select(
        							 product.brandName,
        							 product.price.min(),
        							 product.price.max(),
        							 product.price.sum(),
									 product.price.avg()
									)
							  	.from(product)
							  	.groupBy(product.brandName)
							  	.having(	
							  			product.price.avg().gt(5000000),
							  			product.price.avg().lt(5400000)
							  			)
							  	.fetch();
        	
        	System.out.println(tuple);
        	
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
