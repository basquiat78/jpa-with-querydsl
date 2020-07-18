# SELECT using queryDSL    

## 시작하기 전에    

이전 브랜치에서 등록했던 데이터를 그대로 사용할 예정이다.    

또한 SELECT 관련 API를 살펴보는 만큼 hibernate.hbm2ddl.auto는 당연히 none으로 두자.    


### fetch()     

개발자 영어 용어 정리를 찾아보면 fetch, get, retrieve의 늬앙스에 대한 글들이 있다. 한번 찾아보길 바란다. ~~하지만 링크를 걸어주는면 어떨까?~~    

[fetch, get, retrieve](https://nassol.tistory.com/3?fbclid=IwAR1idBVZUeVx9UxEm2J9ri2ZOf_6zMsNCts0hKZ12bdIU7w-GK54d1-_cPQ)     

그냥 요즘은 get이라는 표현보다는 fetch를 많이 쓰는듯 싶다. 그것을 반영한 것인가? 이전 버전에는 못봤던거 같은데 최신 버전은 이렇게 변한듯.     

아무튼 단어의 뜻을 보면 '가져오다'라는 의미이니 DB에서 정보를 가져오겠구나 할 수 있지만 그 어떤 조건도 없다.    

그렇다. 저것은 그냥 다 가져오는 것이다.

코드로 확인해 보자.    

```

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
        	List<Brand> brandList = query.select(brand)
			        					  .from(brand)
			        					  .fetch();
        	
        	brandList.stream().forEach(b -> {
						        		System.out.println(b.toString());
						        		System.out.println(b.getPartner());
        					  });
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

```

결과는?    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.partner_id as partner_6_0_,
            brand0_.updated_at as updated_5_0_ 
        from
            basquiat_brand brand0_
Brand(code=FBASS, name=에프베이스, enName=FBass, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    select
        partner0_.id as id1_1_0_,
        partner0_.address as address2_1_0_,
        partner0_.entry_at as entry_at3_1_0_,
        partner0_.partner_name as partner_4_1_0_,
        partner0_.updated_at as updated_5_1_0_ 
    from
        basquiat_partner partner0_ 
    where
        partner0_.id=?
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    select
        partner0_.id as id1_1_0_,
        partner0_.address as address2_1_0_,
        partner0_.entry_at as entry_at3_1_0_,
        partner0_.partner_name as partner_4_1_0_,
        partner0_.updated_at as updated_5_1_0_ 
    from
        basquiat_partner partner0_ 
    where
        partner0_.id=?
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)

```

중간에 쿼리를 한번씩 더 날리는 이유는 Lazy모드 때문이다.     

처음 브랜드에서 파트너 정보를 가져올 때 한번 날리고 1차 캐시에 있기 때문에 날리지 않다가 브랜드의 fk가 변경되면 다시 한번 날려서 가져오기 때문이다. ~~JPA 기초~~    

자. 사실 이대로 그냥 쓰면 안되는거 다들 알것이다. 데이터가 몇건 되지 않으니까 테스트로 사용하지만 실제 수십만건의 데이터가 있다면 어떻게 될까?    

그것은 차후 where를 이용한 조건 검색을 통한 예제에서 서서히 알아보자.    

### fetchOne()    

단 건을 조회해 올 때 사용한다. 값이 없으면 null을 반환하지만 만일 2건 이상의 데이터가 조회되면 NonUniqueResultException 에러를 발생시킨다.

다음과 같이 실행을 해보자.    

```
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
        	Brand selectBrand = query.select(brand)
        							 .from(brand)
        							 .limit(1)
		        					 .fetchOne();
        	
        	System.out.println(selectBrand.toString());
        	System.out.println(selectBrand.getPartner().toString());
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

결과

queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.partner_id as partner_6_0_,
            brand0_.updated_at as updated_5_0_ 
        from
            basquiat_brand brand0_ limit ?
Brand(code=FBASS, name=에프베이스, enName=FBass, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    select
        partner0_.id as id1_1_0_,
        partner0_.address as address2_1_0_,
        partner0_.entry_at as entry_at3_1_0_,
        partner0_.partner_name as partner_4_1_0_,
        partner0_.updated_at as updated_5_1_0_ 
    from
        basquiat_partner partner0_ 
    where
        partner0_.id=?
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)

```

하지만 만일 

```
Brand selectBrand = query.select(brand)
        							 .from(brand)
        							 .limit(2) // 즉, 반환되는 데이터가 2건이상 나오게 하거나 이 조건을 주지 않으면?
		        					 .fetchOne();

```

이렇게 하게 되면 어떤 일이 벌어질까? 일단 fetchOne 이전에 2개 이상의 데이터를 반환할 것이다.      

내부적으로 다음 코드가 수행된다.    

```
 @Nullable
@SuppressWarnings("unchecked")
@Override
public T fetchOne() throws NonUniqueResultException {
    try {
        Query query = createQuery(getMetadata().getModifiers(), false);
        return (T) getSingleResult(query);
    } catch (javax.persistence.NoResultException e) {
        logger.trace(e.getMessage(),e);
        return null;
    } catch (javax.persistence.NonUniqueResultException e) {
        throw new NonUniqueResultException(e);
    } finally {
        reset();
    }
}

```

그래서 아래와 같은 에러가 발생한다.

```

queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.partner_id as partner_6_0_,
            brand0_.updated_at as updated_5_0_ 
        from
            basquiat_brand brand0_
com.querydsl.core.NonUniqueResultException: javax.persistence.NonUniqueResultException: query did not return a unique result: 6
	at com.querydsl.jpa.impl.AbstractJPAQuery.fetchOne(AbstractJPAQuery.java:258)
	at io.basquiat.JpaMain.main(JpaMain.java:33)
Caused by: javax.persistence.NonUniqueResultException: query did not return a unique result: 6
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:128)
	at org.hibernate.query.internal.AbstractProducedQuery.getSingleResult(AbstractProducedQuery.java:1588)
	at com.querydsl.jpa.impl.AbstractJPAQuery.getSingleResult(AbstractJPAQuery.java:183)
	at com.querydsl.jpa.impl.AbstractJPAQuery.fetchOne(AbstractJPAQuery.java:253)
	... 1 more

```
다시 밑에 코드를 보자.    

```
Brand selectBrand = query.select(brand)
					   .from(brand)
					   .limit(1)
    					   .fetchOne();
```
limit 1을 걸게 되면 조회된 데이터에서 1건을 가져오게 된다. 당연히 단건이 조회되고 fetchOne으로 결과가 반환된다.

### fetchFirst()    

그래서 이걸 쓰면 된다.    

내부 코드는 이렇다.


```
@Override
public final T fetchFirst() {
    return limit(1).fetchOne();
}
```
테스트는 굳이 하지 않겠다.    

### fetchResults()    

이 메소드를 보면서 처음에 느꼈던 것은 fetch라는 녀석이 있는데 왜 이게 있을까였다. 하지만 실제로 fetch()와는 다르게 반환 타입이 좀 다르다는 것을 알 수 있다.    

nodeJS와 관련해서 얘기를 하면 백오피스, 즉 어떤 어플리케이션의 어드민사이트를 개발하다보면 jQuery쓰게 되면 보통 bootstrap table이나 datatables같은 그리드를 사용할 일이 있다.     

보통 그리드의 경우에는 페이징을 처리하기 위해서 필요한 것들이 있는데 바로 페이지 사이즈, 화면에 보여줄 row 수, 그리고 전체 카운트가 필요하다.    

눈썰미가 있다면 이 이야기를 듣고 이 메소드는 바로 페이징 관련 정보를 함꼐 가지고 있는 녀석이겠구나 할 수도 있을 것이다.     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/2.query-dsl-select/capture/capture1.png)    

이미지를 보면 반환된 객체에서 무엇을 얻을 수 있는지 알 수 있다.    

일단 코드 한번 보자.    


```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;

import java.util.List;

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
        	QueryResults<Brand> queryResults = query.select(brand)
				        						 .from(brand)
						        				 .fetchResults();

        	System.out.println(queryResults.getLimit());
        	System.out.println(queryResults.getOffset());
        	System.out.println(queryResults.getTotal());
        	
        	List<Brand> brandList = queryResults.getResults();
        	brandList.stream().forEach(b -> {
    								System.out.println(b.toString());
    								System.out.println(b.getPartner().toString());
							  });
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
```

결과를 보면 

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        count(brand) 
    from
        Brand brand */ select
            count(brand0_.br_code) as col_0_0_ 
        from
            basquiat_brand brand0_
Hibernate: 
    /* select
        brand 
    from
        Brand brand */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.partner_id as partner_6_0_,
            brand0_.updated_at as updated_5_0_ 
        from
            basquiat_brand brand0_
9223372036854775807
0
6
Brand(code=FBASS, name=에프베이스, enName=FBass, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    select
        partner0_.id as id1_1_0_,
        partner0_.address as address2_1_0_,
        partner0_.entry_at as entry_at3_1_0_,
        partner0_.partner_name as partner_4_1_0_,
        partner0_.updated_at as updated_5_1_0_ 
    from
        basquiat_partner partner0_ 
    where
        partner0_.id=?
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    select
        partner0_.id as id1_1_0_,
        partner0_.address as address2_1_0_,
        partner0_.entry_at as entry_at3_1_0_,
        partner0_.partner_name as partner_4_1_0_,
        partner0_.updated_at as updated_5_1_0_ 
    from
        basquiat_partner partner0_ 
    where
        partner0_.id=?
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)

```

맨 앞에 카운터를 가져오기 위한 쿼리를 한번 날리는 것을 알 수 있다.     

하지만 SprginBoot와 JPA와 연계할 때 카운터와 관련되서 따로 카운터 쿼리를 작성할 수 있는 방법이 있다.     

왜 그럴까? 실무에서는 테이블은 단순하게 하나가 아니라 여러 개의 테이블과 조인해서 가져오는 경우가 있다. 그래서 실제로 카운터를 가져오는 쿼리와 다를 수 있다.    

무슨 말이냐면 많은 테이블과 조인을 했지만 실제 카운트 자체는 조인을 하지 않아도 구할 수 있는 경우가 있기 때문이다.     

예를 들어 다음과 같은 쿼리가 있다고 보자.        

```

SELECT COUNT(1) AS count 
	FROM Brand br
	JOIN Partner pt ON br.partner_id = pt.id
```

이 경우 굳이 Brand의 카운터를 가져오는데 Partner 테이블과 조인할 이유가 있을까?? 특히 LEFT OUTER JOIN이 걸려있다면??     

그냥     

```
SELECT COUNT(1) AS count 
	FROM Brand
```
와 다를 바가 없다. 성능을 고려해 본다면 당연히 후자의 쿼리가 좋다.    


이 이야기를 하는 이유는 이것이 한번에 페이징과 관련된 정보를 가져오는 이점을 활용하다보면 성능 저하를 가져오는 경우가 있다. 예를 들면 여러 개의 테이블이 조인된 상황이라도 실제 카운터는 조인을 하지 않고도 가져올 경우도 있기 때문이다.    

이렇다면 사실 카운터 쿼리를 따로 작성해서 날리는 것이 성능 이점을 가져올 수 있다. 따라서 이 메소드는 상황에 맞춰서 사용해야 하고 그렇지 않다면 카운터 쿼리를 따로 날리는게 실무에서 유리하다.    

## fetchCount();

그래서 이넘이 존재한다.    

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	long count = query.select(brand)
				         .from(brand)
	        				.fetchCount();

        	System.out.println("Total Count : " + count);
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
```

결과는 뭐...

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        count(brand) 
    from
        Brand brand */ select
            count(brand0_.br_code) as col_0_0_ 
        from
            basquiat_brand brand0_
Total Count : 6
```

지금까지 기본적인 조회 메소드를 알아봤다.

# WHERE CLAUSE using queryDSL   

실제 업무는 단순하지 않다. 원하는 정보를 얻기 위해서는 조건을 줘야 한다. 그렇지 않으면 수십만 건의 데이터를 가져올지도 모른다....     

## where()

sql에서 FROM절 이후에 조건을 검색하는 방식은 WHERE을 활용하는 것이다.     

그렇다면 sql에서는 어떤 방식인지 한번 예로 살펴보자.    

```
SELECT br_code,
	   br_name,
	   br_en_name,
	   test_number
	FROM brand
   WHERE br_code = ?
     AND br_name = ?
     AND (br_en_name = ? OR br_en_name = ?)
     AND number = ?
     AND number BETWEEN 10 AND 20
     AND number != ?
     AND br_name = ''
     AND br_name IS NULL
     AND br_name IS NOT NULL
     AND number IN ( 11, 12, 13)
     AND number NOT IN (20, 30)
     AND number >= 10
     AND number <= 20
     AND number > 10
     AND number < 20
     AND name LIKE '흥민%'
     AND name LIKE '%흥민%'
     AND name LIKE '%흥민'

```

위에 sql은 당연히 저렇게 작성하지 않는다. 다만 조건식을 한번 그냥 나열해 본 것이다.    

그럼 queryDSL에서는 저것을 어떻게 지원하는지 한번 살펴보자.

### eq()

단어에서 그냥 냄새가 난다. equal이라는 것을. 그러면 위에 AND 조건에서는 br_code = ?, number = ? 와 같을 것이라는 것을 말이다.

밑에 시나리오를 진행하기 전에 number 필드를 추가하고 generated-source 과정을 한번 거쳐야 한다. 그리고 테이블 생성후 임의의 숫자를 넣어서 다시 DB에 인서트해야한다.    


##### 시나리오    

1. 브랜드에서 number가 11인 row를 찾는다.     

2. br_name이 포데라인 녀석을 찾는 쿼리를 작성한다.        

3. 위의 1과 2의 조건을 AND로 검색한다.

현재 number가 11인 녀석은 에프베이스와 포데라이다.

우선 sql이라면?

```
1. SELECT * 
		FROM brand
	   WHERE number = 11

2. SELECT * 
		FROM brand
	   WHERE br_name = '포데라'

3. SELECT * 
		FROM brand
	   WHERE number = 11
	     AND br_name = '포데라'

```

위와 같을 것이다.   

그럼 Let's Get It~

```
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
        	List<Brand> brandList = query.select(brand)
					     		   .from(brand)
								   .where(brand.number.eq(11))
				        			   .fetch();
        	System.out.println("첫 번째 시나리오 start");
        	brandList.stream().map(s -> s.toString())
        					 .forEach(System.out::println);
        	System.out.println("첫 번째 시나리오 end");
        	
        	Brand fodera = query.select(brand)
						   .from(brand)
						   .where(brand.name.eq("포데라"))
			   			   .fetchOne();
        	System.out.println("두 번째 시나리오 start");
        	System.out.println(fodera.toString());
        	System.out.println("두 번째 시나리오 end");
        	
        	Brand what = query.select(brand)
						 .from(brand)
						 .where(brand.number.eq(11).and(brand.name.eq("포데라")))
			   			 .fetchOne();
			System.out.println("세 번째 시나리오 start");
			System.out.println(what.toString());
			System.out.println("세 번째 시나리오 end");
			
			Brand otherExpression = query.select(brand)
								 	   .from(brand)
									   .where(brand.number.eq(11),
											 brand.name.eq("포데라"))
						   			   .fetchOne();
			System.out.println("세 번째 시나리오는 다른 방식으로 start");
			System.out.println(otherExpression.toString());
			System.out.println("세 번째 시나리오는 다른 방식으로 end");
        	
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

```

표현 방식은 .where()안에 조건을 메소드 체이닝으로 하는 방식과 구분자 ','로 표현식을 나열하는 방식이 있다.    

사람마도 선호하는 방식이 다르니 어떤 걸 사용하지는 답이 없다.    

개인적으로는 메소드 체이닝 방식을 좋아하지만 유지보수, 조건 컴포넌트 조립등을 생각하면 후자의 방식을 좀 더 많이 사용하지 않을 까 싶다.        

자 암큰 결과 그럼 결과는?  

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number = ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number=?
첫 번째 시나리오 start
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
첫 번째 시나리오 end
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.name = ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_name=?
두 번째 시나리오 start
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
두 번째 시나리오 end
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number = ?1 
        and brand.name = ?2 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number=? 
            and brand0_.br_name=?
세 번째 시나리오 start
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
세 번째 시나리오 end
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number = ?1 
        and brand.name = ?2 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number=? 
            and brand0_.br_name=?
세 번째 시나리오는 다른 방식으로 start
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
세 번째 시나리오는 다른 방식으로 end
```

'어 그런데 br_name != ? 을 해볼려고 하니 notEq()같은게 없어요.'     

당연히 queryDSL에서는 그것도 지원한다. 어떻게 하면 될까?    

```
List<Brand> brandList = query.select(brand)
			       		   .from(brand)
						   .where(brand.enName.ne("Fodera")) // or .where(brand.enName.eq("Fodera").not())
						   .fetch();
System.out.println("like notEq()???");
brandList.stream().map(s -> s.toString())
		 		  .forEach(System.out::println);
```

ne()를 쓰면 된다. 또는 기존 코드에서 eq("Fodera").not()을 붙여도 된다. 결과는 똑같다.     

참고로 ne는 not equal의 줄임말이다.    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        not brand.enName = ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name<>?
like notEq()???
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

어라? 근데 != 아니고 <>다.     

둘 다 같은 기능을 하지만 사실 != 표현식은 sql에서 스탠다드 표현식은 아니다. 그래서 스택오버플로우나 몇몇 커뮤니티에서는 != 보다는 <>을 쓰는 것을 권장한다.    

근데 성능상에 차이가 있나? 이건 모르겠다. 단지 ANSI 스탠다드냐 아니냐의 차이인데 이런 표현식을 지원한다면 문제가 될까?      

이건 좀 의문이다. 나는 그냥 != 이 표현식이 좋다.     

### or()

sql에서는 (br_en_name = ? OR br_en_name = ?) 이런 표현식이 될것이다.    

역시 코드로 확인해 보자.    

```
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
        	List<Brand> brandList = query.select(brand)
									     .from(brand)
									     .where(brand.enName.eq("FBass").or(brand.enName.eq("Fender")))
				        				 .fetch();
        	brandList.stream().map(s -> s.toString())
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
```

쿼리는 어떻게 날아가나 확인해 보자.

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName = ?1 
        or brand.enName = ?2 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name=? 
            or brand0_.br_en_name=?
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

### isNull(), isNotNull(), isEmpty(), isNotEmpty()

가령 br_name IS NULL, br_name IS NOT NULL같은 것을 조회할 필요가 있으면 사용할 수 있다.    

다만 스트링, varchar로 된 컬럼의 경우에는 null과 빈 공백은 엄연히 다르다.   

DBeaver나 heidSql, workbench같은 툴을 사용해서 조회를 하다보면 컬럼에 null이라고 표시되는 경우도 있고 아닌 경우도 있다.    

아닌 경우라면 보통 ''이렇게 빈 공백이 들어간 경우이다. 이것은 null이 아니다. 그래서 null인 녀석을 조회하면 이 경우에는 데이터에 포함되지 않는다.     

그래서 스트링 타입의 경우에는 isEmtpy(), isNotEmpty()를 제공하는데 실제로 쿼리가 날아가는 것도 다르다.    

일단 코드로 확인해 보자.

```
List<Brand> brandIsNullList = query.select(brand)
						     	.from(brand)
						     	.where(brand.enName.isNull())
	        				 		.fetch();
System.out.println("isNull() start");
brandIsNullList.stream().map(s -> s.toString())
			  		  .forEach(System.out::println);

System.out.println("isNotNull() start");
List<Brand> brandIsNotNullList = query.select(brand)
							       .from(brand)
							       .where(brand.enName.isNotNull())
								   .fetch();
brandIsNotNullList.stream().map(s -> s.toString())
			  		   	 .forEach(System.out::println);

System.out.println("isEmpty() start");
List<Brand> brandIsEmptyList = query.select(brand)
								 .from(brand)
								 .where(brand.enName.isEmpty())
								 .fetch();
brandIsEmptyList.stream().map(s -> s.toString())
				  	   .forEach(System.out::println);

System.out.println("isNotEmpty() start");
List<Brand> brandIsNotEmptyList = query.select(brand)
								    .from(brand)
								    .where(brand.enName.isNotEmpty())
								    .fetch();
brandIsNotEmptyList.stream().map(s -> s.toString())
			  		      .forEach(System.out::println);
```

날아가는 쿼리는?    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName is null */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name is null
isNull() start
isNotNull() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName is not null */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name is not null
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
isEmpty() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        length(brand.enName) = 0 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            length(brand0_.br_en_name)=0
isNotEmpty() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        not length(brand.enName) = 0 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            length(brand0_.br_en_name)<>0
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)

```

empty관련해서는 length를 구해서 0이냐 0이 아니냐로 따진다.    

조건을 줄 때 고려해 볼만한 부분이다.    

### in(), notIn()

여러 개의 값을 묶어서 조회하는 경우에 사용한다.

코드로 살펴보자.

```
System.out.println("queryDSL로 뭔가 하기 직전!!!");
List<Brand> brandInList = query.select(brand)
						     .from(brand)
						     .where(brand.number.in(10, 11, 12))
						     .fetch();
System.out.println("in() start");
brandInList.stream().map(s -> s.toString())
			  	  .forEach(System.out::println);

System.out.println("notIn() start");
List<Brand> brandNotInList = query.select(brand)
						  	   .from(brand)
							   .where(brand.number.notIn(11, 12))
							   .fetch();
brandNotInList.stream().map(s -> s.toString())
			  		 .forEach(System.out::println);
```

쿼리는?    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number in (
            ?1
        ) */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number in (
                ? , ? , ?
            )
in() start
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
notIn() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number not in (
            ?1
        ) */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number not in  (
                ? , ?
            )
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

### between()    

특정 값 사이의 정보를 구해올 때 사용한다. 보통은 int 타입의 컬럼이나 날짜 관련해서 start, end날짜를 기준으로 그 사이에 등록된 상품을 가져온다던가 할 때 많이 보게 되는 쿼리다. 스트링 타입도 아마 조회할 수 있는 듯 싶다.    

여기서는 number가 10, 20사이인 브랜드만 가져와보자.    

```
List<Brand> brandBetweenList = query.select(brand)
					       		 .from(brand)
								 .where(brand.number.between(10, 20))
								 .fetch();
System.out.println("between() start");
brandBetweenList.stream().map(s -> s.toString())
			  		   .forEach(System.out::println);
```

쿼리를 살펴보자.    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number between ?1 and ?2 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number between ? and ?
between() start
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

총 6개의 정보중 4개만 나왔다. 범위를 지정해서 쿼리를 하고 싶은 때 사용하면 된다.    

### goe(), gt(), loe(), lt()    

개인적으로 가장 헛깔리는 부분이다.

1. goe -> greater or equal  : >=, 우측보다 크거나 같은 경우         
2. gt  -> greater than 		: >,  우측보다 큰 경우      
3. loe -> less or equal 		: <=, 우측보다 작거나 같은 경우    
4. lt  -> less than			: <,  우측보다 작은 경우    

자 그럼 한번씩 확인해 보는 시간을 갖자.    

```
List<Brand> brandGoeList = query.select(brand)
				       		 .from(brand)
				       		 .where(brand.number.goe(10)) // 10보다 크거나 같은 
				       		 .fetch();
System.out.println("goe() start");
brandGoeList.stream().map(s -> s.toString())
		 		   .forEach(System.out::println);

List<Brand> brandGtList = query.select(brand)
				       		.from(brand)
				       		.where(brand.number.gt(10)) // 10보다 큰 
				       		.fetch();
System.out.println("gt() start");
brandGtList.stream().map(s -> s.toString())
		  	 	  .forEach(System.out::println);

List<Brand> brandLoeList = query.select(brand)
				 			 .from(brand)
							 .where(brand.number.loe(10)) // 10보다 작거나 같은 
							 .fetch();
System.out.println("goe() start");
brandLoeList.stream().map(s -> s.toString())
				   .forEach(System.out::println);

List<Brand> brandLtList = query.select(brand)
				     		.from(brand)
				       		.where(brand.number.lt(10)) // 10보다 작은 
				       		.fetch();
System.out.println("gt() start");
brandLtList.stream().map(s -> s.toString())
	 			  .forEach(System.out::println);
```

결과는 한번씩 확인~    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number >= ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number>=?
goe() start
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number>?
gt() start
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number <= ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number<=?
goe() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number < ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number<?
gt() start
```

조건에 따른 쿼리가 어떻게 만들어져서 나가는지 한번씩 확인하는 것은 필수이다.    

### like(), startsWith(), endsWith(), contains()      

like 검색과 관련해서 다음과 같은 특징을 알아야 한다.    

1. LIKE '?%'  : 특정 문자열로 시작하는 데이터를 검색한다.    

2. LIKE '%?'  : 특정 문자열로 끝나는 데이터를 검색한다.    

3. LIKE '%?%' : 특정 문자열이 포함된 테이터를 검색한다.    

일단 queryDSL의 경우에는 위에 API를 사용할 때 주의를 해야 하는 것이 있는데 그것은 like()인 경우이다.

위에 언급했던 방식을 적용하기 위해서 like() API를 사용한다면 원하는 방식에 따라서 문자열에 %를 붙여줘야 한다.

예를 들면 

```
List<Brand> brandLikeList = query.select(brand)
			       		 	  .from(brand)
			       		 	  .where(brand.enName.like("end%")) // end 문자열로 시작하는 데이터 검색 
			       		 	  .fetch();
			       		 	  
또는 			       		 	  
			       		 	  
List<Brand> brandLikeList = query.select(brand)
			       		 	  .from(brand)
			       		 	  .where(brand.enName.like("%end")) // end 문자열로 끝나는 데이터 검색 
			       		 	  .fetch();

또는 
			       		 	  
List<Brand> brandLikeList = query.select(brand)
			       		 	  .from(brand)
			       		 	  .where(brand.enName.like("%end%")) // end 문자열이 포함된 데이터 검색 
			       		 	  .fetch();
```

하지만 저런 방식이 번거로울 경우 다음과 같은 방식을 사용할 수 있다.     

1. .where(brand.enName.like("end%")) -> .where(brand.enName.startsWith("end")    

2. .where(brand.enName.like("%end")) -> .where(brand.enName.endsWith("end")    

3. .where(brand.enName.like("%end%")) -> .where(brand.enName.contains("end"))     

이렇게 대처할 수 있다.     

또한 DB에 따라 case sensitivity, 즉 대소문자를 구분하는 경우가 있다. 이런 경우에는 문자열로 'FEnder'라면 'end'로 검색되지 않는다.        

왜냐하면 End와 end는 다르다고 보기 때문이다.     

일반적으로 mySql을 그냥 깔면 보통 대소문자 구분없이 찾아지지만 이런 부분에 대해 설정이 되어 있으면 대소문자 구분없이 찾고 싶은 경우가 생길 것이다.    

그래서 해당 API는 IgnoreCase가 붙은 API즉, likeIgnoreCase(), startsWithIgnoreCase(), endsWithIgnoreCase(), containsIgnoreCase() 같은 API도 제공한다.    

```
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
```

다음과 같이 코드를 짜고 실행해 보면 현재 Fender라는 영문 브랜드명이 존재하기 때문에 위에서는 contains()로 검색했을 경우만 나올 것이다.

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName like ?1 escape '!' */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name like ? escape '!'
like() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName like ?1 escape '!' */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name like ? escape '!'
startsWith() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName like ?1 escape '!' */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name like ? escape '!'
endsWith() start
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName like ?1 escape '!' */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name like ? escape '!'
contains() start
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

SQL의 WHERE절 문법을 queryDSL에서 어떻게 활용하는지 알아보았다.
