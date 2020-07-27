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

        	String brandCode = "FENDER";
        	String enName = "Fender";
        	String name = "펜더";
        	String partnerId = "RIDINBASS";
        	String partnerName = "라이딩 베이스";
        	
        	List<Tuple> brandList = query.select(
        										 brand.name, 
        										 brand.enName, 
        										 //Expressions.constant(100),
        										 //ExpressionUtils.as(Expressions.constant(500), "constant"),
        										 brand.constant(100),
        										 brand.constant(500, "constant")
        										 )
        								 .from(brand)
										 .where(
													 //brand.codeEq(brandCode)
													 //brand.nameEq(name),
													 //brand.enNameEq(enName)
													 //brand.enNameAndNameEq(enName, name)
													 brand.enNameOrNameEq(enName, name)
												  )
										  .fetch();
        	System.out.println(brandList.toString());
        	List<Partner> partnerList = query.selectFrom(partner)
        								 	 .where(
	        										 //partner.idEq(partnerId)
	        										 //partner.nameEq(partnerName)
	        										 //partner.idAndNameEq(partnerId, partnerName)
	        										 partner.idOrNameEq(partnerId, partnerName)
	        										)
        								 	 .fetch();
        	System.out.println(partnerList.toString());
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
