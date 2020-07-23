# GROUP BY using queryDSL    

### 시작하기 앞서    

그 전에 Java8 Stream API를 이용한 groupby와 자바스크립트 진형의 lodash를 이용한 groupby에 대해서 먼저 좀 알아보자.     

아시는 분들은 그냥 건너띄어도 좋다. 하지만 group by라는 것이 생소한 분들은 한번쯤은 코드레벨에서 어떻게 group by를 하는지 알아보는 것도 좋다.    

이 특성은 디비와 비슷한 면이 있어서 (물론 많이 다르다) 한번 어떤 특징이 있는지 SQL보다는 익숙한 코드를 통해서 먼저 말아보고자 한다.     

나의 경우 전체 데이터를 디비에서 groupby해서 가져와야 하는 경우가 아니면 주어진 데이터내에서 groupby를 하는 것을 좀 더 선호하기 때문인데 일단 시작하자.    

### Javascript GROUP BY using lodash     

여러분들은 [repl.it](https://repl.it) 사이트에서 상단에 '<> start coding'을 클릭하고 node.js를 선택해서 'Repl create'버튼을 클릭한다.    

```
const _ = require('lodash');

let dataList = [
  {name:'Fodera', price: 15000000, model: 'Empperor 5 Deluxe', color: 'Blueburst'},
  {name:'Fender', price: 5400000, model: 'Custom Shop 63 Journeyman', color: 'Fiesta Red'},
  {name:'Sandberg', price: 5500000, model: 'California TT 5 Masterpiece', color: 'Twotone Sunburst'},
  {name:'Mayone', price: 4500000, model: 'Jabba Classic 5', color: 'Natural'},
  {name:'Fender', price: 3200000, model: 'Elite 5', color: 'Candy Apple Red'},
  {name:'Fender', price: 3000000, model: 'Elite 4', color: 'Candy Apple Red'},
  {name:'Sandberg', price: 5500000, model: 'California VS 5 Masterpiece', color: 'Black'}
];

let groupByPrice = _.groupBy(dataList, 'price');
console.log(groupByPrice);

let groupByName = _.groupBy(dataList, 'name');
console.log(groupByName);

let groupByModel = _.groupBy(dataList, 'model');
console.log(groupByModel);
```

다음과 같이 작성을 하고 한번 run을 실행시켜보면 어떤 결과를 볼 수 있는지 확인해 보자.     

아마도 확인을 했다면 코드에서 선언한 키값으로 하나의 그룹 리스트를 만드는 것을 볼 수 있다.     
 
그럼 자바8의 Stream API에서 그룹핑을 하게 되면 어떻게 될까?   

### Java8 GROUP BY using Stream API     

다음과 같은 단순 객체를 하나 만들어보자.

```
package io.basquiat.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BassGuitar {

	private String name;
	
	private int price;
	
	private String model;
	
	private String color;
	
}

```

그리고 test폴더에 

```
package io.basqauit.groupby.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.basquiat.simple.BassGuitar;

public class GroupByTest {

	@Test
	public void itemEntityTest() {
		
		BassGuitar bass1 = BassGuitar.builder()
									 .name("Fodera")
									 .price(15000000)
									 .model("Emperor Deluxe 5")
									 .color("Blueburst")
									 .build();

		BassGuitar bass2 = BassGuitar.builder()
									 .name("Fender")
									 .price(5400000)
									 .model("Custom Shop 63 Journeyman")
									 .color("Fiesta Red")
									 .build();
		BassGuitar bass3 = BassGuitar.builder()
									 .name("Sandberg")
									 .price(5500000)
									 .model("California TT 5 Masterpiece")
									 .color("Twotone Sunburst")
									 .build();
		
		BassGuitar bass4 = BassGuitar.builder()
									 .name("Fender")
									 .price(3200000)
									 .model("Elite 5")
									 .color("Candy Apple Red")
									 .build();
		
		
		BassGuitar bass5 = BassGuitar.builder()
									 .name("Fender")
									 .price(3000000)
									 .model("Elite 4")
									 .color("Candy Apple Red")
									 .build();
		
		
		BassGuitar bass6 = BassGuitar.builder()
									 .name("Sandberg")
									 .price(5500000)
									 .model("California VS 5 Masterpiece")
									 .color("Black")
									 .build();
		
		
		Map<String, List<BassGuitar>> groupByName = Arrays.asList(bass1, bass2, bass3, bass4, bass5, bass6)
												   .stream()
												   .collect(Collectors.groupingBy(BassGuitar::getName));
		System.out.println(groupByName);
		
		Map<Integer, List<BassGuitar>> groupByPrice = Arrays.asList(bass1, bass2, bass3, bass4, bass5, bass6)
															.stream()
															.collect(Collectors.groupingBy(BassGuitar::getPrice));
		System.out.println(groupByPrice);
		
	}
	
}
```
다음과 같이 노가다를 해서 한번 Group By를 한번 해봤다.

```
name으로 GROUP BY
{
	Fender=[
			BassGuitar(name=Fender, price=5400000, model=Custom Shop 63 Journeyman, color=Fiesta Red), 
			BassGuitar(name=Fender, price=3200000, model=Elite 5, color=Candy Apple Red), 
			BassGuitar(name=Fender, price=3000000, model=Elite 4, color=Candy Apple Red)
		   ], 
			
	Fodera=[
			BassGuitar(name=Fodera, price=15000000, model=Emperor Deluxe 5, color=Blueburst)
		   ], 
	
	Sandberg=[
			 BassGuitar(name=Sandberg, price=5500000, model=California TT 5 Masterpiece, color=Twotone Sunburst), 
			 BassGuitar(name=Sandberg, price=5500000, model=California VS 5 Masterpiece, color=Black)]
}
====================================
price로 GROUP BY
{
	3200000=[
			BassGuitar(name=Fender, price=3200000, model=Elite 5, color=Candy Apple Red)
			], 
			
	5400000=[
			BassGuitar(name=Fender, price=5400000, model=Custom Shop 63 Journeyman, color=Fiesta Red)
			], 
			
	5500000=[
			BassGuitar(name=Sandberg, price=5500000, model=California TT 5 Masterpiece, color=Twotone Sunburst), 
			BassGuitar(name=Sandberg, price=5500000, model=California VS 5 Masterpiece, color=Black)
			], 
			
	15000000=[
			BassGuitar(name=Fodera, price=15000000, model=Emperor Deluxe 5, color=Blueburst)
			], 
			
	3000000=[
			BassGuitar(name=Fender, price=3000000, model=Elite 4, color=Candy Apple Red)
			]
}
```

group by로 나온 결과들을 잘 살펴보기 바란다.     

group by라는 것은 원하는 어떤 특정 컬럼이나 키 값으로 묶어서 보는 것이라는 것을 알 수 있다.     

너무 간단했나?     

그럼 이제는 실제 DB에서는 이 GROUP BY가 어떻게 사용되는지 확인해 보자.     

## GROUP BY ON database

그에 앞서 db에서의 group by는 위에서 우리가 살펴본 코드레벨의 group by와는 좀 다르다.     

특정 컬럼에 대해서 group by를 하면 그 컬럼의 유니크한 값들로 테이터를 그룹핑 하는데 중복된 열은 제거하게 된다.    

또한 보통 집합 함수와 같이 사용되기 때문에 이것도 같이 알아보는 시간을 가져 보자.     

그 전에 이전 브랜치에서 사용했던 Product에 필드를 몇개 추가하자.

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
	private String id;

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

그리고 기존 데이터를 유지하기 위해서 hibernate.hbm2ddl.auto을 none으로 두었기 때문에 스키마를 다시 생성해서 데이터를 다시 한번 밀어넣어 보자.

```
drop table basquiat_product;

CREATE TABLE `basquiat_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT 0,
  `brand_name` varchar(255) NOT NULL,
  `model` varchar(255),
  `color` varchar(255),
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO basquiat_product
	(
		product_name, price, brand_name, model, color, created_at, updated_at
    )
VALUES
    (
		'Bass Guitar',  5400000, '펜더', 'Custom Shop 63 Journeyman', 'Fiesta Red', NOW(), NOW()
    ),
    (
		'Bass Guitar',  5400000, '샌드버그', 'California TT 5 Masterpiece', 'Twotone Sunburst', NOW(), NOW()
    ),
    (
		'Bass Guitar',  3200000, '펜더', 'Elite 5', 'Candy Apple Red', NOW(), NOW()
    ),
    (
		'Bass Guitar',  3000000, '펜더', 'Elite 4', 'Candy Apple Red', NOW(), NOW()
    ),
    (
		'Electric Guitar', 6400000, '펜더', 'Custom Shop telecaster', 'Scotch Cream Butter', NOW(), NOW()
    ),
    (
		'Bass Guitar',  5400000, '샌드버그', 'California VS 5 Masterpiece', 'Balck Sunburst', NOW(), NOW()
    ),
    (
		'Electric Guitar', 6700000, '펜더', 'Custom Shop Stratocaster', 'Daphne Blue', NOW(), NOW()
    )  ;
```

위에 데이터를 기준으로 우리가 만일 brand_name별로 가장 비싼 악기를 뽑아보고 싶다면 어떻게 하면 될까?    

일단 brand_name이 그룹핑할 대상이 될것이다. 그리고 가장 비싼 악기이니 그중 price가 가장 높은 즉 max인 녀석을 추려내면 되지 않을까?    

그럼 쿼리는 어떻게 작성하면 될까?     

```
SELECT brand_name, MAX(price)
	FROM basquiat_product
  GROUP BY brand_name;
```

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture1.png)    

결과를 보면 펜더에서 가장 고가가 6700000고 샌드버그의 경우에는 동일하니 어째든 5400000이 나온다.      

물론 이것은 model로 생각을 해도 무방하다. 호기심이 생긴다면 한번 테스트 해보는 것도 괜찮다. 하지만 같은 model이 없어서 뭐....      

자 그럼 이런 생각도 해볼 수 있다.     

'그럼 가장 비싼 악기, 가장 싼 악기도 결과를 가져올 수 있겠군요?'

물론이지요?

```
SELECT brand_name, MAX(price), MIN(price)
	FROM basquiat_product
  GROUP BY brand_name;
```
![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture2.png)    

'그럼 가장 비싼 악기, 싼 악기, 그리고 악기들의 평균, 그리고 brand_name별로 악기 수도 가져올 수 있겠군요?'

네 다 됩니다.     

```
SELECT brand_name, MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)
	FROM basquiat_product
    GROUP BY brand_name;
```

친절하게 각 브랜드별 모든 악기의 합산까지 넣어드렸어요~     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture3.png)    

하지만 만일 GROUP BY 절을 사용하지 않으면 어떤 일이 벌어질까?     

```
10:50:44	SELECT brand_name, MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)  FROM basquiat_product LIMIT 0, 1000	Error Code: 1140. In aggregated query without GROUP BY, expression #1 of SELECT list contains nonaggregated column 'basquiat.basquiat_product.brand_name'; this is incompatible with sql_mode=only_full_group_by	0.00085 sec
```
DB입장에서는 집합 함수를 사용하고 있는데 그 그룹할 대상이 없으니 위와 같은 오류를 발생한다.  In aggregated query without GROUP BY 보이는가?     

'저기요? 근데 보통 카운터 쿼리 보면 count도 집합 함수인데 GROUP BY가 없는데요?'

일단 저기 위에 사용한 집합 함수의 경우에는 만일 튜플에서 brand_name을 없애고 GROUP BY도 없애면 어떻게 될까?     

물론 잘 나온다. 하지만 결과는 어떻게 될까? 이 경우에는 그룹핑할 컬럼이 없으니 쿼리는 내부적으로 모든 데이터를 대상으로 집계를 내버린다.      

결국 count역시 모든 데이터를 대상으로 카운터를 가져온다는 것을 알 수 있다.      

물론 다음과 같이     

```
SELECT MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)
	FROM basquiat_product
    GROUP BY brand_name;
```
쿼리를 날려도 기존과 동일한 결과를 가져오지만 데이터적으로는 어떤 brand_name으로 집계를 냈는지 알 수 없기 때문에 brand_name을 함께 조회하게 되는 것이다.     

그러면 이런 궁금증이 생길 것이다. ~~궁금증이 생겨야 한다!!!!!!!!~~     

'저기 근데요 보통 우리가 조건을 거는 경우에 FROM절 이후 WHERE절에 조건을 걸게 되는데 지금같은 경우에는 FROM절 이후에 WHERE절을 이용해서 조건을 걸 수 없는 경우가 있는거 같아요? 예를 들면 평균을 낸 AVG는 GROUP BY가 이뤄진 이후에 평균에 대고 조건을 걸 수 있을거 같은데 이러면 평균값에 대한 조건을 WHERE절에는 걸수 없잖아요?'     

맞다. 바로 보았다. 그래서 이런 녀석이 존재한다.    

```
SELECT brand_name, MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)
	FROM basquiat_product
    GROUP BY brand_name
    HAVING AVG(price) > 5000000;
```
바로 HAVING절을 사용한다.     

자 위에 쿼리대로라면 AVG(price)가 5백만원 이상인 녀석들만 가져오라고 말하고 있으니 위에서 조회했던 결과에서 어떤게 나올지 이미 여러분은 알고 있을 것이다.     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture4.png)    

쿼리 공부하다보면 가장 많이 사용되는 내용중 하나이다.      

```
어떤 팀에 속한 사원중 연봉이 사원의 평균 연봉보다 높은 사람을 가져오시오~~
```
이런 문제 많이 봤을껄? 왜냐하면 대부분의 쿼리 관련 책들이나 공부하다보면 무조건 나오는 마치 'Hello World'같은 녀석이기 때문이다. ~~공감합니까?~~    

또한 이 GROUP BY도 여러 개의 컬럼으로 묶어서 그룹핑 할 수도 있다.     

```
SELECT brand_name, model, MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)
	FROM basquiat_product
    GROUP BY brand_name, model 
```
현재 데이터에서는 model이 같은게 없으니...이런 걸 생각해보자.      

악기가 생상된 시기가 좀 오래되서 연식있는 제품의 경우에는 model이 같을 수 있지만 가격을 할인해서 판다든가 하는?    

'Custom Shop 63 Journeyman'모델을 가진 악기를 하나 더 만들어서 가격을 낮춰서 넣어보자.    

```
INSERT INTO basquiat_product
	(
		product_name, price, brand_name, model, color, created_at, updated_at
    )
    VALUES
	(
		'Bass Guitar',  4400000, '펜더', 'Custom Shop 63 Journeyman', 'Shell Pink', NOW(), NOW()
    )
```

그리고 위에 쿼리를 날려보면?    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture5.png)    

결과를 보면 이해할 수 있을 것이다.     

그리고 참고로 해당 그룹할 컬럼에 인덱스가 잡혀 있다면 이 경우에는 자동으로 오름차순으로 정렬되서 나타나게 된다.    

예를 들면 지금 위에서 테스트해본 group by brand_name, model의 경우 이 두개의 컬럼이 하나의 인덱스로 걸려 있다면 (각 컬럼으로 인덱스가 걸려있으면 안된다) 결과는 전혀 달라진다.    

```
CREATE INDEX idx_brand_name_model ON basquiat_product ( brand_name, model );

SELECT brand_name, model, MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)
	FROM basquiat_product
    GROUP BY brand_name, model
```

저 두개를 묶어서 인덱스를 생성하는 쿼리를 날리고 다시 한번 쿼리를 실행하면?     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture6.png)    

자동으로 정렬되는 것을 알 수 있다.     

일단 이런 특성은 알아야 우리가 좀 더 정확하게 원하는 데이터를 원하는 방식으로 가져올 수 있는 것이다.     

자 이 정도면 queryDSL에서 우리가 어떻게 사용해야 될지 이미 감을 잡으신 분들도 있으리라 생각한다.     

그럼 이젠 지금 테스트하면서 생성된 데이터를 가지고 시작해 보자.    

## GROUP BY using queryDSL     

일단은 GROUP BY 없이 집합 함수를 사용한 기본적인 코드를 한번 살펴보자.     

쿼리로 치면

```
SELECT MIN(price), MAX(price), AVG(price), SUM(price), COUNT(1)
	FROM basquiat_product;
```
와 같은 것이다.     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/6.query-dsl-groupby-aggregation/capture/capture7.png)    

그럼 코드로 옮겨보자.    

```
package io.basquiat;

import static io.basquiat.model.QProduct.product;

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
        	
        	Tuple tuple = query.select(
									 product.price.min(),
									 product.price.max(),
									 product.price.avg(),
									 product.price.sum(),
									 product.count()
									)
							  	.from(product)
							  	.fetchOne();
        	
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
```

결과는

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        min(product.price),
        max(product.price),
        avg(product.price),
        sum(product.price),
        count(product) 
    from
        Product product */ select
            min(product0_.price) as col_0_0_,
            max(product0_.price) as col_1_0_,
            avg(product0_.price) as col_2_0_,
            sum(product0_.price) as col_3_0_,
            count(product0_.id) as col_4_0_ 
        from
            basquiat_product product0_
[3000000, 6700000, 4987500.0, 39900000, 8]
```
오호? 제대로 했다는 것을 알 수 있다.     

자 그럼 위에서 테스트했던 쿼리를 실제로 한번 똑같이 GROUP BY을 이용해서 queryDSL로 변환해 보자.     

```
SELECT brand_name, MAX(price), MIN(price)
	FROM basquiat_product
  GROUP BY brand_name;
```
코드로 작성하면 

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
									 product.price.max()
									)
							  	.from(product)
							  	.groupBy(product.brandName)
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
```

그럼 결과는?    

```
Hibernate: 
    /* select
        product.brandName,
        min(product.price),
        max(product.price) 
    from
        Product product 
    group by
        product.brandName */ select
            product0_.brand_name as col_0_0_,
            min(product0_.price) as col_1_0_,
            max(product0_.price) as col_2_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name
[[샌드버그, 5400000, 5400000], [펜더, 3000000, 6700000]]
```
어 근데 좀 다르다. 왜일까?     

그 이유는 아까 전에 brand_name과 model을 묶어서 인덱스로 생성했기 때문이다.     

그럼 다음과 같이     

```
ALTER TABLE basquiat_product 
DROP INDEX idx_brand_name_model;
```
아까 생성한 인덱스를 지우고 다시 한번 실행하면

```
Hibernate: 
    /* select
        product.brandName,
        min(product.price),
        max(product.price) 
    from
        Product product 
    group by
        product.brandName */ select
            product0_.brand_name as col_0_0_,
            min(product0_.price) as col_1_0_,
            max(product0_.price) as col_2_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name
[[펜더, 3000000, 6700000], [샌드버그, 5400000, 5400000]]
```
오호? 위에서 설명했던 인덱스가 걸려 있는 컬럼을 GROUP BY하면 해당 컬럼으로 자동으로 오름차순으로 정렬한다는 것이 여기서도 증명이 되었다.     

'그럼, 아까 위에서처럼 두개의 컬럼을 묶어서 GROUP BY도 가능한가요?'      

이 다음에 해볼려고 했어요~

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
        						     product.model,
									 product.price.min(),
									 product.price.max()
									)
							  	.from(product)
							  	.groupBy(product.brandName, product.model)
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
```

결과도 확인  

```
Hibernate: 
    /* select
        product.brandName,
        product.model,
        min(product.price),
        max(product.price) 
    from
        Product product 
    group by
        product.brandName,
        product.model */ select
            product0_.brand_name as col_0_0_,
            product0_.model as col_1_0_,
            min(product0_.price) as col_2_0_,
            max(product0_.price) as col_3_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name ,
            product0_.model
[
	[펜더, Custom Shop 63 Journeyman, 4400000, 5400000], 
	[샌드버그, California TT 5 Masterpiece, 5400000, 5400000], 
	[펜더, Elite 5, 3200000, 3200000], 
	[펜더, Elite 4, 3000000, 3000000], 
	[펜더, Custom Shop telecaster, 6400000, 6400000], 
	[샌드버그, California VS 5 Masterpiece, 5400000, 5400000], 
	[펜더, Custom Shop Stratocaster, 6700000, 6700000]
]
```

5번째 그림과 결과가 똑같다.     

'어 아까는 정렬이 되었는데요?'     

당연히 테스트하면서 인덱스를 지웠으니 

```
CREATE INDEX idx_brand_name_model ON basquiat_product ( brand_name, model );
```
다시 인덱스를 생성하고 쿼리를 하면?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.brandName,
        product.model,
        min(product.price),
        max(product.price) 
    from
        Product product 
    group by
        product.brandName,
        product.model */ select
            product0_.brand_name as col_0_0_,
            product0_.model as col_1_0_,
            min(product0_.price) as col_2_0_,
            max(product0_.price) as col_3_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name ,
            product0_.model
[
	[샌드버그, California TT 5 Masterpiece, 5400000, 5400000], 
	[샌드버그, California VS 5 Masterpiece, 5400000, 5400000], 
	[펜더, Custom Shop 63 Journeyman, 4400000, 5400000], 
	[펜더, Custom Shop Stratocaster, 6700000, 6700000], 
	[펜더, Custom Shop telecaster, 6400000, 6400000], 
	[펜더, Elite 4, 3000000, 3000000], 
	[펜더, Elite 5, 3200000, 3200000]]
```

정렬이 되서 조회가 된다.     

그럼 또 우리는 가장 흔하디 흔한 평균 가격보다 큰 녀석만 조회하는 것도 해봐야 한다.       

악기의 평균 가격중 4백만원보다 큰 녀석만 가져오겠다면? 이미 예상으로는 전부 나올 것이다.

```
List<Tuple> tuple = query.select(
							 product.brandName,
							 product.price.min(),
							 product.price.max(),
							 product.price.sum(),
							 product.price.avg()
							)
					  	.from(product)
					  	.groupBy(product.brandName)
					  	.having(product.price.avg().gt(4000000))
					  	.fetch();
	
System.out.println(tuple);
```

결과는  

```
Hibernate: 
    /* select
        product.brandName,
        min(product.price),
        max(product.price),
        sum(product.price),
        avg(product.price) 
    from
        Product product 
    group by
        product.brandName 
    having
        avg(product.price) > ?1 */ select
            product0_.brand_name as col_0_0_,
            min(product0_.price) as col_1_0_,
            max(product0_.price) as col_2_0_,
            sum(product0_.price) as col_3_0_,
            avg(product0_.price) as col_4_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name 
        having
            avg(product0_.price)>?
[
	[샌드버그, 5400000, 5400000, 10800000, 5400000.0], 
	[펜더, 3000000, 6700000, 29100000, 4850000.0]
]
```
그럼 위에서 4번째 그림에서 테스트 했던 5백만원보다 큰 녀석만 가져오는 코드를 짠다면?

```
List<Tuple> tuple = query.select(
							 product.brandName,
							 product.price.min(),
							 product.price.max(),
							 product.price.sum(),
							 product.price.avg()
							)
					  	.from(product)
					  	.groupBy(product.brandName)
					  	.having(product.price.avg().gt(5000000))
					  	.fetch();
	
System.out.println(tuple);
```

결과는 똑같을 것이다.   

```
Hibernate: 
    /* select
        product.brandName,
        min(product.price),
        max(product.price),
        sum(product.price),
        avg(product.price) 
    from
        Product product 
    group by
        product.brandName 
    having
        avg(product.price) > ?1 */ select
            product0_.brand_name as col_0_0_,
            min(product0_.price) as col_1_0_,
            max(product0_.price) as col_2_0_,
            sum(product0_.price) as col_3_0_,
            avg(product0_.price) as col_4_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name 
        having
            avg(product0_.price)>?
[[샌드버그, 5400000, 5400000, 10800000, 5400000.0]]
```

이렇게 해서 GROUP BY와 GROUP BY이후 생성된 데이터를 다시 추려내는 HAVING절을 어떻게 queryDSL로 작성하는지 알아봤다.     

'자..잠깐만요? having절에서도 여러개의 조건을 추가할 수 있나요?'     

당연히 가능하다!!!!

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
```
자. 어거지로 한번 짜 보자.     

일단 평균 가격이 5백만원보다 크고 530만원보다 작은 녀석을 추려낸다고 해보자. 일단 데이터로는 나오지 않을 것이다.     

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        product.brandName,
        min(product.price),
        max(product.price),
        sum(product.price),
        avg(product.price) 
    from
        Product product 
    group by
        product.brandName 
    having
        avg(product.price) > ?1 
        and avg(product.price) < ?2 */ select
            product0_.brand_name as col_0_0_,
            min(product0_.price) as col_1_0_,
            max(product0_.price) as col_2_0_,
            sum(product0_.price) as col_3_0_,
            avg(product0_.price) as col_4_0_ 
        from
            basquiat_product product0_ 
        group by
            product0_.brand_name 
        having
            avg(product0_.price)>? 
            and avg(product0_.price)<?
[]
```
쿼리가 날아간 것을 보면 원하는 대로 나간 것을 알 수 있다.      

대부분은 우리가 생각할 수 있는 것은 거의 구현이 가능하도록 설계되어 있다. 물론 제약도 있을 것이다.     

하지만 결국 여러 쿼리들, 자신이 원하는 데이터를 어떻게 가져올 지에 대한 아이디어만 있다면 거의 왠간해선 가능하다고 본다.      

## At A Glance      

사실 문법만 나열하고 이렇게 쓰면 되요~ 라고 하면 나도 편했을 것이다.      

특히 내가 그냥 알고 있는 지식을 텍스트로 표현하자니 전달하는데 많은 애로사항이 꽃을 피었다.     

어찌보면 그냥 경험에서 생긴 일종의 관념으로 사용하는 경향이 있어서 어떤 정확한 지식적인 부분이 부족해서 일 수도 있었을 것이다.     

하지만 queryDSL은 어떻게 보면 쿼리를 잘 아는 사람에게는 코드레벨에서 쿼리를 짤 수 있는 정말 최고의 선택이 될 것이다. 그로 인해 개발 속도, 포퍼먼스도 훨씬 잘 나올것이다.     

그래서 일부로 관련 쿼리에 대한 지식을 나열했는데 어떻게 잘 전달이 되었는지 이게 궁금하다.     

다음에는 우리가 지금까지 테스트해오면서 중간중간 튜플로 정보를 가져오기도 하고 객체로 꺼내오기도 했지만 사실 실무에서는 아마 Projection을 잘 써야 하는 경우가 대부분이라고 생각이 든다.     

그래서 튜플같은 경우에는 그냥 로그로만 찍었지만 실제로 튜플에서 내가 원하는 정보를 가져오는 방식은 다르다. 마치 ResultSet에서 get으로 꺼내오는 방식이랑 비슷하기도 하고 실제로 JPQL에서는 DTO로 매핑해서 쓰게 된다.     

그래서 다음 브랜치에서는 이와 관련된 내용을 담을 것이다.      
