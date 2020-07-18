# SELECT CLAUSE using queryDSL    

앞에서 SELECT를 하면서 객체 전체를 가져오는 예제만 했지 실제로 쿼리처럼 각 컬럼을 명시해서 가져오는 방법은 딱 한번 튜플을 써서 한 예제 외에는 없었다.    

하지만 실제로 우리는 SELECT절 내에서 다양한 방식을 사용하게 된다. 때론 DB에서 제공하는 함수들, 예들 들면 DATE_FORMAT같은 것을 사용하기도 한다.    

그럼 queryDSL에서는 어떻게 할까? 라는 의문이 드는데 대부분 지원한다. 다만 제공하는 모듈을 사용해야하는 불편함이 있지만 queryDSL에서 제공하는 모듈을 사용하면 된다.    

자 그럼 가장 흔히 사용하는 것부터 알아보자.    

### .as()

alias의 줄임말인 as는 보통 언제 쓰냐면은 다른 테이블을 join해서 데이터를 가져올 때 대상이 되는 Join table의 컬럼명이 같은 경우와 DB에서 제공하는 함수를 사용했을 때 보통 사용한다.     

예를 들면     

```
SELECT br.name AS br_name,
	   pt.name AS pt_name
	FROM brand br
	JOIN partner pt ON br.partner_id = pt.id
````
같이 만일 brand와 partner테이블의 name이 같은 경우 구분을 하기 위해서 사용하게 된다.     

또는 다음과 같이 가장 흔한 카운터 쿼리의 경우처럼 DB의 함수를 사용한 경우에도 사용하게 된다.

```

SELECT COUNT(1) AS count
	FROM brand
```
위의 경우 별칭을 안주면 결과에서 COUNT(1)이 컬럼으로 넘어간다.     

실제로 api호출에 응답으로 데이터를 줄 때 JSON형식에 키값으로 'COUNT(1)'로 들어가는 황당한 경우를 볼 수 있다.     

~~경험담: 프론트엔드 담당자분님께서 당황하며 'DATE_FORMAT(end_date, '%Y-%m-%d %H:%i')'이란 키가 있는데 이건 뭐죠?~~     

자 그럼 이제 어떻게 사용할까?

조인 부분은 차후에 하겠지만 일단 이것을 하기 위해서 join을 걸어보자.    

또한 아직 프로젝션에 대해서 공부하지 않았지만 대충 어떤 느낌인지만 보고 가자.    

```
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
										brand.name.as("brand_name"),
										partner.name.as("partner_name")
									  )
		       		 	   	   .from(brand)
		       		 	   	   .join(brand.partner, partner)
		       		 	   	   .fetch();
        	
        	System.out.println(tuple.toString());
			
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
여기서 이 부분을 주목하자.     

```
query.select(
			brand.name.as("brand_name"),
			partner.name.as("partner_name")
		  )
   .from(brand)
   .join(brand.partner, partner)
   .fetch();
```

brand의 이름과 partner의 객체에 선언된 필드명은 name이라 서로 중복이 된다. 물론 테이블 생성시에는 br_name, partner_name이라 직접 쿼리하면 다음과 같이    

```
SELECT br.br_name,
	   pt.partner_name
	FROM brand br
	JOIN partner pt ON br.partner_id = pt.id

```
처럼 다르기 때문에 굳이 별칭을 줄 필요가 없지만 객체 입장에서는 name이 중복이 된다.    

따라서 as() API를 이용해서 별칭을 주면 된다. 그럼 결과는?

```
Hibernate: 
    /* select
        brand.name as brand_name,
        partner.name as partner_name 
    from
        Brand brand   
    inner join
        brand.partner as partner */ select
            brand0_.br_name as col_0_0_,
            partner1_.partner_name as col_1_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
[[에프베이스, 뮤직포스], [펜더, 뮤직포스], [포데라, 뮤직포스], [말로우, 라이딩 베이스], [매티슨, 라이딩 베이스], [샌드버그, 라이딩 베이스]]
```
쿼리를 보면 alias가 붙어서 쿼리가 날아간 것을 볼 수 있다.    

### .when()

컬럼의 값에 따른 값을 따로 보여줘야 하는 즉, 조건문을 써야 경우가 있다.     

예를 들면 user라는 테이블을 조회햇는데 나이가 10~19살 사이면  '십대', 20~29면 '이십대'로 값을 내려줘야 하는 경우가 있다고 생각해 보자.    

쿼리에서는 대충 이런 식일 것이다.

```
select user_id, 
	   CASE 
	   	WHEN age = 10 THEN '10짤 부럽다'
	   	WHEN age = 20 THEN '20짤 부럽다'
	    ELSE '그외 같이 늙어가넹~'
	   END AS age_interval
FROM user

```

같이 작성할 수도 있고 다음처럼 좀 더 복잡한 조건을 줘서    

```
SELECT user_id, 
	   CASE 
	   	WHEN age >= 10 AND age < 20 THEN '십대 부럽다~'
	   	WHEN age >= 20 AND age < 30 THEN '이십대 부럽다~'
	    ELSE '그외 같이 늙어가네~'
	   END AS age_interval
FROM user

```

이렇게도 할 수 있고 저렇게도 할 수 있다.     

일단 쿼리는 이런데 queryDSL의 경우에는 전자의 경우와 후자의 경우 코딩 방식이 달라진다.    

자 그럼 일단 첫 번째 단순 조건을 테스트해보자.    

날 쿼리로 짜면 

```
SELECT br_code AS id,
	   CASE 
           WHEN br_en_name = 'Fodera'  THEN 'Victor Wooten이 쓴다는 그 베이스' 
           WHEN br_en_name = 'Fender'  THEN '베이스의 근본'
           ELSE '엄청 좋은 고가의 베이스' 
		END AS comment 
	FROM basquiat_brand

result grid
id			| comment
------------- |---------------------------
FBASS		| 엄청 좋은 고가의 베이스
FENDER		| 베이스의 근본
FODERA 		| Victor Wooten이 쓴다는 그 베이스
MARLEAUX 	| 엄청 좋은 고가의 베이스
MATTISSON	| 엄청 좋은 고가의 베이스
SANDBERG		| 엄청 좋은 고가의 베이스
```

그럼 코드는 어떻게?    

```
List<Tuple> tuple = query.select(	brand.code.as("id"),
								brand.enName.when("Fodera").then("Victor Wooten이 쓴다는 그 베이스")
									       .when("Fender").then("베이스의 근본")
									       .otherwise("엄청 좋은 고가의 베이스")
									       .as("comment")
							  )
       		 	   	   .from(brand)
       		 	   	   .fetch();
```

아하? 그냥 메소드 체이닝으로 풀어가면 된다.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/3.query-dsl-orderby-n-paging/capture/capture1.png)    

이전 브랜치에서 사용했던 값 그대로 한번 생각해 보자.     

number를 기준으로 1-3, 4-6인 녀석을 조건문으로 분리하고 싶다면?     

위와 같은 코드로는 할 수 없고 이 때는 CaseBuilder라는 녀석을 사용해야 한다.    

그럼 코드로 한번 보자.     

```
List<Tuple> tuple = query.select(	brand.code.as("id"),
								new CaseBuilder().when(brand.number.between(1, 3)).then("참 좋은 베이스 브랜드")
												 .when(brand.number.between(4, 5)).then("이것도 참 좋은 베이스 브랜드")
												 .otherwise("이것도 역시 참 좋은 베이스")
												 .as("comment")
							  )
       		 	   	   .from(brand)
       		 	   	   .fetch();
System.out.println(tuple.toString());
```

결과는 어떻게 나올까?    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand.code as id,
        (case 
            when (brand.number between ?1 and ?2) then ?3 
            when (brand.number between ?4 and ?5) then ?6 
            else '이것도 역시 참 좋은 베이스' 
        end) as comment 
    from
        Brand brand */ select
            brand0_.br_code as col_0_0_,
            case 
                when brand0_.number between ? and ? then ? 
                when brand0_.number between ? and ? then ? 
                else '이것도 역시 참 좋은 베이스' 
            end as col_1_0_ 
        from
            basquiat_brand brand0_
[[FBASS, 참 좋은 베이스 브랜드], [FENDER, 참 좋은 베이스 브랜드], [FODERA, 참 좋은 베이스 브랜드], [MARLEAUX, 이것도 참 좋은 베이스 브랜드], [MATTISSON, 이것도 참 좋은 베이스 브랜드], [SANDBERG, 이것도 역시 참 좋은 베이스]]
```
오호라! 이런 방식으로 내부적으로 복잡한 조건을 따로 줄 수 있다.     

참고로 between은 양쪽에 설정한 값을 포함한다. 결국 number >= 1 AND number <= 3과 같다는 것을 앞서 브랜치에서 설명하지 않았기에 한번 언급하고 넘어간다.    

경험담으로 JPA와는 무관하게 이와 관련해서 쿼리에 이런 비지니스 로직이 들어가는게 좋냐 마냐라는 걸로 의견을 나눈 적이 있다.    

이게 사람마다 다 다르다.    

A: '쿼리에 저렇게 하나 코드레벨에서 무언가를 하든 뭐 차이가 나나?'    

B: '아무래도 성능의 저하가 있고 차라리 코드레벨에서 비지니스 로직으로 해결하는게 리소스에 이점이 있을 것이다.'    

나는 모르겠다. 대다수의 분들은 데이터를 가져오는데 최적화해서 코드레벨에서 비지니스 로직으로 처리하는게 더 좋다고 말하고 나의 개인적인 입장에서도 코드레벨에서 처리하는게 더 좋다고 본다.    

왜냐하면 성능의 이점 이런건 모르겠지만 최소한 코드의 흐름을 파악하는데 오히려 이게 낫지 않을까?     

원본 데이터의 변경을 가하지 않아서 무엇인지 알 수 있고 차후에 항상 변경되는 요구사항에 맞춰서 코드레벨단에서 변경을 하는게 오히려 더 유연하다고 보기 때문이다.    

뭐 답이 있을까마는....     

### .coalesce() 

mySql로 따지면 IFNULL이나 COALESCE()와 동일한 기능을 하는 녀석이다.    

즉, 해당 컬럼이 null이면 설정한 값으로 대체하라는 의미이다. ~~의외로 COALESCE()을 잘 모르는 분들이 많다. 대부분 IFNULL이 많은 듯~~   

```
SELECT br_code,
	   IFNULL(br_name, 'no name') AS br_name, 
	   -- 또는 COALESCE(br_name, 'no name') AS br_name
FROM basquiat_brand
```
쿼리는 위와 같이 사용하게 된다. 코드를 한번 살펴보자.    

```
List<Tuple> tuple = query.select( brand.code,	
							   brand.name.coalesce("no name").as("name")
							 )
	       		 	   .from(brand)
	       		 	   .fetch();
System.out.println(tuple.toString());
```
workbench에서 필드에 null을 세팅할 수 있는 기능이 있다. 그래서 에프베이스라는 값을 null로 세팅하고 코드를 실행하면  

```
Hibernate: 
    /* select
        brand.code,
        coalesce(brand.name,
        ?1) 
    from
        Brand brand */ select
            brand0_.br_code as col_0_0_,
            coalesce(brand0_.br_name,
            ?) as col_1_0_ 
        from
            basquiat_brand brand0_
[[FBASS, no name], [FENDER, 펜더], [FODERA, 포데라], [MARLEAUX, 말로우], [MATTISSON, 매티슨], [SANDBERG, 샌드버그]]
```
결과는 예상한대로 나왔다.    

### .nullif()    

사실 이거는 잘 안쓰는 편인데 회사에서 한 군데 쓰는 곳이 있어서 한번 소개해 본다.    

가령 이런 것이다. 어떤 컬럼과 뒤에 오는 값이 값으면 해당 컬럼은 null로 표시하는 것이다.    

```
SELECT br_code, 
	   NULLIF(br_name, '펜더') AS br_name
FROM basquiat_brand

result grid

br_code	  |	br_name
FBASS 	  |	에프베이스
FENDER 	  |	NULL
FODERA 	  |	포데라
MARLEAUX   |	말로우
MATTISSON  |	매티슨
SANDBERG   |	샌드버그
```
br_name이 '펜더'라면 null로 세팅해라 이런 의미이다.    

위 쿼리 결과를 포면 펜더쪽은 null로 세팅해서 나온다.     

회사에서 사용하는 이유는 특정 상품이 기획전이나 프로모션이 걸려 있을 경우에는 해당 모델명을 null처리를 해줘야 하는 경우가 있다.     

뭐 내부적인 이유인 듯 한데 queryDSL에서도 이것을 지원하니 필요하면 사용하면 된다.    


### 문자열 concat 및 number형식의 연산    

보통은 업데이트시 number의 경우에는 1씩 증가시킨다거나 (예를 들면 어떤 리퀘스트가 발생하면 좋아요를 하나 추가한다든가), 문자열을 합친다는가 하는 경우가 생길 수 있다.    

이런 경우 전자는 .add(), 후자는 .concat()을 사용할 수 있다. 

concat과 관련해서 append, prepend같은 함수를 겸해서 사용할 수 있다.    

연산과 관련해서 number형식이라면 .divide(), .abstract(), .multiply()등을 사용할 수 있으며 소수점 처리의 경우 그것을 처리하는 함수도 제공한다.    

1. .round() : 반올림      
2. .floor() : 버림     
3. .ceil()  : 올림     


일단 위에서 언급한 녀석들을 코드로 한번 확인해 보자.   

```
List<Tuple> tuple = query.select( brand.code,	
							   brand.name.append("append").as("append_name"),
							   brand.name.prepend("prepend").as("prepend_name"),
							   brand.name.concat("concat").as("concat_name"),
							   brand.name.concat(brand.number.stringValue()).as("other_type_concat_name"),
							   brand.number,
							   brand.number.add(1).as("add_number"),
							   brand.number.subtract(1).as("subtract_number"),
							   brand.number.divide(2).as("divide_number"),
							   brand.number.multiply(2).as("multiply_number")
							)
       		 	   	  .from(brand)
       		 	   	  .fetch();

```

결과는   

```
Hibernate: 
    /* select
        brand.code,
        concat(brand.name,
        ?1) as append_name,
        concat(?2,
        brand.name) as prepend_name,
        concat(brand.name,
        ?3) as concat_name,
        concat(brand.name,
        str(brand.number)) as other_type_concat_name,
        brand.number,
        (brand.number + ?4) as add_number,
        (brand.number - ?4) as subtract_number,
        (brand.number / ?5) as divide_number,
        (brand.number * ?5) as multiply_number 
    from
        Brand brand */ select
            brand0_.br_code as col_0_0_,
            concat(brand0_.br_name,
            ?) as col_1_0_,
            concat(?,
            brand0_.br_name) as col_2_0_,
            concat(brand0_.br_name,
            ?) as col_3_0_,
            concat(brand0_.br_name,
            cast(brand0_.number as char)) as col_4_0_,
            brand0_.number as col_5_0_,
            brand0_.number+? as col_6_0_,
            brand0_.number-? as col_7_0_,
            brand0_.number/? as col_8_0_,
            brand0_.number*? as col_9_0_ 
        from
            basquiat_brand brand0_
[
	[FBASS, 에프베이스append, prepend에프베이스, 에프베이스concat, 에프베이스1, 1, 2, 0, 0, 2], 
	[FENDER, 펜더append, prepend펜더, 펜더concat, 펜더2, 2, 3, 1, 1, 4], 
	[FODERA, 포데라append, prepend포데라, 포데라concat, 포데라3, 3, 4, 2, 1, 6], 
	[MARLEAUX, 말로우append, prepend말로우, 말로우concat, 말로우4, 4, 5, 3, 2, 8], 
	[MATTISSON, 매티슨append, prepend매티슨, 매티슨concat, 매티슨5, 5, 6, 4, 2, 10], 
	[SANDBERG, 샌드버그append, prepend샌드버그, 샌드버그concat, 샌드버그6, 6, 7, 5, 3, 12]
]

```
number타입의 경우에는 2로 나눴을 때 해당 number가 int타입이라 그냥 내부적으로 소수점을 버리는듯.     

참고로 concat의 경우에는 타입이 맞지 않으면 오류가 발생한다. 그래서 .concat(number type)처럼 뒤로 오는 값이 number type이면 .stringValue()로 변환하자.     

'저기요? 보니깐 toString()이 있는데 이걸로 해도 되지 않나요?'    

생성된 QBrand를 따라가다 보면 컬럼에 매핑되는 필드는 객체 타입으로 되어 있는 것을 알 수 있다.    

깊게 들어가 보지 않았지만 toString()을 걸게 되면 객체에 대한 값을 가져오게 되어 있는 듯 해서 실제로는 '에프베이스brand.number'처럼 경로에 대한 문자열이 찍혀서 나오게 된다.    

이것은 직접 테스트해보기 바란다.    

### .constants()    

자주 쓰는 일은 거의 없었던거 같은데 보통 UNION을 걸어야 하는 경우 쓰는 것중 하나가 그냥 고정값을 사용하는 것이다.     

그냥 간단하게 예를 들면    

```
SELECT br_code,
       number,
       'ONE' AS type
	FROM basquiat_brand
    WHERE number BETWEEN 1 AND 3
UNION	
SELECT br_code,
	   number,
	  'TWO' AS type
	FROM basquiat_brand
   WHERE number BETWEEN 4 AND 6

result grid
br_code   |number	| type
FBASS	 | 1	   	| ONE
FENDER    | 2	 	| ONE
FODERA    | 3	 	| ONE
MARLEAUX  | 4	 	| TWO
MATTISSON | 5	 	| TWO
SANDBERG  | 6	 	| TWO
```
이런 쿼리를 작성할 일은 없겠지만 쿼리 자체만으로는 그냥 SELECT br_code FROM basquiat_brand와 다를 바 없다.     

그냥 예를 들어서 저렇게 UNION을 중심으로 type을 두고 쿼리의 정체가 어디서 오는것인지 그냥 간단하게 만들어본 쿼리이다.     

자 그럼 'ONE' AS type같은 것을 쓸 수 있을까?    

물론 다음과 같이 하고 싶겠지만 ~~그건 니 생각~~     

```
List<Tuple> tuple = query.select( brand.code,	
        						   "A" 
	    						 )
 	   	 			   .from(brand)
       		 	   	   .fetch();
```
뻘겋게 IDE가 여러분에게 에러를 보여줄 것이다.    

사실 어떻게 보면 당연한 것이다. 실제로 QBrand객체를 따라가면 필드의 타입이 String이나 Integer가 아니다.     

DateTimePath, StringPath, NumberPath같이 객체로 구성되어져 있는 것을 보면 그냥 문자열 "A"를 넣으면 에러가 발생하는 것은 당연할 것이다.    

그래서 다음 코드를 한번 살펴보자.    

```
List<Tuple> tuple = query.select( brand.code,	
							   Expressions.constant("A"),
							   ExpressionUtils.as(Expressions.constant("A"), "constant")
							)
       		 	   	   .from(brand)
       		 	   	   .fetch();
```
맨 처음이 Expressions을 사용해 상수를 만드는 일반적인 사용법이고 밑에 부분이 생성한 상수를 ExpressionUtils를 활용해 감싸서 alias를 주는 방법이다.     

하지만 실제 날아가는 것은 생각한 것과는 좀 다르다.    

```
Hibernate: 
    /* select
        brand.code 
    from
        Brand brand */ select
            brand0_.br_code as col_0_0_ 
        from
            basquiat_brand brand0_
[[FBASS, A, A], [FENDER, A, A], [FODERA, A, A], [MARLEAUX, A, A], [MATTISSON, A, A], [SANDBERG, A, A]]
```
쿼리에는 날아가는 것이 보이지 않는다.     

아마 내부적으로는 최적화와 관련해서 실제 쿼리에는 실어서 보내지 않는 것 같다.    

이 부분은 내용을 한번 찾아봐야 할 듯 싶다.    

이 외에도 많은 것들이 존재하긴 하는데 실무에서 자주 쓰일 만한 것들 위주로 한번 소개해 봤다.    

그 외에도 aggregation, 즉 min, max, avg같은 집합과 관련된 녀석들이 있는데 이 녀석들은 GROUP BY와 관련된 내용에서 소개할 예정이다.

# SUB QUERY using queryDSL   

서브 쿼리는 말 그대로 쿼리 안에 인쿼리로 어떤 값을 세팅할 때 사용한다.    

예를 들면 다음과 같이 사용할 수 있다.

```
SELECT br_code,
       br_en_name,
       (SELECT COUNT(1) FROM basquiat_brand) AS br_count
	FROM basquiat_brand
	
result grid

br_code	   |	br_en_name  |	br_count

'FBASS',		'FBass',		'6'
'FENDER',	'Fender',	'6'
'FODERA',	'Fodera',	'6'
'MARLEAUX',	'Marleaux',	'6'
'MATTISSON',	'Mattisson',	'6'
'SANDBERG',	'Sandberg',	'6'
```
처럼 SELECT절에 사용하는 방법이 있다.     

물론 서브 쿼리이기 때문에 조건도 가능하다.    

```
SELECT id,
       partner_name,
       address,
       (SELECT COUNT(1) FROM basquiat_brand WHERE partner_id = id) AS '파트너가 가지고 있는 전체 브랜드 카운트'
	FROM basquiat_partner
	
result grid
id			 |	partner_name	 |	   address	   	 |	파트너가 가지고 있는 전체 브랜드 카운트
'MUSICFORCE',		'뮤직포스',			'청담동 어딘가 있다.',		'3'
'RIDINBASS',		'라이딩 베이스',		'합정동 어딘가 있다.',		'3'
```

이렇게 조건을 줄 수도 있다.    

### SELECT CLAUSE Sub Query

그럼 코드로 확인하자.    

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
        List<Tuple> tuple = query.select( partner.id,
									  partner.name,
									  partner.address,
									  JPAExpressions.select(brand.count())
                    								   .from(brand)
                    								   .where(partner.eq(brand.partner))
									)
						 	   	 .from(partner)
						 	   	 .fetch();
        	System.out.println(tuple.toString());
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
위와 같이 JPAExpressions을 활용하는 방법이다.    

결과를 보자.    

```
Hibernate: 
    /* select
        partner.id,
        partner.name,
        partner.address,
        (select
            count(brand) 
        from
            Brand brand 
        where
            partner = brand.partner) 
    from
        Partner partner */ select
            partner0_.id as col_0_0_,
            partner0_.partner_name as col_1_0_,
            partner0_.address as col_2_0_,
            (select
                count(brand1_.br_code) 
            from
                basquiat_brand brand1_ 
            where
                partner0_.id=brand1_.partner_id) as col_3_0_ 
        from
            basquiat_partner partner0_
[[MUSICFORCE, 뮤직포스, 청담동 어딘가 있다., 3], [RIDINBASS, 라이딩 베이스, 합정동 어딘가 있다., 3]]
```

물론 별칭도 위에서 상수를 세팅할 때 별칭을 주는 방식으로 서브 쿼리이 별칭을 줄 수 있다.

```
ExpressionUtils.as(
			  	JPAExpressions.select(brand.count())
			  				 .from(brand)
			  				 .where(brand.partner.id.eq(partner.id)),
			    "brand_total_count"
		  )
```

결과는 별칭 줬기 때문에 쿼리가 살짝 달라진다.    

```
Hibernate: 
    /* select
        partner.id,
        partner.name,
        partner.address,
        (select
            count(brand) 
        from
            Brand brand 
        where
            partner = brand.partner) as br_tot_count 
    from
        Partner partner */ select
            partner0_.id as col_0_0_,
            partner0_.partner_name as col_1_0_,
            partner0_.address as col_2_0_,
            (select
                count(brand1_.br_code) 
            from
                basquiat_brand brand1_ 
            where
                partner0_.id=brand1_.partner_id) as col_3_0_ 
        from
            basquiat_partner partner0_
[[MUSICFORCE, 뮤직포스, 청담동 어딘가 있다., 3], [RIDINBASS, 라이딩 베이스, 합정동 어딘가 있다., 3]]
```
위에서 날아간 쿼리를 보면 내부적으로 별칭을 주는 것을 알 수 있다.     

몇 버전인지는 모르겠지만 이전 버전에서는 SELECT절에 서브 쿼리를 사용하기 위해서는 ExpressionUtils.as를 감싸서 사용했던 걸로 기억하는데 현재 사용하는 최신 버전은 그마저도 필요가 없다.    

### WHERE CLAUSE Sub Query

자 그럼 이런 경우도 한번 살펴보자.    

어거지이긴 하지만 basquiat_brand의 number가 파트너사의 전체 카운트보다 큰 brand만 추려내는 요구 사항이 있다고 보자.    

```
SELECT br_code,
	   br_en_name,
       br_name,
       number
	FROM basquiat_brand
    WHERE (SELECT count(1) FROM basquiat_partner) < number

result grid
 br_code	   |  br_en_name | br_name	   |	number
'FODERA',	'Fodera',	 '포데라',		 '3'
'MARLEAUX',	'Marleaux',	 '말로우',		 '4'
'MATTISSON',	'Mattisson',	 '매티슨',		 '5'
'SANDBERG',	'Sandberg',	 '샌드버그',	 '6'
```
뭔가 좀 이상하긴 하지만 파트너사 전체 카운트가 2이기 때문에 number가 2보다 큰 데이터가 나왔다.    

자 그럼 코드로 작성을 해 보자.    

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
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
        	List<Tuple> tuple = query.select( brand.code,
        									  brand.enName,
        									  brand.name,
        									  brand.number
	    									)
				       		 	   	 .from(brand)
				       		 	   	 .where(brand.number.gt(JPAExpressions.select(partner.count())
				       		 	   			 		   	  				  .from(partner)
       		 	   			 		   	  				   )
			       		 	   			   )
				       		 	   	 .fetch();
        	System.out.println(tuple.toString());
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
코드를 보면 별반 다를 바가 없다.    

결과를 한번 보자.    

```
Hibernate: 
    /* select
        brand.code,
        brand.enName,
        brand.name,
        brand.number 
    from
        Brand brand 
    where
        brand.number > (
            select
                count(partner) 
            from
                Partner partner
        ) */ select
            brand0_.br_code as col_0_0_,
            brand0_.br_en_name as col_1_0_,
            brand0_.br_name as col_2_0_,
            brand0_.number as col_3_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.number>(
                select
                    count(partner1_.id) 
                from
                    basquiat_partner partner1_
            )
[[FODERA, Fodera, 포데라, 3], [MARLEAUX, Marleaux, 말로우, 4], [MATTISSON, Mattisson, 매티슨, 5], [SANDBERG, Sandberg, 샌드버그, 6]]
```
결국 우리가 이전 브랜치에서 배웠던 WHERE절에서 제공하는 API와의 조합으로 서브 쿼리를 사용할 수 있다.    

사실 서브 쿼리는 편하긴 하다. 하지만 예전 알던 DBA분이 정말 꼭 써야만 하는 이유가 있는 경우가 아니라면 대부분은 JOIN으로 해결할 수 있기 때문에 가급적이면 서브 쿼리는 잘 안쓰는 게 좋다고 이야기한 적이 있다. 특히 mySql과 관련해서 IN과 연계하는 서브 쿼리의 경우에는 인덱스를 타지 않아 조회 성능이 현저하게 떨어지기 때문이라는 것이다.    

Full Scan이 일어난다는 이야기를 언뜻 한거 같은데 자 그러면 이번이 이 브랜치의 마지막이 될 것이기 때문에 한번 위 쿼리를 한번 플랜을 떠서 살펴보는 시간을 갖겠다.    

```
EXPLAIN
SELECT id,
       partner_name,
       address,
       (SELECT COUNT(1) FROM basquiat_brand WHERE partner_id = id) AS '파트너가 가지고 있는 전체 브랜드 카운'
	FROM basquiat_partner;
```
우리가 queryDSL로 변환했던 쿼리이다. mySQL에서 플랜을 보는 방법은 쿼리 위에 'EXPLAIN' 키워드를 두고 쿼리를 하면 된다.

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/4.query-dsl-select-sub-query/capture/capture1.png)    

일일이 설명할려니 일단 필력이 딸리고 지식도 살짝 딸려서 링크 하나 걸어본다.    

[EXPLAIN을 사용해서 쿼리 최적화 하기](http://www.mysqlkorea.com/sub.html?mcode=manual&scode=01&m_no=21444&cat1=7&cat2=217&cat3=227&lang=k)     

[MySQL 옵티마이저 구조](https://cheese10yun.github.io/mysql-explian/)

링크를 보면 몇 몇 중요한 키포인트가 있는데 그 중 하나가 바로 type이다.    

id를 기준으로 보면 대상 테이블 basquiat_partner에 대해서 ALL이다. 이것은 보통 full scan이 일어났을 경우 발생하는데 전체 데이터를 다 훝어봤다는 의미이다. 지금이야 데이터가 몇 건 안되지만 만일 수십, 수백만건이상이 있는 테이블을 full scan을 했다고 생각해 보자.    

```
EXPLAIN  
    SELECT id,
       partner_name,
       address,
       COUNT(bb.br_code) AS count
	FROM basquiat_partner bp
    JOIN basquiat_brand bb ON bp.id = bb.partner_id
    GROUP BY id
```
서브 쿼리를 JOIN으로 풀고 aggregation, 즉 카운트라는 집합 함수를 사용했기 때문에 파트너사의 id를 GROUP BY로 잡았다.      

그럼 한번 플랜을 떠 보자.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/4.query-dsl-select-sub-query/capture/capture2.png)    

실제로 쿼리를 날리면 같은 결과지만 full scan을 타지 않고 type이 index이다.     

자 그럼 SELECT절의 서브 쿼리를 join방식으로 발라보자.    

```
List<Tuple> tuple = query.select( partner.id,
							   partner.name,
							   partner.address,
							   brand.count().as("br_tot_count")
							 )
			 	   	   .from(partner)
			 	   	   .join(brand).on(partner.id.eq(brand.partner.id)) // .join(brand).on(partner.eq(brand.partner)) 객체 자체를 비교해도 된다. 
			 	   	   .groupBy(partner.id) // .groupBy(partner) 객체는 이미 pk를 알고 있다.
			 	   	   .fetch();
```
아직 join과 groupBy를 배우지 않았지만 변환하면 저렇게 바꿀 수 있다. 결과는?   

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        partner.id,
        partner.name,
        partner.address,
        count(brand) as br_tot_count 
    from
        Partner partner   
    inner join
        Brand brand with partner.id = brand.partner.id 
    group by
        partner.id */ select
            partner0_.id as col_0_0_,
            partner0_.partner_name as col_1_0_,
            partner0_.address as col_2_0_,
            count(brand1_.br_code) as col_3_0_ 
        from
            basquiat_partner partner0_ 
        inner join
            basquiat_brand brand1_ 
                on (
                    partner0_.id=brand1_.partner_id
                ) 
        group by
            partner0_.id
[[MUSICFORCE, 뮤직포스, 청담동 어딘가 있다., 3], [RIDINBASS, 라이딩 베이스, 합정동 어딘가 있다., 3]]
```

사실 예제 자체가 좀 어거지라 참담하기도 하고 데이터 자체도 유의미하지 않아서 큰 차이점이 없어 보이지만 실제로 서브 쿼리가 많은 경우에 성능 저하를 체감한다.     

최근 이와 관련해서 재직중인 회사에서는 DB와 관련 최적화 튜닝을 위해 초빙했던 그 분도 이 서브 쿼리를 분석해 조인 형식으로 풀어가는데 상당히 많은 시간을 할애하기도 했었다.     

일단 이 브랜치에서 해야 할 분량은 얼추 맞춘 거 같다.    

예제로 사용한 WHERE절의 서브 쿼리는 실력이 딸려서 JOIN으로 해결못함. ~~안되는건지 못한 건지 모르는게 함정.~~

아참 쿼리에서 FROM의 Inline View로도 서브 쿼리가 가능하다. 경험이 있는 분들은 봤을 수가 있는데    

예를 들면 어거지이긴 하지만    

```
SELECT subQuery.*
	FROM (SELECT bb.br_code, 
				bb.br_en_name,
                  bp.id AS partner_id,
                  bp.address
			FROM basquiat_brand bb 
			JOIN basquiat_partner bp ON bb.partner_id = bp.id
	      ) subQuery
```
요런 쿼리들을 볼 수 있는데 저것을 보통 인라인 뷰라고 보통 지칭을 한다.    

아쉽게도 하이버네이트에서는 이것을 지원하지 않는다.    

queryDSL은 JPQL문법을 보다 개발자에 편의에 맞춰서 생긴 만큼 JPQL에서 지원하지 않는 것은 사용할 수 없다. 아마도 해볼려고 해도 뻘건 에러를 볼 것이다.    

언제가는 뭐 지원할지 모르겠지만 인라인 뷰 형식의 서브 쿼리를 꼭 써야 하는 경우라면 최대한 조인이나 네이티브 쿼리를 사용해야한다.     

~~에이 설마? 별로 좋은 건 아니라고 해도 언젠가는 지원하겠지...~~ 

이렇게 이번 브랜치는 마무리하고자 한다.    

다음에는 Join과 Aggregation, 즉 집합과 그와 관련해서 실과 바늘같이 따라오는 GROUP BY에 대해서 알아볼 까 한다.     
