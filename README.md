# Projections 

### 시작하기 전에     

지금까지 우리가 테스트해오면서 눈썰미 좋으신 분들은 .select()에서 어떻게 코딩을 하느냐에 따라 반환 타입이 달라지는 것을 눈치채셨을 것이다.     

또한 마지막으로 fetch나 fetchOne같이 결과를 가져오는 방식에 따라서도 collections이나 단일 타입으로 가져오는 것을 알 수 있다.      

```
package io.basquiat;

import static io.basquiat.model.QProduct.product;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Product;

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
	        	
	        	List<String> brandNames = query.select(product.brandName)
										 .from(product)
										 .fetch();
	        	
	        	List<Tuple> tuple = query.select(
	        									 product.brandName,
	        									 product.model
	        							         )
								   .from(product)
								   .fetch();
	        	
	        	Tuple tuple = query.select(
									 product.brandName,
									 product.model
							         )
						   	 .from(product)
						   	 .fetchFirst();
	        	
	        	String brandNameOne = query.select(product.brandName)
								   	 .from(product)
								   	 .limit(1)
								   	 .fetchOne();

         	String brandNameFirst = query.select(product.brandName)
									   .from(product)
									   .fetchFirst();
         	
         	List<Product> productList = query.select(product)
										  .from(product)
										  .fetch();

         	Product singleProduct = query.select(product)
									   .from(product)
									   .fetchFirst();
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
위와 같이 프로젝션에 대해서 어떻게 코딩을 하느냐에 따라서 반환 타입이 전부 달라진다.     

이것은 .select()와 .fetch()관련 메소드를 따라가보면 select의 경우에는 parameter spread syntax에 따라서 단일인지 여러개가 들어오는 배열 타입인지 체크하고 타입을 추론하기 때문이고 fetch에서는 각 메소드에 따라 반환 타입을 결정하기 때문이다.     

그래서 친절하게 IDE에서는 코드레벨에서 자동으로 캐스팅을 할 수 있다. 또한 반환 타입이 맞지 않으면 씨뻘겋게 밑줄 그어주시니 눈에도 확 들어온다.     

근데 위에서 보면 객체 자체를 반환하는 경우라면 그나마 낫다. ~~하지만 조회 결과는 DTO로 반환하는게 어떨까?~~     

하지만 단일 타입이거나 튜플의 형식일 경우에는?    

그리고 대부분은 객체보다는 필요한 정보를 가져오기 위해서 객체 자체를 조회하기 보다는 컬럼 단위, 즉 프로젝션으로 조회하는 경우가 더 많다.     

자 그렇다면 우리는 튜플로 반환하는 경우에는 우리가 원하는 정보를 어떻게 끄집어 내는지 코드를 한번 살펴보자.     

```
List<Tuple> tuple = query.select(
							 product.brandName,
							 product.model
					         )
					   .from(product)
					   .fetch();
tuple.stream().forEach(t -> {
	String brandName = t.get(product.brandName);
	String model = t.get(product.model);
	int price = t.get(product.price);
	System.out.println(brandName + " :: " + model + " :: " + price);
});
```

결과?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.brandName,
        product.model 
    from
        Product product */ select
            product0_.brand_name as col_0_0_,
            product0_.model as col_1_0_ 
        from
            basquiat_product product0_
샌드버그 :: California TT 5 Masterpiece
샌드버그 :: California VS 5 Masterpiece
펜더 :: Custom Shop 63 Journeyman
펜더 :: Custom Shop 63 Journeyman
펜더 :: Custom Shop Stratocaster
펜더 :: Custom Shop telecaster
펜더 :: Elite 4
펜더 :: Elite 5
```

PreparedStatement와 ResultSet조합으로 get으로 가져오는 방식이랑 너무 닮았다. ~~너무 시러~~

```
Connection conn = null;
PreparedStatement stmt = null;
ResultSet rs = null;
try {
    conn = DataSource.getConnection();
    String sql = "SELECT brandName, model, price FROM basquiat_product";
    stmt = conn.prepareStatement(sql);
    rs = stmt.executeQuery();
    while(rs.next()) {
       System.out.println(rs.getString("brandName"));
       System.out.println(rs.getString("model"));
       System.out.println(rs.getInt("price"));
    }
    rs.close();
    stmt.close();
    conn.close();
} catch (Exception e) {
   e.printStackTrace();
}    
```

'허허... 10년전에 줄기차게 사용하던 그것이로구나.'    

다만 차이점이라면 ResultSet은 내가 꺼내는 값이 타입이 뭔지 알아야 하지만 queryDSL에서는 타입이 맞지 않으면 오류를 보여주고 IDE에서 자동으로 캐스팅을 해준다.    

그런데 어째든 그럼 저렇게 get으로 일일이 꺼내서 DTO에 set을 해준다? ~~어↗림도 없지↗!!!~~     

자 그럼 JPA에서 JPQL을 DTO에 매핑해서 사용하는 방법을 먼저 알아보자. ~~보채지 말아요. completedJPA에서도 느리지만 업데이트할거에요~~     

[Basic queryDSL](https://github.com/basquiat78/jpa-with-querydsl/tree/1.query-dsl-basic)     

처음 JPA의 JPQL, criteria와 비교했던 브랜치에서 사용했던 코드를 Product에 맞춰서 수정했다.     

```
package io.basquiat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

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
        	
        	@SuppressWarnings("rawtypes")
        	List productList = em.createQuery("SELECT p.brandName, p.model, p.price FROM Product p")
        						  .getResultList();

        	for(Object object : productList) {
    			Object[] results = (Object[]) object;
    			System.out.println("One Row start");
    			for(Object result : results) {
    				System.out.println(result);
    			}
    			System.out.println("One Row end");
    		}
        	
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

결과는?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* SELECT
        p.brandName,
        p.model,
        p.price 
    FROM
        Product p */ select
            product0_.brand_name as col_0_0_,
            product0_.model as col_1_0_,
            product0_.price as col_2_0_ 
        from
            basquiat_product product0_
One Row start
펜더
Custom Shop 63 Journeyman
5400000
One Row end
One Row start
샌드버그
California TT 5 Masterpiece
5400000
One Row end
One Row start
펜더
Elite 5
3200000
One Row end
One Row start
펜더
Elite 4
3000000
One Row end
One Row start
펜더
Custom Shop telecaster
6400000
One Row end
One Row start
샌드버그
California VS 5 Masterpiece
5400000
One Row end
One Row start
펜더
Custom Shop Stratocaster
6700000
One Row end
One Row start
펜더
Custom Shop 63 Journeyman
4400000
One Row end
```

이전 브랜치에서는 DTO와 매핑해서 가져올 수 있는 방법이 있다고 그냥 언급만 했는데 그럼 이제 앞으로도 queryDSL에서도 사용할 예정이기 때문에 DTO를 한번 만들어 보자.    

```
package io.basquiat.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
}
```
자 그리고 우리는 이 DTO에 매핑을 하는 코드를 짜 볼것이다.     

```
package io.basquiat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.dto.ProductDTO;

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
        	
        	List<ProductDTO> productList = em.createQuery("SELECT new io.basquiat.dto.ProductDTO(p.id, p.name, p.price, p.brandName, p.model, p.color) FROM Product p", ProductDTO.class)
        						  		  .getResultList();

        	System.out.println(productList);
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
뭔가 좀 길어서 슬픈 코드가 보인다.     

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* SELECT
        new io.basquiat.dto.ProductDTO(p.id,
        p.name,
        p.price,
        p.brandName,
        p.model,
        p.color) 
    FROM
        Product p */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_ 
        from
            basquiat_product product0_
[
	ProductDTO(id=1, name=Bass Guitar, price=5400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red), 
	ProductDTO(id=2, name=Bass Guitar, price=5400000, brandName=샌드버그, model=California TT 5 Masterpiece, color=Twotone Sunburst), 
	ProductDTO(id=3, name=Bass Guitar, price=3200000, brandName=펜더, model=Elite 5, color=Candy Apple Red), 
	ProductDTO(id=4, name=Bass Guitar, price=3000000, brandName=펜더, model=Elite 4, color=Candy Apple Red), 
	ProductDTO(id=5, name=Electric Guitar, price=6400000, brandName=펜더, model=Custom Shop telecaster, color=Scotch Cream Butter), 
	ProductDTO(id=6, name=Bass Guitar, price=5400000, brandName=샌드버그, model=California VS 5 Masterpiece, color=Balck Sunburst), 
	ProductDTO(id=7, name=Electric Guitar, price=6700000, brandName=펜더, model=Custom Shop Stratocaster, color=Daphne Blue), 
	ProductDTO(id=8, name=Bass Guitar, price=4400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Shell Pink)
]
```
호? 좋은데?     

하지만 보시다시피 매핑할 DTO을 사용하는 방식이 참 귀찮다. 패키지 경로를 모두 써야 하는 단점이 있고 이렇다 보니 뭔가 복잡한 쿼리를 작성할 때는 걸리적거린다.    

자 그럼 이제 여러분이 궁금한 것이 이것일 것이다.    

'그래요. 알았어요. 그럼 queryDSL은 좀 편한가요? 그렇다면 빨리 queryDSL로 어케 하는지나 알려줘요.'     

현재 queryDSL의 공식홈페이지에서는 4.1.3까지에 대한 공식 도규먼트를 제공한다. ~~일해라! 정기석!! 아..아니다 그래도 음반도 냈잖아? 삼촌도 찾고~~     

[Projections](http://www.querydsl.com/static/querydsl/4.1.3/reference/html_single/#d0e2187)

여기에서는 단락으로는 2가지 방식을 소개하지만 내부적인 내용을 살펴보면 총 4가지가 존재한다.     

하나씩 알아보자.   

## Projections using Bean population     

### 1. using .bean()     
이것은 DTO의 setter를 이용해서 매핑하는 방식과 field를 직접 매핑하는 방식이다.     

여기에는 주의할 점이 있는데 일단 코드를 먼저 살펴보자.    

```
List<ProductDTO> productList = query.select(Projections.bean(ProductDTO.class, product.name, product.price))
								 .from(product)
								 .fetch();
    	
System.out.println(productList);
```
일단 우리가 위에서 JPA에서 JPQL과 DTO매핑을 위해 생성했던 ProductDTO를 그대로 쓰면 에러가 난다. 왜냐하면 Immutable하게 만들기 위해 사용한 롬복의 @Value때문인데 이 녀석의 특징은 내부적으로 getter만 만들기 때문에 setter가 없어서 에러가 난다. 또한 이 녀석의 경우에는 빈 생성자를 생성하지 않고 내부적으로 생성해도 에러가 발생한다.     

그리고 이 방법은 빈 생성자가 있어야 에러가 발생하지 않는다.     

따라서 다음과 같이 ProductDTO에서 롬복의 어노테이션을 좀 수정하자.    

```
package io.basquiat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
}
```
그리고 위에서 작성한 코드를 다시 실행하면 

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.name,
        product.price 
    from
        Product product */ select
            product0_.product_name as col_0_0_,
            product0_.price as col_1_0_ 
        from
            basquiat_product product0_
[
	ProductDTO(id=null, name=Bass Guitar, price=5400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=5400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=3200000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=3000000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Electric Guitar, price=6400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=5400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Electric Guitar, price=6700000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=4400000, brandName=null, model=null, color=null)
]
```

### 2. using .fields()

이 방법은 직접 필드와 매핑을 하는 방법이다. 다만 특이한 점은 setter가 없어도 가능하다.    

```
package io.basquiat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
}
```
다음과 같이 수정하고  

```
List<ProductDTO> productList = query.select(Projections.fields(ProductDTO.class, product.name, product.price))
								 .from(product)
								 .fetch();

System.out.println(productList);
```
이렇게 코드를 작성해서 실행해 보자.

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.name,
        product.price 
    from
        Product product */ select
            product0_.product_name as col_0_0_,
            product0_.price as col_1_0_ 
        from
            basquiat_product product0_
[
	ProductDTO(id=null, name=Bass Guitar, price=5400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=5400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=3200000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=3000000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Electric Guitar, price=6400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=5400000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Electric Guitar, price=6700000, brandName=null, model=null, color=null), 
	ProductDTO(id=null, name=Bass Guitar, price=4400000, brandName=null, model=null, color=null)
]
```
결과가 잘 나온다.     

## Projections using Constructor usage

### 1. using .constructor()    

말 그대로 생성자를 통해서 만든다.

그래서 Product의 어노테이션도 좀 변경해야 한다. 

```
package io.basquiat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
}
```

그리고 다음과 같이 코딩을 하면 된다.     

```
List<ProductDTO> productList = query.select(Projections.constructor(
												ProductDTO.class, 
												product.id,
												product.name, 
												product.price,
												product.brandName,
												product.model,
												product.color
												
										))
								.from(product)
								.fetch();
```
결과는 뭐....

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color 
    from
        Product product */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_ 
        from
            basquiat_product product0_
[
	ProductDTO(id=1, name=Bass Guitar, price=5400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red), 
	ProductDTO(id=2, name=Bass Guitar, price=5400000, brandName=샌드버그, model=California TT 5 Masterpiece, color=Twotone Sunburst), 
	ProductDTO(id=3, name=Bass Guitar, price=3200000, brandName=펜더, model=Elite 5, color=Candy Apple Red), 
	ProductDTO(id=4, name=Bass Guitar, price=3000000, brandName=펜더, model=Elite 4, color=Candy Apple Red), 
	ProductDTO(id=5, name=Electric Guitar, price=6400000, brandName=펜더, model=Custom Shop telecaster, color=Scotch Cream Butter), 
	ProductDTO(id=6, name=Bass Guitar, price=5400000, brandName=샌드버그, model=California VS 5 Masterpiece, color=Balck Sunburst), 
	ProductDTO(id=7, name=Electric Guitar, price=6700000, brandName=펜더, model=Custom Shop Stratocaster, color=Daphne Blue), 
	ProductDTO(id=8, name=Bass Guitar, price=4400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Shell Pink)
]
```
하지만 이 경우에는 좀 제한적인 것이 있다. 만일 실제로 셀렉트 해오는 정보가 DTO전체의 컬럼을 다 가져온다면 사용할 만 하지만 1,2개의 컬럼 정보만 가져와서 이 방식으로 DTO에 매핑한다면 좀 애로사항이 많다.     

생성자만큼 담아야 하기 때문에 해당 생성자의 갯수와 타입이 맞지 않으면 일단 에러가 난다. 또한 그에 맞춰서 해당 파라미터에 null을 세팅할려니 null 세팅이 현재 되지 않는다.     

그래서 이런 경우에는 위에 방식을 사용하자.     

물론 Expressions.constant("")이렇게 해서 빈 공백을 넣을 수는 있다.    

이와 관련된 깃헙 이슈가 있는데 아직까지도 해결안된듯...   

[NullExpressions Issue](https://github.com/querydsl/querydsl/issues/2049)     

그리고 더 큰 문제는 생성자의 파라미터 숫자와 실제로 세팅하는 숫자가 맞지 않아도 에러를 발생시키지 않는다.     

결국 생성자가 많고 매핑할 컬럼이 많으면 실수가 나올 수 있는 여지가 너무나 많다. 그래서 이런 경우 런타임에러가 발생하기 전까지는 오류를 알 수 없다.     

결국 잠재적인 오류를 생성할 소지가 많다.     

물론 생성자가 적으면 상관없겠지만 말이다.     

### 2. using @QueryProjection

이 경우에는 두가지 사용방식이 있다.    

그중에 하나는 DTO의 생성자에 해당 어노테이션을 붙여서 사용하는 방법과 엔티티의 생성자에 붙여서 사용하는 방식이다.    

#### 2.1. DTO using @QueryProjection

Product

```
package io.basquiat.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ProductDTO {
	
	@QueryProjection
	public ProductDTO(Long id, String name, int price, String brandName, String model, String color) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.brandName = brandName;
		this.model = model;
		this.color = color;
	}

	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
}
```
이때는 @AllArgsConstructor을 사용할 수 없다. 

```
/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.core.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for APT based query type generation. Annotate constructors with this annotation.
 *
 * <p>Example</p>
 *
 * <pre>
 * class UserInfo {
 *
 *     private String firstName, lastName;
 *
 *     {@code @QueryProjection}
 *     public UserInfo(String firstName, String lastName) {
 *         this.firstName = firstName;
 *         this.lastName = lastName;
 *     }
 *
 *     // getters and setters
 * }
 * </pre>
 *
 * <p>The projection can then be used like this</p>
 *
 * <pre>
 * {@code
 * QUser user = QUser.user;
 * List <UserInfo> result = querydsl.from(user)
 *     .where(user.valid.eq(true))
 *     .select(new QUserInfo(user.firstName, user.lastName))
 *     .fetch();
 * }
 * </pre>
 */
@Documented
@Target(ElementType.CONSTRUCTOR)
@Retention(RUNTIME)
public @interface QueryProjection {

}
```
타겟이 생성자에 위치하는 어노테이션이기 때문이다.    

근데 특이한게 이 어노테이션을 다는 순간 ~~메이븐이 자동으로 젠을 해주는걸 이전 브랜치에서 알았...~~ target/generated-sources/java폴더 내에 QProductDTO.java가 생성된 것이 보였다.     

혹시 그럼??????     

그럼 코드는 어떻게 바뀔까?    

```
package io.basquiat.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * io.basquiat.dto.QProductDTO is a Querydsl Projection type for ProductDTO
 */
@Generated("com.querydsl.codegen.ProjectionSerializer")
public class QProductDTO extends ConstructorExpression<ProductDTO> {

    private static final long serialVersionUID = 1898996289L;

    public QProductDTO(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<Integer> price, com.querydsl.core.types.Expression<String> brandName, com.querydsl.core.types.Expression<String> model, com.querydsl.core.types.Expression<String> color) {
        super(ProductDTO.class, new Class<?>[]{long.class, String.class, int.class, String.class, String.class, String.class}, id, name, price, brandName, model, color);
    }

}
```
코드를 보는 순간 느낌이 온다.     

그럼 이제 실제로 코드로 한번 구현해 보자.     

```
package io.basquiat;

import static io.basquiat.model.QProduct.product;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.dto.ProductDTO;
import io.basquiat.dto.QProductDTO;

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

        	List<ProductDTO> productList = query.select(new QProductDTO(
        														product.id,
        														product.name, 
        														product.price,
        														product.brandName,
        														product.model,
        														product.color
        														))
        										.from(product)
        										.fetch();
        	
        	System.out.println(productList);
        	
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
오호라? 근데 이게 참 좋은게 뭐냐면 한번 코드에서 뭔가를 하나 지우거나 타입이 맞지 않으면 에러가 발생한다는 것이다.     

또한 QProductDTO에서 자동완성 기능을 사용하면 어떤 파라미터가 들어가야 하는지 알 수 있다는 점이다.     

즉, 자동완성 기능을 통해 생성자의 순서와 타입을 알 수 있고 그 생성자에 맞춰서 코딩을 할 수 있다.     

하지만 눈썰미가 좋은 분들은 이런 생각이 들것이다.     

'음....보통 POJO타입의 DTO는 사실 어떤 프레임워크나 라이브러리에 종속되지 않기 위해서 사용하는데 그런 DTO에 @QueryProjection이라니.....'      

이 의문대로라면 이 DTO는 @QueryProjection가 붙는 순간 queryDSL에 종속적인 DTO가 되기 때문에 POJO가 아니다라고 말할 수 있을지 모른다.     

```
그럼 특정 기술규약과 환경에 종속되지 않으면 모두 POJO라고 말할 수 있는가? 
진정한 POJO란 객체지향적인 원리에 충실하면서, 환경과 기술에 종속되지 않고 필요에 따라 재활용될 수 있는 방식으로 설계된 오브젝트를 말한다.
```
내가 한 말은 아니고..... 토비의 스프링에서 언급된 POJO에 대한 이야기중 공감할 만한 부분만 가져온 글이다.     

사실 DTO라는게 여기저기에서 사용될 소지가 다분하다. 하지만 그런거 신경쓰지 않는다면 이렇게 쓰는 방식이 아마도 가장 최선의 선택이 아닐까 싶다.     

물론 @QueryProjection이 DTO에 들어오는게 좀 거시기하다면 위의 bean을 활용한 방법도 고려해보면 좋을 것이다.  

#### 2.2. SELF ENTITY using @QueryProjection

이 방법은 ENTITY자체로 반환하는 방식이다. 사실 '엔티티를 반환하지 말고 DTO를 통해서 반환해라'라는 것에는 좀 반하는 방식이긴 하지만 어쨰든 이것을 지원하니 이것도 한번 살펴보자.    

Product

```
package io.basquiat.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "basquiat_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Product {

	@QueryProjection
	public Product(Long id, String name, int price, String brandName, String model, String color, 
				   LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.brandName = brandName;
		this.model = model;
		this.color = color;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	@Builder
	public Product(String name, int price, String brandName, String model, String color) {
		super();
		this.name = name;
		this.price = price;
		this.brandName = brandName;
		this.model = model;
		this.color = color;
	}

	/** 생산품 고유 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 생산품 명 */
	@Column(name = "product_name")
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	@Column(name = "brand_name")
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void onCreatedAt() {
    	createdAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdatedAt() {
    	updatedAt = LocalDateTime.now();
    }
	
}
```
일단 Product의 경우는 기본키 매핑을 Identity로 잡았기 때문에 빌드 패턴을 위한 생성자에서는 id를 받지 않기 때문에 조회용을 위한 생성자를 새로 만들었다.   

근데 이전 브랜치에서는 이 id를 String으로 잡았는데 잘 작동하는게 좀 의문이긴 하지만 그래서 Long으로 바꿨다.     

눈치 좋으신 분들은 이미 바꾸셨을지도....      

아무튼 그냥 간단하게 해당 어노테이션을 생성자에 붙이면 된다.    

그럼 QProduct.java에 기존에 없던 다음과 같은 코드가 생성된다.    

```
public static ConstructorExpression<Product> create(Expression<Long> id, Expression<String> name, Expression<Integer> price, Expression<String> brandName, Expression<String> model, Expression<String> color, Expression<java.time.LocalDateTime> createdAt, Expression<java.time.LocalDateTime> updatedAt) {
        return Projections.constructor(Product.class, new Class<?>[]{long.class, String.class, int.class, String.class, String.class, String.class, java.time.LocalDateTime.class, java.time.LocalDateTime.class}, id, name, price, brandName, model, color, createdAt, updatedAt);
    }
```
오! QProductDTO.java에 생성되었던 것과 비슷하게 생긴 녀석이 생겨버렸다.     

근데 코드를 보면 결국 .constructor()로 매핑하는 방식이다.     

이 경우에는 코드가 살짝 다르다.    

```
        	List<Product> productList = query.select(QProduct.create(
    														product.id,
    														product.name, 
    														product.price,
    														product.brandName,
    														product.model,
    														product.color,
    														product.createdAt,
    														product.updatedAt
    														))
    									   .from(product)
    									   .fetch();
```
실행 결과는?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color,
        product.createdAt,
        product.updatedAt 
    from
        Product product */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_,
            product0_.created_at as col_6_0_,
            product0_.updated_at as col_7_0_ 
        from
            basquiat_product product0_
[
	Product(id=1, name=Bass Guitar, price=5400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=2, name=Bass Guitar, price=5400000, brandName=샌드버그, model=California TT 5 Masterpiece, color=Twotone Sunburst, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=3, name=Bass Guitar, price=3200000, brandName=펜더, model=Elite 5, color=Candy Apple Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=4, name=Bass Guitar, price=3000000, brandName=펜더, model=Elite 4, color=Candy Apple Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=5, name=Electric Guitar, price=6400000, brandName=펜더, model=Custom Shop telecaster, color=Scotch Cream Butter, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=6, name=Bass Guitar, price=5400000, brandName=샌드버그, model=California VS 5 Masterpiece, color=Balck Sunburst, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=7, name=Electric Guitar, price=6700000, brandName=펜더, model=Custom Shop Stratocaster, color=Daphne Blue, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41), 
	
	Product(id=8, name=Bass Guitar, price=4400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Shell Pink, createdAt=2020-07-22T11:19:50, updatedAt=2020-07-22T11:19:50)]
```
하지만 이 경우에는 반환된 타입이 Product임에도 불구하구 영속성 컨텍스트와는 무관하게 작동한다.    

지금까지 우리는 queryDSL로 조회한 경우에 대해서 영속성에 대해서 논한 적이 없다.     

그럼 다음과 같이 코드를 한번 짜보자.    

Product

```
package io.basquiat.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "basquiat_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Product {
	
	@QueryProjection
	public Product(Long id, String name, int price, String brandName, String model, String color,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.brandName = brandName;
		this.model = model;
		this.color = color;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	@Builder
	public Product(String name, int price, String brandName, String model, String color) {
		super();
		this.name = name;
		this.price = price;
		this.brandName = brandName;
		this.model = model;
		this.color = color;
	}

	/** 생산품 고유 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 생산품 명 */
	@Column(name = "product_name")
	private String name;
	
	/** 생산품 가격 */
	private int price;
	
	/**
	 * 가격을 바꾼다.
	 * @param price
	 */
	public void changePrice(int price) {
		this.price = price;
	}
	
	/** 브랜드 명 */
	@Column(name = "brand_name")
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void onCreatedAt() {
    	createdAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdatedAt() {
    	updatedAt = LocalDateTime.now();
    }
    
}
```
Product에서 가격을 변경하는 코드를 추가했다.    

그리고 다음과 같이 실행을 해보자.     

```
JPAQueryFactory query = new JPAQueryFactory(em);
System.out.println("queryDSL로 뭔가 하기 직전!!!");

Product selectedProduct = query.select(product)
						   .from(product)
						   .fetchFirst();
selectedProduct.changePrice(5200000);
System.out.println(selectedProduct.toString());

tx.commit();
```
queryDSL은 결국 계속 언급해 왔지만 JPQL을 기반으로 한다. 위와 같이 select절에서 프로젝션을 객체 자체로 꺼내오게 되면 이 녀석은 영속성을 갖게 된다.

진짜 그런가?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product 
    from
        Product product */ select
            product0_.id as id1_2_,
            product0_.brand_name as brand_na2_2_,
            product0_.color as color3_2_,
            product0_.created_at as created_4_2_,
            product0_.model as model5_2_,
            product0_.product_name as product_6_2_,
            product0_.price as price7_2_,
            product0_.updated_at as updated_8_2_ 
        from
            basquiat_product product0_ limit ?
Product(id=1, name=Bass Guitar, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T09:03:17)
Hibernate: 
    /* update
        io.basquiat.model.Product */ update
            basquiat_product 
        set
            brand_name=?,
            color=?,
            created_at=?,
            model=?,
            product_name=?,
            price=?,
            updated_at=? 
        where
            id=?
```

원래 540만원짜리를 할인해서 530만으로 세팅했더니 dirty checking이 벌어졌다.    

JPQL에서도 반환타입을 엔티티로 반환하면 역시 마찬가지이다.     

그런데? 위에서 우리가 엔티티내부에 

```
@QueryProjection
public Product(Long id, String name, int price, String brandName, String model, String color,
		LocalDateTime createdAt, LocalDateTime updatedAt) {
	super();
	this.id = id;
	this.name = name;
	this.price = price;
	this.brandName = brandName;
	this.model = model;
	this.color = color;
	this.createdAt = createdAt;
	this.updatedAt = updatedAt;
}
```
를 통해서 반환한 코드 

```
Product selectedProduct = query.select(QProduct.create(
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color,
											product.createdAt,
											product.updatedAt
											))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
selectedProduct.changePrice(5400000);
System.out.println(selectedProduct.toString());

System.out.println("dirty checking 시도후 다시 조회!");

Product againProduct = query.select(QProduct.create(
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color,
											product.createdAt,
											product.updatedAt
											))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(againProduct.toString());
```
이 코드를 실행하면 어떤 일이 벌어질까?     

일단 첫 번째 selectedProduct로 먼저 조회를 하고 아까 테스트하면서 530만원으로 변경한 것을 다시 540만원으로 변경을 하기 위해 dirty checking을 시도했다.      

하지만 실제 로그는??

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color,
        product.createdAt,
        product.updatedAt 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_,
            product0_.created_at as col_6_0_,
            product0_.updated_at as col_7_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
Product(id=1, name=Bass Guitar, price=5400000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:49:41)
dirty checking 시도후 다시 조회!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color,
        product.createdAt,
        product.updatedAt 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_,
            product0_.created_at as col_6_0_,
            product0_.updated_at as col_7_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
Product(id=1, name=Bass Guitar, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:49:41)
```
어???? dirty checking이 발생하지 않았다.

그래서 QProduct.create의 create메소드를 쭉 따라가보니

```
/**
 * Returns a fetch of transformers applicable to the given constructor.
 *
 * @param constructor constructor
 * @return transformers
 */
public static Iterable<Function<Object[], Object[]>> getTransformers(Constructor<?> constructor) {
    Iterable<ArgumentTransformer> transformers = Lists.newArrayList(
            new PrimitiveAwareVarArgsTransformer(constructor),
            new PrimitiveTransformer(constructor),
            new VarArgsTransformer(constructor));

    return ImmutableList
            .<Function<Object[], Object[]>>copyOf(filter(transformers, applicableFilter));
}
```
마주하게 되는 getTransformers메소드를 만나게 된다.      

어라? 근데 ImmutableList라니?     

그래서 다음과 같이 Product에서 

```
//@QueryProjection
public Product(Long id, String name, int price, String brandName, String model, String color,
		LocalDateTime createdAt, LocalDateTime updatedAt) {
	super();
	this.id = id;
	this.name = name;
	this.price = price;
	this.brandName = brandName;
	this.model = model;
	this.color = color;
	this.createdAt = createdAt;
	this.updatedAt = updatedAt;
}
```
처럼 주석을 처리하고 위에서 배웠던 생성자 방식으로 한번 매핑으로 다시 한번 테스트를 해봤다.

```
Product selectProduct = query.select(Projections.constructor(
												Product.class, 
												product.id,
												product.name, 
												product.price,
												product.brandName,
												product.model,
												product.color,
												product.createdAt,
												product.updatedAt
										))
							   		.from(product)
							   		.where(product.id.eq(1L))
							   		.fetchFirst();
	System.out.println(selectProduct.toString());
	selectProduct.changePrice(5400000);
```
그랬더니 로그가 다음과 같이 나왔다.    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color,
        product.createdAt,
        product.updatedAt 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_,
            product0_.created_at as col_6_0_,
            product0_.updated_at as col_7_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
Product(id=1, name=Bass Guitar, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red, createdAt=2020-07-22T22:39:41, updatedAt=2020-07-22T22:39:41)
```
아하...     

Bean population을 이용한 bean, fields역시 메소드를 따라가면 만나는 녀석인 바로 Immutable이다.     

즉 queryDSL의 프로젝션 방식을 사용하게 되면 영속성과 무관해진다.    

이렇다 보니 문득 이런 생각이 들었다.    

'아니. 그러면 굳이 DTO를 만들 이유가 있나요? 그냥 위 방법으로 엔티티에 @QueryProjection 주는게 더 효과적인거 같은데.....'     

물론 그렇다.    

하지만 내 개인적인 생각이지만 그럼에도 DTO를 통해서 반환을 함으로써 이것은 DTO임을 명확하게 하는게 좀 더 유리하지 않을까 생각을 하게 된다.     

왜냐하면 복잡한 로직으로 치닫게 되면 이것을 영속성을 가진 엔티티로 착각할 수도 있고 그래서 지금같이 dirty checking을 사용하려는 실수를 할 수 있지 않을까?     

그래서 엔티티로 반환하지 말고 DTO로 반환하라고 말하는 것이다.     

또한 위에서 POJO에 대해서 고민할 떄도 토비의 스프링에서 언급했던 말이 DTO가 비록 queryDSL에 종속되도 OOP에서 유연하게 재활용될 수 있다면 그것이 최선의 선택이 아닌가 싶다.     

다만 엔티티에 @QueryProjection를 붙이는 것은 만일 중간에 어떤 로직으로 가공하지 않고 바로 Response로 반환한다면 고려해 볼 만 하다. 

하지만 '소프트웨어의 가치'라는 측면에서 이것은 언젠가 변할 수 있다. ~~너무 먼 미래인가?~~     

어째든 이런 모든 방법은 상황에 따라 유연하게 대처하며 사용하는 것이 최고의 방법일 것이다.     

### 아직 끝나지 않았다     

어허~ 아직 안끝났다.     

지금까지 진행하면서 이런 의문이 들지 않았나?     

'DTO를 사용하는건 알겠어요. 위 방법중에 근데 이런 경우라면 어떻게 하죠? Bean population을 이용한 bean, fields로 매핑하는 경우 만일 DTO의 속성중 name이 있는데 이게 명확하지 않아서 productName이라고 바꾸게 되었다면 이때는 저 위에 방식을 사용할 수 없을거 같은데요?'     

이것은 아주아주 쉽게 해결할 수 있다.     

자 그럼 어떤 이야기를 하는건지 살펴보자.    

먼저 위에 이야기대로 ProductDTO를 좀 수정하자.   

```
package io.basquiat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String productName;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
}
```

그리고 다음과 같이 코드를 실행하면?

```
ProductDTO selectProduct = query.select(Projections.bean(
											ProductDTO.class, 
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```
결과가 이렇다.  

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
ProductDTO(id=1, productName=null, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red)
```
어라? productName이 null이다. 당연히 setter를 찾을 텐데 ProductDTO에서 name이 productName으로 변경되었기 때문에 name에 대한 setter을 찾아 내부적으로 매핑할려고 할 것이고 없으니 productName은 null로 세팅되는 것이다.     

그러면 어떻게 할까? 쿼리를 짜다 보면 이런 차이로 인해 AS를 쓰는 것을 기억해야만 한다.    

```
ProductDTO selectProduct = query.select(Projections.bean(
											ProductDTO.class, 
											product.id,
											product.name.as("productName"), 
											product.price,
											product.brandName,
											product.model,
											product.color
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```
간단하지 않은가? .as()를 활용해 DTO의 필드명이랑 맞춰주면 된다.     

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name as productName,
        product.price,
        product.brandName,
        product.model,
        product.color 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
ProductDTO(id=1, productName=Bass Guitar, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red)
```
원하는 결과가 나왔다.     

그러면 fields의 경우도 ?

fields의 경우는 setter가 없어도 상관없으니 ProductDTO에서 걸어둔 @Setter어노테이션을 지우고 테스트해도 된다.     

```
ProductDTO selectProduct = query.select(Projections.fields(
											ProductDTO.class, 
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```

결과는?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
ProductDTO(id=1, productName=null, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red)
```
이 경우도 마찬가지이다.   

```
ProductDTO selectProduct = query.select(Projections.fields(
											ProductDTO.class, 
											product.id,
											product.name.as("productName"), 
											product.price,
											product.brandName,
											product.model,
											product.color
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```
결과는 위와 동일하게 잘 된다.     

하지만 생성자를 이용한 방식 상관이 없다.     

생성자를 이용한 방식이니 ProductDTO에 @AllArgsConstructor를 붙여주고 다음 코드를 테스트해보자.   

```
ProductDTO selectProduct = query.select(Projections.constructor(
											ProductDTO.class, 
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```

결과는

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.id,
        product.name,
        product.price,
        product.brandName,
        product.model,
        product.color 
    from
        Product product 
    where
        product.id = ?1 */ select
            product0_.id as col_0_0_,
            product0_.product_name as col_1_0_,
            product0_.price as col_2_0_,
            product0_.brand_name as col_3_0_,
            product0_.model as col_4_0_,
            product0_.color as col_5_0_ 
        from
            basquiat_product product0_ 
        where
            product0_.id=? limit ?
ProductDTO(id=1, productName=Bass Guitar, price=5300000, brandName=펜더, model=Custom Shop 63 Journeyman, color=Fiesta Red)
```
들어오는 타입으로만 체크하니 일단 타입만 맞으면 들어오게 된다.     

물론 이 방식은 sub query를 사용할 떄도 마찬가지이다.     

귀찮지만 하는김에 한번 Bean population의 .fields()로 테스트 해보자. ~~어짜피 .bean()도 마찬가지니까~~     

ProductDTO에 그냥 필드 하나 더 추가해 보자.     

```
package io.basquiat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String productName;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
	/** brand count */
	private long productCount;
	
}
```

productCount가 long인 이유는 count()가 반환하는 타입이 NumberExpression<Long>이기 때문이다.        

암튼 기존의 코드를

```
ProductDTO selectProduct = query.select(Projections.fields(
											ProductDTO.class, 
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color,
											ExpressionUtils.as(JPAExpressions.select(product.count()).from(product), "productCount")
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```
와 같이 ExpressionUtils를 사용해 별칭을 줘서 사용할 수 있다.     

그냥 JPAExpressions.select(product.count()).from(product)만 사용하면 

```
java.lang.IllegalArgumentException: Unsupported expression select count(product)
from Product product
	at com.querydsl.core.types.QBean.createBindings(QBean.java:75)
	at com.querydsl.core.types.QBean.<init>(QBean.java:129)
	at com.querydsl.core.types.Projections.fields(Projections.java:166)
	at io.basquiat.JpaMain.main(JpaMain.java:33)
```
같이 bindings에러가 나는 것을 확인할 수 있다.    

직접 확인해 보시길....

밑에 코드는 생성자 방식으로 서브 쿼리를 추가한 것이다.

```
ProductDTO selectProduct = query.select(Projections.constructor(
											ProductDTO.class, 
											product.id,
											product.name, 
											product.price,
											product.brandName,
											product.model,
											product.color,
											JPAExpressions.select(product.count()).from(product)
									))
						   		.from(product)
						   		.where(product.id.eq(1L))
						   		.fetchFirst();
System.out.println(selectProduct.toString());
```
보면 알겠지만 이럴 때는 별칭이 필요없다. 타입만 맞으면 되니깐!      

이런 이유로 생성자 방식을 고려한다면 이 부분에서는 세심한 주의가 필요하다.    


## queryDSL에서 이런것도 지원하네.     

앞서 GROUP BY에 대해 진행하면서 JAVA8 Stream API로 group by한것을 기억할 것이다.      

프로젝션과 관련해서 문서를 보다가 그 밑에 뜬금없이 Result aggregation부분이 있길래 봤더니 마치 JAVA8 Stream API를 쓰는 것처럼 queryDSL에서도 지원한다.    

마무리하기전에 알아보자.     

기존에 데이터가 있으니 이런 생각을 해보자.     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/7.query-dsl-projections/capture/capture1.png)    

이미지처럼 basquiat_brand와 basquiat_partner를 조인한 결과를 살펴보자.      

여기서 우리는 이런 생각을 해볼 수 있다.     

'파트너별로 가지고 있는 브랜드를 묶어서 보고 싶은데?'      

그것을 queryDSL에서 지원한다.    

```
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
        	System.out.println(results.toString());
        	
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
import static com.querydsl.core.group.GroupBy.groupBy을 임포트해서 사용하는 방식인데, 바로 .transform()이라는 메소드를 통해 묶어서 보고 싶은 것 즉 그러니까 groupBy대상을 partner.id 또는 partner.name으로 지정하고 리스트를 묶는데 brand로 묶는다고 명시를 해주면 된다.     

자바의 Collectors.groupingBy()의 사용 용법과 상당히 비슷하다.     

결과는 어떨까?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        partner.id,
        brand 
    from
        Brand brand   
    inner join
        brand.partner as partner */ select
            partner1_.id as col_0_0_,
            brand0_.br_code as col_1_0_,
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
//partner.id로 groupBY
{
	MUSICFORCE=[
				Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
				Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
				Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null)
			   ], 
				
	RIDINBASS=[
				Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
				Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
				Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
			 ]
}


//partner.name로 groupBY
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        partner.name,
        brand 
    from
        Brand brand   
    inner join
        brand.partner as partner */ select
            partner1_.partner_name as col_0_0_,
            brand0_.br_code as col_1_0_,
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
{
	뮤직포스=[
			Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
			Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
			Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null)
		   ], 
			
	라이딩 베이스=[
				Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
				Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
				Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
			  ]
}
```

이것은 또한 JPA 영속성을 지니고 있다.    

Brand 엔티티의 number 필드에 @Setter를 추가하고 다음과 같이 dirty checking을 시도해보면 

```
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
```
결과는 

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        partner.name,
        brand 
    from
        Brand brand   
    inner join
        brand.partner as partner */ select
            partner1_.partner_name as col_0_0_,
            brand0_.br_code as col_1_0_,
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
[Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
Hibernate: 
    /* update
        io.basquiat.model.Brand */ update
            basquiat_brand 
        set
            br_en_name=?,
            launched_at=?,
            br_name=?,
            number=?,
            partner_id=?,
            updated_at=? 
        where
            br_code=?
```
업데이트 쿼리가 날아간다.     

필요하면 정말 유용하게 사용할 수 있을 것 같다.   

# At A Glance    

어떻게 생각하는가? DTO로 반환해서 사용하는게 좋은지 아니면 Entity 생성자에 @QueryProjection를 붙여서 entity로 반환하는것이 좋은지 말이다.      

위에서도 언급했지만 엔티티로 반환해도 어짜피 영속성과는 무관하니 상황에 맞춰서 잘 사용하면 되지 않을까 싶기도 하고....     

아니면 '아냐 그냥 무조건 DTO로 가야되!'     

라고 할 수도 있다.     

사실 답은 없는거 같다.     

이제는 거의 막바지에 온거 같은 느낌이 든다. 아마도 다음 브랜치 아니면 다다음 브랜치로 마무리가 될 듯 싶다.      