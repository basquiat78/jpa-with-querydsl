# JOIN using queryDSL    

지금까지 진행하면서 그냥 의식의 흐름대로 진행을 했더니 문득 순서상 이게 먼저 나왔어야 하는게 아닌가 싶다.    

이미 앞서 예제에서도 join을 많이 썼던지라....     

뭐 그건 중요한건 아니니까!    

### 시작하기 앞서    

일단 JOIN과 집합 함수와 GROUP BY에 대한 문법을 알아보기 전에 관련 SQL에 대한 기초 지식을 먼저 언급한다.     

따라서 원하는 데이터를 어떤 방식으로든 뽑아 낼수 있고 상상할 수 있는 분들은 인트로 부분은 건너띄고 문법을 설명하는 파트로 그냥 건너띄어도 상관없다.     

또는 '나는 SQL 이런거 모르겠고 그냥 queryDSL문법이 중요해'라고 생각하시는 분 역시 건너띄어도 상관없다.    

이 브랜치의 목적은 queryDSL의 문법을 살펴보는 것이지만 처음 입문하시는 분들중 SQL이 약하신 분들은 최소한 이런 것들을 사용할 때 데이터가 어떻게 나올지 어떤 형식으로 사용할지는 알아두는 것이 좋다.     

### create table and insert data

basquiat_item, basquiat_delivery_policy 테이블 만들기    

```
최신 버전 mysql
CREATE TABLE `basquiat_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'item identity',
  `it_name` varchar(255) NOT NULL DEFAULT '' COMMENT '상품명',
  `price` int DEFAULT 0 COMMENT '',
  `created_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '상품 생성일',
  `updated_dt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '상품 수정일', 
   PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1;


CREATE TABLE `basquiat_delivery_policy` (
  `item_id` bigint NOT NULL,
  `fee` int DEFAULT 0 COMMENT '기본배송비',
  `add_fee` int DEFAULT 0 COMMENT '도서 산간 추가배송비',
  `created_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '정책 등록일',
  `updated_dt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정책 수정일',
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_basquiat_item` FOREIGN KEY (`item_id`) REFERENCES `basquiat_item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

안정적인 버전의 mysql
CREATE TABLE `basquiat_item` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'item identity',
  `it_name` varchar(255) NOT NULL DEFAULT '' COMMENT '상품명',
  `price` int(11) DEFAULT 0 COMMENT '',
  `created_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '상품 생성일',
  `updated_dt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '상품 수정일', 
   PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1;


CREATE TABLE `basquiat_delivery_policy` (
  `item_id` bigint(11) NOT NULL,
  `fee` int(11) DEFAULT 0 COMMENT '기본배송비',
  `add_fee` int(11) DEFAULT 0 COMMENT '도서 산간 추가배송비',
  `created_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '정책 등록일',
  `updated_dt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정책 수정일',
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_basquiat_item` FOREIGN KEY (`item_id`) REFERENCES `basquiat_item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

```

주목할 부분이 아마도 지금까지 JPA를 공부하다 보면 지겹도록 보는 alter table 쿼리중 외래키 설정하는 쿼리를 봤을 것이다.    

```
CONSTRAINT `fk_basquiat_item` FOREIGN KEY (`item_id`) REFERENCES `basquiat_item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
```
요거는 아예 테이블 생성시 외래키를 잡는 형태이다.     

외래키가 잡혀 있으면 그 대상 테이블 즉 basquiat_item테이블은 그냥 테이블을 드랍시킬 수 없다.    

basquiat_delivery_policy에 걸려있는 외래키 설정를 먼저 드랍하거나 basquiat_delivery_policy테이블을 먼저 지워야 한다.     

또한 데이터 역시 마찬가지이다. 만일 basquiat_item에 id가 1인 데이터가 있고 basquiat_item_delivery도 기본키이자 외래키로 잡혀있는 item_id가 1인 데이터가 있다면 basquiat_item테이블에서 id가 1인 데이터를 지우려고 하면 지워지지 않는다.     

CASCADE와 관련되어 있기 때문이다. ~~일단 꺠알같은 별거 아닌 지식~~     

시간이 좀 남아서 최신 mySql을 한번 깔아봤는데 지금까지 볼 수 없었던 경고 메세지가 떠서 이유를 찾아봤다.    

[stackoverflow](https://stackoverflow.com/questions/58938358/mysql-warning-1681-integer-display-width-is-deprecated)     

예전 알고 지내시던 DBA분이 하셨던 이야기가 참 다시 생각난다.     

'최신 버전이 무조건 좋지는 않은거 같다. 일단은 안정적인 버전을 쓰는게 차라리 나아 보이거든'     

자 위와 같이 테이블을 두개를 생성하고 다음과 같이 데이터를 밀어 넣어보자.    

```
INSERT INTO basquiat_item 
	(
     it_name, price
	) 
VALUES 
	(
	 'Fodera Emperor2 5 Deluxe', 15000000
    ),
    (
	 'Fender Custom Shop 63', 5500000
    );

INSERT INTO basquiat_delivery_policy
	(
     item_id, fee, add_fee
	)
VALUES
	(
     1, 2000, 2000
	),
    (
     2, 2500, 3000
	)    
    
```

조인의 경우에는 정말 많은 조인의 종류가 있다.     

CROSS JOIN, INNER JOIN, NATURAL JOIN, LEFT OUTER JOIN, RIGHT OUTER JOIN, SELF JOIN, FULL OUTER JOIN (mysql 지원안함) 등등등...    

그전에 이거 먼저 보고 가자.    

### Cartesian    

곱집합이라고도 한다.       

보통 이 경우에는 모든 경우의 수를 계산, 즉 위에 생성된 테이블을 기준으로 각 테이블의 row수의 곱만큼 출력을 하게 된다.    

즉 2 * 2니깐 4개의 row수를 출력하게 된다.    

이것을 표현하는 방식은 다음과 같이 어떤 조건을 주지 않고 쿼리를 작성했을 경우이다.   

```
SELECT item.*, 
	   policy.*
	FROM basquiat_item item
	JOIN basquiat_delivery_policy policy;

SELECT item.*, 
	   policy.*
FROM basquiat_item item,
     basquiat_delivery_policy policy;
     
SELECT item.*, 
	   policy.*
	FROM basquiat_item item 
	CROSS JOIN basquiat_delivery_policy policy;
```

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture1.png)    

물론 여기에 WHERE절을 사용해서 조건을 줄 수도 있다.    

여기서 중요한 것은 바로 조건을 어떻게 주느냐에 따라 이것은 INNER JOIN이 될 수도 있고 CROSS JOIN이 될 수도 있고 SELF JOIN이 될 수도 있다.    

결국 중요한 것은 이 조건을 어떻게 주느냐에 따라 결정된다.    

하지만 실무에서는 이보다는 가장 많이 사용하게 되는 것이 INNER JOIN, LEFT OUTER JOIN이 될 것이고 간혹 RIGHT OUTER JOIN (보통 LEFT JOIN으로 쇼부친다)이 될 것이다.    

그럼 이것을 왜 설명을 하는 것인가요? ~~위에 설명했잖아요?~~        

자 이것이 가장 기본적인 베이스가 된다.   

그러면 이것에 대해서 하나씩 알아가 보자.    

## INNER JOIN    

일단 이거와 비교대상이 되는 NATURAL JOIN은 제외한다. 일반적으로 NATURAL JOIN의 경우에는 조인될 테이블의 컬럼명이 같으면 무조건 그 컬럼명들을 조건으로 거는 방식이다.    

아..그냥 넘어갈려고 했지만 일단 NATURAL JOIN이 어떤 식인지 알아보자면   

예로 다음과 같은 테이블이 있다고 생각해 보자.    

```

TABLE A [id, name, model, prop1, prop2, prop3....] <-- 컬럼명을 나열

TABLE B [id, name, model, prop1, test1, test2....] <-- 컬럼명을 나열

컬럼명이 같은 컬럼 [id, name, model, prop1]

```

이것을 NATURAL JOIN으로 하게 되면 내부적으로

```
SELECT a.*,
       b.*
	FROM A a
	NATURAL JOIN B b;

이것이 밑에 INNER JOIN으로 변경된다.

SELECT a.*,
       b.*
	FROM A a
	INNER JOIN B b ON b.id = a.id
	              AND b.name = a.name
	              AND b.model = a.model
	              AND b.prop1 = a.prop1;
```
오! 뭔가 좋아보이는데?      

하지만 이렇다는 것은 의도치 않은 결과를 초래할 수 있을 뿐만 아니라 테이블 설계가 잘못되었다고 보는 경향이 좀 있다.      

따라서 정말 정말로 특이한 케이스가 아니라면 이것을 여러분이 쓸 일이 있을런지도 의문이다.     

자 그럼 우리는 이제부터 어떤 방식으로 쿼리를 짜는지 기본적인 방법부터 알아보자. ~~이미 위에 있네???~~    

```
SELECT *
	FROM [table 명] [별칭] 
	JOIN [table 명] [별칭] ON [별칭].[컬럼] = [별칭].[컬럼]
	                    AND [별칭].[컬럼] = [별칭].[컬럼]
```

또는 위에서 우리가 본 Cartesian에서 예제로 둔    

```
SELECT item.*, 
	   policy.*
	FROM basquiat_item item,
     	 basquiat_delivery_policy policy
  WHERE item.id = policy.item_id;
  
SELECT item.*, 
	   policy.*
	FROM basquiat_item item 
	CROSS JOIN basquiat_delivery_policy policy
  WHERE item.id = policy.item_id;	       
```
과 같이 표현할 수도 있다.     

'저기요 별칭을 줄려면 AS가 붙어야 하지 않나요? 그리고 INNER라는 키워드가 빠졌어요!'     

AS의 경우에는 띄워쓰기로 생략이 가능하다. 물론 컬럼에서도 마찬가지이다. 그리고 INNER의 경우에는 생략이 가능하다.     

그리고 AND의 경우에는 만일 유니크 제약 조건이 2개 이상 걸려있을 경우 AND로 조건을 추가 할 수 있다. 물론 조회할 데이터의 성격에 따라 유니크 제약 조건이 없는 경우라도 언제든지 조건 추가 가능하다.         

그럼 이런 의문이 들 것이다.     

'INNER JOIN, LEFT OUTER JOIN같은 녀석이 있는 것을 보니 뭔가 다른 점이 있는 건가?'     

그렇다.

당연히 차이가 있다.     

이것을 이해하기 위해서는 우리가 중학교 때 배운 집합에 대한 개념이 필요하다.     

'알고 지내는 DBA님: DB는 말이죠. 결국 집합이에요. 교집합, 합집합같은 개념이 들어가요.'     

INNER JOIN은 이 집합을 표현하는 벤다이어그램에서 교집합에 해당한다.     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture2.png)    

그림처럼 2개의 테이블의 교집합을 말이다.    

일단 다음과 같이 쿼리를 짜보자.    

```
SELECT item.*, 
	   policy.*
	FROM basquiat_item item 
	INNER JOIN basquiat_delivery_policy policy ON item.id = policy.item_id;  
```

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture3.png)    
   
두 개의 테이블에서 아이디가 같은 녀석을 가져오는 조건을 걸어서 교집합을 가져왔다. ~~음? 당연한거 아니에요?~~       

여기까지는 그렇다 치자. 자 그러면 다음과 같이 인서트 쿼리를 하나만 더 날려보고 위 쿼리를 다시 한번 날려보자.     

```
INSERT INTO basquiat_item 
	(
     it_name, price
	) 
VALUES 
	(
	 'Mayones Jabba Custom 5', 6000000
    ),
    (
	 'Alleva Coppla LG5', 7000000
    );
```

그리고 다시 

```
SELECT item.*, 
	   policy.*
	FROM basquiat_item item 
	INNER JOIN basquiat_delivery_policy policy ON item.id = policy.item_id;  
```

어떤 일이 벌어질까?    

위에 이미지 결과와 똑같다???    

어라? 분명 basquiat_item에는 새로 유입된 데이터가 있는데도?     

바로 INNER JOIN은 교집합이기 때문이다. 따라서 조인을 건 대상 테이블인 basquiat_delivery_policy에는 그와 매핑되는 데이터가 없기 때문에 새로 유입된 정보는 조회 결과에서 제외되는 것이다.     

'어허... 이러면 안되는데요? 왜냐하면 배송 정보가 없더라도 회사 내부 방침으로 이런 경우에는 기본적인 배송비와 산간 추가 배송비가 책정되어 있습니다. 그리고 이렇게 되면 조회시에 판매중인 상품이라도 조회가 안될거 같은데요?'     

그러면 조인 전략을 바꿔야 한다.     

그것은 바로 밑에     

## LEFT OUTER JOIN     

벤다이어그램으로 표현하면 다음과 같다.     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture4.png)    
 
어떻게 보면 차집합과 교집합의 합이라고도 볼 수 있다.     

즉 좌측 테이블인 baquiat_item을 기준으로 basquiat_item의 정보와 교집합의 정보를 전부 가져오게 된다.     

```
SELECT item.*, 
	   policy.*
	FROM basquiat_item item 
	LEFT JOIN basquiat_delivery_policy policy ON item.id = policy.item_id;  
```

일단 쿼리를 날려보자. LEFT OUTER JOIN이 정석적인 작성법이지만 간략하게 LEFT JOIN만 명시해도 상관없다.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture5.png)     

하지만 쿼리 결과를 잘 보면 없는 부분은 전부 null로 표시되어 있다.     

간략하게 LEFT JOIN의 특성을 알아 봤다.  

'저기 그라믄요 RIGHT OUTER JOIN은 언제 쓰나요?'    

~~성격이 급하시군요?~~    

## RIGHT OUTER JOIN

당연히 말로 보면 LEFT OUTER JOIN의 반대라는 것을 알 수 있다. 이것은 기준의 문제이다.     

벤다이어그램은 생략하겠다. 사실 PPT로 그리려니 너무 귀찮다.         

위 쿼리와 반대로 basquiat_delivery_policy가 기준이 되는 테이블이라면     

```
SELECT item.*, 
	   policy.*
	FROM basquiat_delivery_policy policy
	LEFT OUTER JOIN basquiat_item item ON policy.item_id = item.id; 
```
이런 쿼리가 있다고 생각해 보자.     

이거 날리면 어떻게 될까?    

잘 생각해보면 좌측 테이블 (basquiat_delivery_policy)과 양쪽 테이블의 교집합의 합이기 때문에 basquiat_item에 수백만 건의 데이터가 있다해도 결국 2건의 데이터만 나올 것이다.     

당연하죠?     

네 당연해요. 이해 못하면 안되요 ㅜㅜ. 그렇지만 좌측 테이블 (basquiat_delivery_policy)입장에서는 전부 다 가져와야 할 필요가 있다면 어떻게 해야할까?     

물론 쿼리를 변경하면 된다.     

하지만 어떤 이유로 쿼리를 변경하기가 쉽지 않다면 그때 사용하면 된다.     

```
SELECT policy.*, 
	   item.*
	FROM basquiat_delivery_policy policy
	RIGHT OUTER JOIN basquiat_item item ON policy.item_id = item.id;     
```

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture6.png)     

이 떄 주의할 점은 기준이 되는 basquiat_delivery_policy 테이블의 RIGHT OUTER 했기 때문에 컬럼을 표시하는 순서가 위와 같은면 결과가 좀 괴상하게 나오게 된다.     

따라서 

```
SELECT item.*, 
	   policy.*
	FROM basquiat_delivery_policy policy
	RIGHT OUTER JOIN basquiat_item item ON policy.item_id = item.id;
```
와 같이 컬럼의 순서를 바꿔 줘야 한다.     

이렇게 보면 각 JOIN을 어떨 때 사용해야 하는지를 알 수 있고 데이터가 어떤 조인을 걸었을 떄 어떤 결과가 나오는지를 어느정도 상상을 할 줄 알아야 한다.     

물론 이런거 없이 queryDSL의 문법으로 요리조리 결과를 봐가면서 분석해도 된다.     

그리고 OUTER JOIN(LEFT, RIGHT포함) 이와 관련된 썰을 좀 풀자면 예전 모 대기업에서 선임으로 있던 분과 DBA담당분과 왜 OUTER JOIN을 사용하냐 문제로 싸운 적이 있다.     

잘 보면 알겠지만 OUTER JOIN을 사용하는 경우에는 데이터가 없으면 null로 채워서 결과를 가져오게 되는데 이게 성능 저하를 불러 온다는 것이다.     

어플리케이션의 비지니스 로직상 정책이 없으면 그냥 item정보만 밀어넣는 경우도 있기 때문인데 DBA분은 그러면 그런 경우라면 해당 조인 대상 테이블에도 빈값을 넣으라고 하고 우리 선임은 왜 빈값을 넣느니 마니, 그럼 없을 때는 정책적으로 마련해서 정책상 기본값을 넣으면 된다라고 엄청 싸워대가지고.........     

여러분 생각은 어떤가?      

자 그럼 구글신에게 물어보면 나올 지식들을 굳이 긴 여정에 작성을 해 놨지만 이제부터는 이런 지식을 바탕으로 queryDSL에서 join을 어떻게 쓰는지 보러 가자.    

## INNER JOIN using queryDSL    

```
List<Brand> selectBrand = query.select(brand)
					 	   	.from(brand)
					 	   	//.join(partner).on(brand.partner.id.eq(partner.id)) 
					 	   	//.join(brand.partner, partner).on(brand.partner.id.eq(partner.id))
					 	   	.innerJoin(partner).on(brand.partner.id.eq(partner.id)) 
					 	   	.fetch();

System.out.println(selectBrand.toString());
selectBrand.stream().map(br -> br.getPartner().toString())
					.forEach(System.out::println);
```
거두절미하고 코드부터 보자.    

위에 코드를 보면 주석처리된 부분을 볼 수 있는데 쿼리에서 말한것처럼 INNER JOIN에 관해서는 .join() 또는 .innerJoin()으로 쓸 수 있다.    

지금 보면 .join(partner)과 .join(brand.partner, partner) 이 코드가 눈에 보일 것이다.     

뒤에 있는 partner는 별칭을 의미하는데 ON절을 사용할 때는 .join(partner)처러머 엔티티를 하나만 넣는다.    

그리고 이것은 앞으로 설명한 LEFT OUTER, RIGHT OUTER일 때도 동일하다.     

물론 넣어줘도 쿼리는 실행되지만 저것을 준 것과 안 준 것은 날아가는 쿼리가 좀 다르다.     

이것은 뒤쪽에서 자세하게 한번 설명해 볼까 한다.     

일단 위에 코드를 그대로 실행하면    

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    inner join
        Partner partner with brand.partner.id = partner.id */ select
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
                on (
                    brand0_.partner_id=partner1_.id
                )
[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
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
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
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
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
```
지금 코드에서 

```
.join(brand.partner, partner).on(brand.partner.id.eq(partner.id))
//.innerJoin(partner).on(brand.partner.id.eq(partner.id)) 
```
위 코드를 주석해제하고 기존에 실행한 코드를 주석해보자.   

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    inner join
        brand.partner as partner with brand.partner.id = partner.id */ select
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
                and (
                    brand0_.partner_id=partner1_.id
                )
[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
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
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
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
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
```

결과는 같지만 쿼리가 살짝 다른 것을 알 수 있다.     

자 그럼 실제로 INNER JOIN인 것이야? 라는 의문이 들수 있기 때문에 우리는 BRAND를 하나 인서트를 한 것이다.    

하지만 Partner정보 없이 인서트를 한다.

```
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
        	
        	Brand newBrand = Brand.builder().code("MAYONES")
        									.name("마요네즈")
        									.enName("Mayones")
        									.build();
        	em.persist(newBrand);
        	em.flush();
        	em.clear();
        	
        	Brand select = em.find(Brand.class, newBrand.getCode());
        	System.out.println(select.toString());
        	
        	
        	JPAQueryFactory query = new JPAQueryFactory(em);
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");
        	List<Brand> selectBrand = query.select(brand)
								 	   	   .from(brand)
								 	   	   //.join(partner).on(brand.partner.id.eq(partner.id)) 
								 	   	   .join(brand.partner, partner).on(brand.partner.id.eq(partner.id))
								 	   	   //.innerJoin(partner).on(brand.partner.id.eq(partner.id)) 
								 	   	   .fetch();
        	
        	System.out.println(selectBrand.toString());
        	selectBrand.stream().map(br -> br.getPartner().toString())
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

결과는 어떻게 나올까?    

이미 위에 JOIN과 관련된 선수 지식을 잘 봐왔다면 어느정도 예상이 되어야 한다.

```
Hibernate: 
    /* insert io.basquiat.model.Brand
        */ insert 
        into
            basquiat_brand
            (br_en_name, launched_at, br_name, number, partner_id, updated_at, br_code) 
        values
            (?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        brand0_.br_code as br_code1_0_0_,
        brand0_.br_en_name as br_en_na2_0_0_,
        brand0_.launched_at as launched3_0_0_,
        brand0_.br_name as br_name4_0_0_,
        brand0_.number as number5_0_0_,
        brand0_.partner_id as partner_7_0_0_,
        brand0_.updated_at as updated_6_0_0_ 
    from
        basquiat_brand brand0_ 
    where
        brand0_.br_code=?
Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null)
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    inner join
        brand.partner as partner with brand.partner.id = partner.id */ select
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
                and (
                    brand0_.partner_id=partner1_.id
                )
[
Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
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
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
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
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
```

결과를 보면 교집합이기 때문에 파트너 정보가 없는 신규 등록한 브랜드의 경우에는 조회되지 않는다.     

## LEFT OUTER JOIN using queryDSL     

자 그럼 위에서 실행했던 코드에서 신규 브랜드를 인서트하고 셀렉트해서 콘솔에 찍는 로직을 일단 지우자.     

```
JPAQueryFactory query = new JPAQueryFactory(em);
System.out.println("queryDSL로 뭔가 하기 직전!!!");
List<Brand> selectBrand = query.select(brand)
					 	   	.from(brand)
					 	   	.leftJoin(partner).on(brand.partner.id.eq(partner.id)) 
					 	   	.orderBy(partner.name.asc().nullsLast())
					 	   	.fetch();

System.out.println(selectBrand.toString());
selectBrand.stream().forEach(br -> {
	System.out.println("============ 콘솔 찍기 시작 ================");
	System.out.println(br.toString());
	if(br.getPartner() != null) {
		System.out.println(br.getPartner().toString());
	} else {
		System.out.println("널 보내고~");
	}
	System.out.println("============ 콘솔 찍기 끗 ================");
});
tx.commit();
```
LEFT JOIN을 걸었다. 그리고 파트너사의 이름으로 오름차순으로 정렬하는데 null인 녀석은 마지막으로 정렬한다.     

즉, 이 말대로라면 신규 브랜드인 'Mayones'는 파트너사가 null이기 때문에 마지막으로 정렬될 것이다.

진짜 그런지 실행해볼까?         

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    left join
        Partner partner with brand.partner.id = partner.id 
    order by
        partner.name asc nulls last */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ 
        left outer join
            basquiat_partner partner1_ 
                on (
                    brand0_.partner_id=partner1_.id
                ) 
        order by
            case 
                when partner1_.partner_name is null then 1 
                else 0 
            end,
            partner1_.partner_name asc
[Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null)]
============ 콘솔 찍기 시작 ================
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null)
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
============ 콘솔 찍기 끗 ================
============ 콘솔 찍기 시작 ================
Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
============ 콘솔 찍기 끗 ================
============ 콘솔 찍기 시작 ================
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
============ 콘솔 찍기 끗 ================
============ 콘솔 찍기 시작 ================
Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null)
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
============ 콘솔 찍기 끗 ================
============ 콘솔 찍기 시작 ================
Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
============ 콘솔 찍기 끗 ================
============ 콘솔 찍기 시작 ================
Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
============ 콘솔 찍기 끗 ================
============ 콘솔 찍기 시작 ================
Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null)
널 보내고~
============ 콘솔 찍기 끗 ================
```

## RIGTH OUTER JOIN using queryDSL     

아 뻔하지만 일단 테스트는 해보자.     

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
        	List<Tuple> tuple = query.select(brand, partner)
							   .from(partner)
							   .rightJoin(brand).on(brand.partner.id.eq(partner.id)) 
							   .orderBy(partner.name.asc().nullsLast())
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
이번 테스트는 튜플 형식으로 객체 자체로 가져오는 방법을 한번 구현해 봤다.     

이 방식은 앞에서 실행했던 코드에도 적용할 수 있다.

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        partner 
    from
        Partner partner   
    right join
        Brand brand with brand.partner.id = partner.id 
    order by
        partner.name asc nulls last */ select
            brand1_.br_code as br_code1_0_0_,
            partner0_.id as id1_1_1_,
            brand1_.br_en_name as br_en_na2_0_0_,
            brand1_.launched_at as launched3_0_0_,
            brand1_.br_name as br_name4_0_0_,
            brand1_.number as number5_0_0_,
            brand1_.partner_id as partner_7_0_0_,
            brand1_.updated_at as updated_6_0_0_,
            partner0_.address as address2_1_1_,
            partner0_.entry_at as entry_at3_1_1_,
            partner0_.partner_name as partner_4_1_1_,
            partner0_.updated_at as updated_5_1_1_ 
        from
            basquiat_partner partner0_ 
        right outer join
            basquiat_brand brand1_ 
                on (
                    brand1_.partner_id=partner0_.id
                ) 
        order by
            case 
                when partner0_.partner_name is null then 1 
                else 0 
            end,
            partner0_.partner_name asc
[
	[
	 Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null),
	 Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
	], 
	
	[
	 Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null),
	 Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
	], 
	
	[
	 Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null),
	 Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
	], 
	
	[
	 Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null),
	 Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
	], 
	
	[
	 Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
	 Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
	], 
	
	[
	 Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
	 Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
	], 
	 
	[
	 Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null), 
	 null
	]
]
```

이렇게 한번 가장 빈번하게 사용하는 방식을 알아봤다.     

'저기 근데요. 이건 잘 알겠지만 위에서 설명한 Cartesian, 즉 곱집합 설명했을 때의 방식은 설마 못 쓰는건가요? 상황에 따라 쓸 수도 있을거잖아요?'     

물론 가능하다.    

```
System.out.println("queryDSL로 뭔가 하기 직전!!!");
List<Tuple> tuple = query.select(brand, partner)
			 	   	   .from(brand, partner)
					   .fetch();
System.out.println(tuple.size());
System.out.println(tuple.toString());
```

FROM절에 저렇게 ','로 마치 다음 쿼리처럼 

```
SELECT brand.*,
       partner.*
	FROM basquiat_brand brand,
         basquiat_partner partner
```
처럼 구성할 수 있다.     

결과
     
```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        partner 
    from
        Brand brand,
        Partner partner */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ cross 
        join
            basquiat_partner partner1_
14            
[
[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
], 

[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 

[Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)]]
```
곱집합이니 현재 brand 7 * partner 2 = 14개가 나온다.     

실제 쿼리는?    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/5.query-dsl-join-and-aggregation/capture/capture7.png)    

~~결과는 똑같죠?~~    

위에서도 설명했지만 조건을 어떻게 거냐에 따라 inner조인이 될수도 있는데 이미 쿼리를 나간 것을 보면 내부적으로 cross join으로 나간 것을 알 수 있다.     

하지만 조건을 주면 어떨까?     

```
List<Tuple> tuple = query.select(brand, partner)
			 	   	   .from(brand, partner)
			 	   	   .where(partner.id.eq(brand.partner.id))
					   .fetch();
System.out.println(tuple.size());
System.out.println(tuple.toString());
```

결과가 심히 궁금하구나.     

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        partner 
    from
        Brand brand,
        Partner partner 
    where
        partner.id = brand.partner.id */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ cross 
        join
            basquiat_partner partner1_ 
        where
            partner1_.id=brand0_.partner_id
6
[
	[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null),   Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)]

]

```
INNER JOIN과 몹시 똑같아 보인다.     

그렇다면 다음과 같은 경우도 한번 생각해 볼 수 있다.     

```
List<Tuple> tuple = query.select(brand, partner)
			 	   	   .from(brand)
			 	   	   .join(brand.partner, partner)
					   .where(brand.partner.name.eq(partner.name))
			 	   	   .fetch();
System.out.println(tuple.size());
System.out.println(tuple.toString());

```

결과는 ?

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        partner 
    from
        Brand brand   
    inner join
        brand.partner as partner 
    where
        brand.partner = partner */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id 
        where
            partner1_.partner_name=partner1_.partner_name
6
[
	[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)], 
	
	[Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null), Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)]]

```

어 근데 궁금증이 생길 것이다.    

이제부터 join시에 .join(brand.partner, partner)과 .join(brand.partner)의 차이점이 무엇인지 그리고 언제 사용할지 알아보자.     

보통 우리가 연관관계가 있는 테이블이나 조인할 조건이 갖춰진 테이블끼리의 조인시 날쿼리로 쿼리를 짠다고 하면 보통    

```
SELECT a.*,
       b.*
    FROM A a
    JOIN B b ON b.a_id = a.id
```
로 짜게 된다.     

즉 A 테이블의 pk인 id가 B 테이블이 외래키로 a_id를 관리한다면 queryDSL에서는 어떤 식으로 표현할까?     

이미 DB에서는 A, B와의 연관관계를 알고 있으며 Brand, Parnter의 엔티티를 보면 연관관계 매핑을 통해서 이 관계를 알고 있다.     

그렇다면 우리는 굳이 ON절에서 b.a_id = a.id라는 조건을 쓸 필요가 있을까?     

물론 쿼리에서는 그렇게 조회해야한다면 이 조건을 무조건 달아야 한다.    

하지만 JPA에서는 이미 알고 있다.

```
List<Tuple> tuple = query.select(brand, partner)
	 	   	     	   .from(brand)
	 	   	     	   .join(brand.partner, partner)
	 	   	     	   .fetch();
```

결과는 ??

```
Hibernate: 
    /* select
        brand,
        partner 
    from
        Brand brand   
    inner join
        brand.partner as partner */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
```
날아가는 쿼리만 봐도 이미 어떻게 하고 있는가?     

하지만 다음과 같이 한번 코드를 실행해 보자.

```
List<Tuple> tuple = query.select(brand, partner)
			 	   	   .from(brand)
			 	   	   .innerJoin(partner)
			 	   	   .fetch();
System.out.println(tuple.size());
System.out.println(tuple.toString());
```
    
코드상에는 오류가 없지만 

```
Jul 21, 2020 4:26:53 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 21, 2020 4:26:53 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 21, 2020 4:26:54 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 21, 2020 4:26:54 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 21, 2020 4:26:54 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jul 21, 2020 4:26:54 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jul 21, 2020 4:26:54 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 21, 2020 4:26:54 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 21, 2020 4:26:54 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Jul 21, 2020 4:26:54 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        partner 
    from
        Brand brand   
    inner join
        Partner partner */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on
Jul 21, 2020 4:26:55 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1064, SQLState: 42000
Jul 21, 2020 4:26:55 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 3
javax.persistence.PersistenceException: org.hibernate.exception.SQLGrammarException: could not extract ResultSet
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:154)
	at org.hibernate.query.internal.AbstractProducedQuery.list(AbstractProducedQuery.java:1542)
	at org.hibernate.query.Query.getResultList(Query.java:165)
	at com.querydsl.jpa.impl.AbstractJPAQuery.getResultList(AbstractJPAQuery.java:146)
	at com.querydsl.jpa.impl.AbstractJPAQuery.fetch(AbstractJPAQuery.java:202)
	at io.basquiat.JpaMain.main(JpaMain.java:34)
Caused by: org.hibernate.exception.SQLGrammarException: could not extract ResultSet
	at org.hibernate.exception.internal.SQLExceptionTypeDelegate.convert(SQLExceptionTypeDelegate.java:63)
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:42)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:113)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:99)
	at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.extract(ResultSetReturnImpl.java:67)
	at org.hibernate.loader.Loader.getResultSet(Loader.java:2285)
	at org.hibernate.loader.Loader.executeQueryStatement(Loader.java:2038)
	at org.hibernate.loader.Loader.executeQueryStatement(Loader.java:2000)
	at org.hibernate.loader.Loader.doQuery(Loader.java:951)
	at org.hibernate.loader.Loader.doQueryAndInitializeNonLazyCollections(Loader.java:352)
	at org.hibernate.loader.Loader.doList(Loader.java:2831)
	at org.hibernate.loader.Loader.doList(Loader.java:2813)
	at org.hibernate.loader.Loader.listIgnoreQueryCache(Loader.java:2645)
	at org.hibernate.loader.Loader.list(Loader.java:2640)
	at org.hibernate.loader.hql.QueryLoader.list(QueryLoader.java:506)
	at org.hibernate.hql.internal.ast.QueryTranslatorImpl.list(QueryTranslatorImpl.java:400)
	at org.hibernate.engine.query.spi.HQLQueryPlan.performList(HQLQueryPlan.java:219)
	at org.hibernate.internal.SessionImpl.list(SessionImpl.java:1412)
	at org.hibernate.query.internal.AbstractProducedQuery.doList(AbstractProducedQuery.java:1565)
	at org.hibernate.query.internal.AbstractProducedQuery.list(AbstractProducedQuery.java:1533)
	... 4 more
Caused by: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 3
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:120)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:97)
	at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:122)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeInternal(ClientPreparedStatement.java:953)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeQuery(ClientPreparedStatement.java:1003)
	at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.extract(ResultSetReturnImpl.java:57)
	... 19 more
Jul 21, 2020 4:26:55 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    681287 nanoseconds spent acquiring 1 JDBC connections;
    377243 nanoseconds spent releasing 1 JDBC connections;
    10064551 nanoseconds spent preparing 1 JDBC statements;
    13345927 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
    39899 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 21, 2020 4:26:55 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
```
inner join basquiat_partner partner1_ on 하다가 syntax에러가 뙇 하고 뜨게 된다.     

아마도 뒤에 ON절이 오기를 기대했건만 없어서 syntax에러를 낸다는 것을 알 수 있다.    

그래서 보통은 ON절을 사용할 경우에는 엔티티를 하나만 넣어서 사용하거나 두개를 넣어서 사용해도 무방하다.    

```
List<Tuple> tuple = query.select(brand, partner)
			 	   	   .from(brand)
			 	   	   .join(partner)
			 	   	   .on(brand.partner.name.eq(partner.name))
			 	   	   .fetch();
System.out.println(tuple.size());
System.out.println(tuple.toString());
```
결과는?

```
Hibernate: 
    /* select
        brand,
        partner 
    from
        Brand brand   
    inner join
        Partner partner with brand.partner.name = partner.name */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner2_ 
                on brand0_.partner_id=partner2_.id 
        inner join
            basquiat_partner partner1_ 
                on (
                    partner2_.partner_name=partner1_.partner_name
                )
```
예상과는 살짝 다르긴 하지만 basquiat_partner partner2_ on brand0_.partner_id=partner2_.id으로 자동으로 조건을 붙이고 또 뒤에 우리가 ON절에 작성했던 코드가 쿼리로 날아간 것을 볼 수 있다.     

하지만 이게 참... 우리가 개발자끼리도 코드 스타일이 다르듯이 쿼리도 이게 짜는 사람마다 취향이란게 존재한다는 것을 알게 되는데 어떤 분은 ON절을 활용해서 쿼리를 짜거나 아니면 그냥 JOIN을 걸고 WHERE절에서 조건을 주는 방식으로 가는 분들도 있다는 것이다.     

그것을 반영한 듯 싶다.    

즉 .join(brand.partner, partner) 쓰는 방식은 뒤에 ON절을 활용하든 WHERE절을 활용하든 연관관계가 있다면 해당 pk-fk로 ON절을 생성한다.

하지만 .join(partner)의 경우에는 뒤에 ON절이 올 것이라고 암묵적인 무언가가 있는 듯 싶다.     

그에 따른 차이가 존재하니 이 부분은 잘 살펴보기 바란다.     

'근데요? 아무 연관관계 없는 녀석과의 조인은요?'     

우린 또 궁금증이 있으면 풀어가야 하는 개발자니깐.... 젠을 하는게 귀찮더라도 엔티티 하나를 준비해보자.     

~~그냥 자동으로 젠이 되는데????~~

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

@Entity
@Table(name = "basquiat_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Product {

	@Builder
	public Product(String name, String brandName) {
		super();
		this.name = name;
		this.brandName = brandName;
	}

	/** 생산품 고유 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;

	/** 생산품 명 */
	@Column(name = "product_name")
	private String name;
	
	/** 브랜드 명 */
	@Column(name = "brand_name")
	private String brandName;
	
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

기존의 데이터가 있으니 테이블 생성문과 insert문으로 데이터를 직접 밀어넣어 보자.    

```
CREATE TABLE `basquiat_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) DEFAULT NULL,
  `brand_name` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO basquiat_product
	(
		product_name, brand_name, created_at, updated_at
    )
VALUES
    (
		'Bass Guitar', '펜더', NOW(), NOW()
    ),
    (
		'Electric Guitar', '펜더', NOW(), NOW()
    );

```
다음과 같이 코드를 실행해 보자.    

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;
import static io.basquiat.model.QProduct.product;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.Tuple;
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
        	
        	List<Tuple> tuple = query.select(brand, product)
        							 .from(brand, product)
        							 .where(brand.name.eq(product.brandName))
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

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        product 
    from
        Brand brand,
        Product product 
    where
        brand.name = product.brandName */ select
            brand0_.br_code as br_code1_0_0_,
            product1_.id as id1_2_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            product1_.brand_name as brand_na2_2_1_,
            product1_.created_at as created_3_2_1_,
            product1_.product_name as product_4_2_1_,
            product1_.updated_at as updated_5_2_1_ 
        from
            basquiat_brand brand0_ cross 
        join
            basquiat_product product1_ 
        where
            brand0_.br_name=product1_.brand_name
[
	[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
	Product(id=1, name=Bass Guitar, brandName=펜더, createdAt=2020-07-21T16:50:34, updatedAt=2020-07-21T16:50:34)], 
	
	[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), 
	Product(id=2, name=Electric Guitar, brandName=펜더, createdAt=2020-07-21T16:50:34, updatedAt=2020-07-21T16:50:34)]
]
```

다음과 같이도 가능하다.    

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;
import static io.basquiat.model.QPartner.partner;
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
        	List<Tuple> tuple = query.select(brand, product)
						 	   	     .from(brand)
						 	   	     .join(product)
						 	   	     .on(brand.name.eq(product.brandName))
						 	   	     .fetch();
        	System.out.println(tuple.size());
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

쿼리 결과는?     

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand,
        product 
    from
        Brand brand   
    inner join
        Product product with brand.name = product.brandName */ select
            brand0_.br_code as br_code1_0_0_,
            product1_.id as id1_2_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            product1_.brand_name as brand_na2_2_1_,
            product1_.created_at as created_3_2_1_,
            product1_.product_name as product_4_2_1_,
            product1_.updated_at as updated_5_2_1_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_product product1_ 
                on (
                    brand0_.br_name=product1_.brand_name
                )
2
[
	[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Product(id=1, name=Bass Guitar, brandName=펜더, createdAt=2020-07-21T16:50:34, updatedAt=2020-07-21T16:50:34)], 
	
	[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Product(id=2, name=Electric Guitar, brandName=펜더, createdAt=2020-07-21T16:50:34, updatedAt=2020-07-21T16:50:34)
	]
]
```
오호? 연관관계가 전혀 없는 테이블이기에  basquiat_partner partner1_ on brand0_.partner_id=partner1_.id 이런 코드가 생성되지 않는다.    

LEFT JOIN도 마찬가지???

```
List<Tuple> tuple = query.select(brand, partner)
			 	   	   .from(brand)
					   .leftJoin(product)
					   .on(brand.name.eq(product.brandName))
					   .fetch();
System.out.println(tuple.size());
System.out.println(tuple.toString());
```

결과는?

```
Hibernate: 
    /* select
        brand,
        product 
    from
        Brand brand   
    left join
        Product product with brand.name = product.brandName */ select
            brand0_.br_code as br_code1_0_0_,
            product1_.id as id1_2_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            product1_.brand_name as brand_na2_2_1_,
            product1_.created_at as created_3_2_1_,
            product1_.product_name as product_4_2_1_,
            product1_.updated_at as updated_5_2_1_ 
        from
            basquiat_brand brand0_ 
        left outer join
            basquiat_product product1_ 
                on (
                    brand0_.br_name=product1_.brand_name
                )
```
하지만 이 경우에는 잘 생각해 보면 ON절이 아닌 WHERE절을 이용해서 조건을 줄 수가 없다.    

따라서 연관관계가 없는 외부 조인시에는 ON절을 활용해야만 한다.    

## FETCH JOIN using queryDSL     

우리는 JPA에서 JPQL을 사용하다 보면 FETCH JOIN을 사용하는 경우가 있다.     

DB레벨이 아닌 JPA의 기능이라는 것을 알 수 있을 텐데 이 이유는 LAZY 전략이 아닌 한방 쿼리로 모든 정보를 가져올 때 사용하게 되는 경우가 있으며 N:1문제에서도 지겹도록 나오기 때문이다.    

차후 completedJPA에서도 공부할 부분이다.    

그럼 지금까지 우리가 테스트한 부분을 가만히 잘 살펴보면 페치 전략을 LAZY를 사용했기 때문에 해당 정보를 가져올 때 그때 디비에서 가져온다는 것을 로그에서도 확인 할 수 있다.    

그럼 코드를 다음과 같이 구성해서 한번 실행해 보자.     

```
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
						 	   	     .leftJoin(brand.partner, partner)
						 	   	     .fetch();
        	System.out.println(selectBrand.size());
        	System.out.println(selectBrand.toString());
        	selectBrand.stream().map(br -> br.getPartner().toString())
        						.forEach(System.out::println);
        	
        	System.out.println("========================================");
        	
        	List<Brand> fetchBrand = query.select(brand)
							 	   	      .from(brand)
								 	   	  .leftJoin(brand.partner, partner)
								 	   	  .fetchJoin()
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
```

정말 간단하지 않은가???

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    inner join
        brand.partner as partner */ select
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
6
[Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
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
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
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
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
========================================
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    left join
        fetch brand.partner   
    left join
        brand.partner as brand_partner 
    order by
        brand_partner.name asc nulls first */ select
            brand0_.br_code as br_code1_0_0_,
            partner1_.id as id1_1_1_,
            brand0_.br_en_name as br_en_na2_0_0_,
            brand0_.launched_at as launched3_0_0_,
            brand0_.br_name as br_name4_0_0_,
            brand0_.number as number5_0_0_,
            brand0_.partner_id as partner_7_0_0_,
            brand0_.updated_at as updated_6_0_0_,
            partner1_.address as address2_1_1_,
            partner1_.entry_at as entry_at3_1_1_,
            partner1_.partner_name as partner_4_1_1_,
            partner1_.updated_at as updated_5_1_1_ 
        from
            basquiat_brand brand0_ 
        left outer join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id 
        left outer join
            basquiat_partner partner2_ 
                on brand0_.partner_id=partner2_.id 
        order by
            case 
                when partner2_.partner_name is null then 0 
                else 1 
            end,
            partner2_.partner_name asc
7
[Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=0, launchedAt=2020-07-21T14:35:09, updatedAt=null), Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null), Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
null
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=RIDINBASS, name=라이딩 베이스, address=합정동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
Partner(id=MUSICFORCE, name=뮤직포스, address=청담동 어딘가 있다., entryAt=2020-07-10T10:49:09, updatedAt=null)
```
'======='을 기준으로 로그를 살펴보면 Stream API에서 map을 할 때 그때 쿼리를 날려서 정보를 가져오는 것을 알 수 있으며 .fetchJoin()을 추가한 코드에서는 그냥 출력하는 것을 알 수 있다.    

뭔가 정신없이 진행이 되서 정리가 좀 안되는 듯한 이 기분은 무엇일까?     

아무튼 이야기가 길어져서 원래 진행할 GROUP BY와 관련된 내용은 다음 브랜치에서 진행해 볼까 한다.     