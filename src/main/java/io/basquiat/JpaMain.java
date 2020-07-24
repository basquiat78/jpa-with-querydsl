package io.basquiat;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;

import java.util.List;
import java.util.Map;

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

//        	ProductDTO selectProduct = query.select(Projections.constructor(
//														ProductDTO.class, 
//														product.id,
//														product.name, 
//														product.price,
//														product.brandName,
//														product.model,
//														product.color,
//														JPAExpressions.select(product.count()).from(product)
//												))
//									   		.from(product)
//									   		.where(product.id.eq(1L))
//									   		.fetchFirst();
//        	System.out.println(selectProduct.toString());
        	
        	Map<String, List<Brand>> results = query.from(brand)
        											.join(brand.partner, partner)
									        		//.transform(groupBy(partner.id).as(list(brand)));
        											.transform(groupBy(partner.name).as(list(brand)));

        	List<Brand> brandList = results.get("라이딩 베이스");
        	System.out.println(brandList);
        	
        	Brand selectOne = brandList.get(0);
        	selectOne.setNumber(11111);
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
