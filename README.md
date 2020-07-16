# ORDER BY using queryDSL    

정렬은 어떤 컬럼을 기준으로 오름차순으로 할 것인지 내림차순으로 할 것인지를 정하는 것이다.    

쿼리를 예로 들어보자.

```

SELECT * 
	FROM brand
   WHERE number > 10
   ORDER BY number ASC;

multi order

SELECT * 
	FROM brand
   WHERE number > 10
   ORDER BY number ASC, br_name ASC;
```

처럼 데이터를 가져올 때 그 순서를 정해서 가져올 수 있다.    

여러개의 컬럼을 정렬할 때는 어느 것을 먼저 정렬할 것이지에 따라 ','로 나열 할 수 있다.

### asc()

오름차순으로 정렬한다. 간혹 후배 개발자와 얘기를 하다보면 이것을 많이 헛갈려 한다.    

내림차순이 asc인지 desc인지 헛갈려 하는데 이해한다. ~~나도 그랬으니까...~~    

영어로 ascending의 약자인데 오름차순은 말 그대로 작은 수에서 높은 수로 올라가는 모습을 보면 된다.     

[1,5,2,6]같은 배열이 있다면 오름차순이니깐 낮은 수부터 정렬하면 [1,2,5,6]이다. ~~누구나 다 아는...~~    

디비의 result grid를 보면    

```
code   | number 
 xx    |   1
 xa	   |   5
 xd	   |   9
```
이렇게 정렬되서 나올 것이다. 물론 문자열도 마찬가지이다. a-b, ㄱ-ㅎ도 마찬가지이다.    

자 그럼 이제 코드로 한번 살펴보자.

```
List<Brand> brandNoAscList = query.select(brand)
				       		   .from(brand)
				       		   .where(brand.number.gt(10))
				       		   .orderBy(brand.number.asc())
				       		   .fetch();
System.out.println("brand.number.asc() start");
brandNoAscList.stream().map(s -> s.toString())
		 		  	 .forEach(System.out::println);

List<Brand> brandNameAscList = query.select(brand)
					       		 .from(brand)
						       	 .where(brand.number.gt(10))
						       	 .orderBy(brand.name.asc())
						       	 .fetch();
System.out.println("brand.name.asc() start");
brandNameAscList.stream().map(s -> s.toString())
		  	 		   .forEach(System.out::println);
```

위에서 처럼 number, name으로 오름차순 정렬을 해보자.

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.number asc */ select
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
        order by
            brand0_.number asc
brand.number.asc() start
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.name asc */ select
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
        order by
            brand0_.br_name asc
brand.name.asc() start
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)            
```

첫번째는 number로 두 번째는 name로 오름차순으로 정렬되었다.

### desc()    

당연히 이것은 descending의 약자로 asc의 반대로 생각하면 된다.

그럼 코드로 역시 살펴보는게 최고다.

위의 코드를 그대로 사용하고 asc를 desc로 바꾸자.

```
List<Brand> brandNoDescList = query.select(brand)
			       		 	    .from(brand)
			       		 	    .where(brand.number.gt(10))
			       		 	    .orderBy(brand.number.desc())
			       		 	    .fetch();
System.out.println("brand.number.desc() start");
brandNoDescList.stream().map(s -> s.toString())
	 		  	      .forEach(System.out::println);

List<Brand> brandNameDescList = query.select(brand)
					       		  .from(brand)
						       	  .where(brand.number.gt(10))
					       	      .orderBy(brand.name.desc())
					       	      .fetch();
System.out.println("brand.name.desc() start");
brandNameDescList.stream().map(s -> s.toString())
  	 				    .forEach(System.out::println);
```

쿼리가 어떻게 날아가는지 확인하자.    

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.number desc */ select
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
        order by
            brand0_.number desc
brand.number.desc() start
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.name desc */ select
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
        order by
            brand0_.br_name desc
brand.name.desc() start
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```
생각대로 결과가 나온 것을 확인할 수 있다.    

'저는 number을 asc로 먼저 정렬하고 name을 desc로 정렬하고 싶읍니다. 가능한가요?'

당연히 쿼리에서 가능하고 queryDSL도 가능하다.    

앞서 where조건을 and로 줄때 우리는 구분자 ','로 한 것을 이미 알고 있다. 이것도 같은 방식으로 멀티 정렬을 지원한다.    
    

```
List<Brand> brandMultiOrderList = query.select(brand)
				       		 	    .from(brand)
				       		 	    .where(brand.number.gt(10))
				       		 	    .orderBy(brand.number.asc(), 
				       		 			     brand.name.desc()
				       		 			   )
				       		 	    .fetch();
System.out.println("multi order by start");
brandMultiOrderList.stream().map(s -> s.toString())
 		  	     		  .forEach(System.out::println);
```

쿼리를 아는 분이라면 참 지겨웁겠다~

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.number asc,
        brand.name desc */ select
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
        order by
            brand0_.number asc,
            brand0_.br_name desc
multi order by start
Brand(code=FODERA, name=포데라, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```
결과를 보면 일단 number로 오름차순으로 정렬하고 이름을 내림차순으로 정렬된 것을 볼 수 있다. 포데라 -> 에프베이스    

하지만 의도치 않은 결과를 가져 올 경우도 있다.    

만일 컬럼이 null을 허용하는 경우 그 컬럼을 정렬을 하게 되면 의도치 않게 null이 맨 위로 올라오거나 맨 밑으로 정렬되는 경우가 있다.    

쿼리에서는 이것을 정의하는 방법이 있는데 현재 mySQL로 테스트하기 때문에 일단 링크 하나 던져본다.    

[How to Order NULL Values First or Last in MySQL?](https://www.designcise.com/web/tutorial/how-to-order-null-values-first-or-last-in-mysql)     

'그럼 queryDSL도 그것을 지원해요?'

일단 나는 DB에서 row 하나를 null로 세팅하고 테스트를 해봤다.    

```
UPDATE basquiat_brand
   SET br_name = null
 WHERE br_code = 'FODERA'
```
툴에서 컬럼을 직접 에디트하면 보통 빈 공백으로 들어가서 의도치 않게 작동할 수 있기 때문에 업데이트 쿼리를 날려서 null로 세팅했다.    

```
List<Brand> brandList = query.select(brand)
		       		 	   .from(brand)
		       		 	   .where(brand.number.gt(10))
		       		 	   .orderBy(brand.name.asc())
		       		 	   .fetch();
System.out.println("order by start");
brandList.stream().map(s -> s.toString())
  	     		  .forEach(System.out::println);
```

일단 그냥 조회하면 

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.name asc */ select
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
        order by
            brand0_.br_name asc
order by start
Brand(code=FODERA, name=null, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

null이 맨 위로 정렬되서 나왔다. 만일 null값은 맨 뒤로 정렬을 하고 싶다면    

```
List<Brand> brandList = query.select(brand)
		   		 	       .from(brand)
			   		 	   .where(brand.number.gt(10))
			   		 	   .orderBy(brand.name.asc().nullsLast())
			   		 	   .fetch();
```
위 코드처럼 asc로 정렬하는데 null값은 마지막으로 정렬시켜줘라고 말해주면 된다.

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.name asc nulls last */ select
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
        order by
            case 
                when brand0_.br_name is null then 1 
                else 0 
            end,
            brand0_.br_name asc
order by start
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=null, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

쿼리가 희안하게 나가긴 하지만 결과는 보는 바와 같다.    

'그럼 nullsFirst()도 있겠군요?'     

이런 경우도 한번 생각해 보자.    

```
List<Brand> brandList = query.select(brand)
       		 	     	   .from(brand)
	       		 	       .where(brand.number.gt(10))
	       		 	       .orderBy(brand.name.desc())
	       		 	       .fetch();
System.out.println("order by start");
brandList.stream().map(s -> s.toString())
  	     		.forEach(System.out::println);
```

내림차순으로 정렬하게 되면 결과는 어떨까?    

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.name desc */ select
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
        order by
            brand0_.br_name desc
order by start
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=null, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

아! null인 경우에는 뒤로 정렬이 된다. 만일 요구에 의해 null이 앞으로 와야 한다면 생각대로 하면 된다.     

```
List<Brand> brandList = query.select(brand)
	       		 	       .from(brand)
	       		 	       .where(brand.number.gt(10))
	       		 	       .orderBy(brand.name.desc().nullsFirst())
	       		 	       .fetch();
```
위 코드처럼 desc로 정렬할 건데 null이면 앞으로 좀 보내줘서 정렬해줘라고 말해주면 된다.    

뭐 결과는    

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.number > ?1 
    order by
        brand.name desc nulls first */ select
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
        order by
            case 
                when brand0_.br_name is null then 0 
                else 1 
            end,
            brand0_.br_name desc
order by start
Brand(code=FODERA, name=null, enName=Fodera, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=22, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FBASS, name=에프베이스, enName=FBass, number=11, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=18, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티, enName=Mattisson, number=56, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=17, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

내가 알기로는 null과 관련해서 이게 DB마다 조금씩 다른 것으로 알고 있다.    

그래서 방어적인 코드를 작성해야 한다면 null인 경우에는 앞으로 보내서 정렬할지 뒤로 보내서 정렬할 지를 명시적으로 코드로 작성하는 것도 하나의 방법이 될 것이다.   

이렇게 해서 정렬과 관련된 내용을 알아봤다.    

# PAGING using queryDSL   

페이징 처리와 관련해서 많은 DB를 경험하신 분들은 JPA의 페이징 방식은 참 맘에 들 것이다. 아무래도 DB마다 페이징 처리 쿼리가 다 다르기 때문이다.    

그런 면에서 mySlq의 limit offset으로 페이징 하는 방식은 상당히 편리하다. queryDSL의 페이징 처리 방식은 내부적으로 JPA	가 DB의 dialect의 맞게 만들어 주지만 작성하는 방식은 mySql의 limit offset의 방식을 따른다. 아무래도 이게 좀 더 직관적이기 때문인것 같은데 그럼 limit offset이 무엇인지 한번 살펴보자.    

[LIMIT으로 결과 값 제한](https://toma0912.tistory.com/42)    

부연 설명으로 mySql에서는 페이징하는 쿼리가 보통 2가지 방식이 있다.     

하나는 limit에서 하는 방법 하나는 limit offset을 통해서 하는 방법이다. 이 2가지는 약간 방식이 다른데 한번 쿼리로 보자.    

```
SELECT *
	FROM brand
	LIMIT 1, 4

or

SELECT *
	FROM brand
	LIMIT 4 OFFSET 1
```

첫 번째 방식은 이런식이다 -> '테이터를 기준으로 1번 row밑으로 4개의 데이터를 가져와라'

**밑** 이말에 주목하자. 즉 1번 row는 포함하지 않는다.   

예를 들면    

```
id  | name
1	|  A
2	|  B
3	|  C
4	|  D
5	|  E
6	|  F

```
라는 데이터가 있으면 1번 row밑으로 즉 아이디가 2이 녀석부터 4개이니 id가 2,3,4,5인 row data를 가져오게 된다.    

두 번쨰 방식도 마찬가지이다. -> '데이터를 기준으로 offset 1, 즉 1번 row밑으로 4개의 데이터를 가져와라'    

그래서 보통 페이징 처리할 때는 프론트에서 받는 page, size를 통해서 limit와 offset을 계산하는 로직을 만들게 된다.     

참고로 위에서 limit ?, ?에서 왼쪽이 offest에 해당하고 limit ? offset ? 은 당연히 오른쪽이다. 만일 생략하게 되면 offset은 0으로 잡힌다.    

이말이 무엇이냐면 0번째 row밑으로라는 의미이다. 이해가 가는가?    

즉 limit 10 이렇게 offset부분을 생략하면 '0번째 row밑으로 10개를 가져와라'의 의미가 된다.    

일단 여기서는 데이터를 어떻게 가져오는지만 살펴볼까 한다.    

보통 mySql인 경우에는 pk를 잡으면 조회시에 특별한 조건을 주지 않으면 asc, 즉 올림차순으로 정렬해서 결과를 반환한다.    

지금까지 사용한 엔티티에서 필드 number의 경우에는 검색 조건 테스트를 위해서 그냥 임의의 번호를 넣었는데 이번에는 페이징과 관련된 테스트를 하기위해서 pk로 잡혀있는 br_code의 정렬 순서대로 number를 넘버링을 했다. ~~위에 설명했으니 무슨 의미인지 알겠지?~~     

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/3.query-dsl-orderby-n-paging/capture/capture1.png)    

일단 코드를 짜보는게 중요하다.    

```
List<Brand> brandPagindList = query.select(brand)
			       		 	    .from(brand)
			       		 	    .offset(3)
			       		 	    .limit(3)
			       		 	    .fetch();
System.out.println("paging start");
brandPagindList.stream().map(s -> s.toString())
  	     		  	  .forEach(System.out::println);
```

자 위에 올려논 이미지의 데이터를 기준으로 저 코드를 해석해 보면 'offset(3), 3번째 row밑으로 limit(3), 3개의 데이터를 가져와라'이다.    

그럼 실제로 그런지 봐야한다.

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ limit ?,
            ?
order by start
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

오호? 쿼리가 나간 것을 보니 위에서 언급했던 첫 번째 방식으로 날리는 것을 확인할 수 있다.    

그리고 이미지에서 Fodera가 3번째 row이니 그 밑인 말로우 베이스부터 3개를 가져온 것을 알 수 있다.    

'한 건의 테스트만으로는 감이 오지 않습니다!'    

그럼 이렇게 해보자. 5번째 row에서 10개를 가져오는 시나리오를 해보자.         

'데이터는 6개 뿐인데 10개를 가져오다니요???'    

일단 한번 날려보자.

```
List<Brand> brandPagindList = query.select(brand)
			       		 	    .from(brand)
			       		 	    .offset(5)
			       		 	    .limit(10)
			       		 	    .fetch();
System.out.println("paging start");
brandPagindList.stream().map(s -> s.toString())
  	     		      .forEach(System.out::println);
```

결과는 이미지를 기준으로 5번째 row밑으로 10개를 가져온다. 하지만 총 6개이기 때문에 하나인 샌드버그만 가져올 것이다.    

```
Hibernate: 
    /* select
        brand 
    from
        Brand brand */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ limit ?,
            ?
paging start
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

우리는 현재 fetch를 통해서 결과를 받아왔다.    

하지만 paging과 관련된 내용이니 맨 앞에서 봤던 fetchResults()를 통해서 한번 체크를 해보자.     

```
QueryResults<Brand> qResult = query.select(brand)
				       		 	.from(brand)
				       		 	.offset(5)
				       		 	.limit(10)
				       		 	.fetchResults();
System.out.println("paging start");
System.out.println("qResult offset : " +  qResult.getOffset());
System.out.println("qResult limit : " +  qResult.getLimit());
System.out.println("qResult totalCount : " +  qResult.getTotal());
qResult.getResults().stream().map(s -> s.toString())
 		  	     		   .forEach(System.out::println);
```

결과도 보자.    

```
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
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_ limit ?,
            ?
paging start
qResult offset : 5
qResult limit : 10
qResult totalCount : 6
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```

이전 브랜치에서 fetchResults()와 관련해서 카운트 쿼리에 대해서는 따로 날리는게 유리하다고 언급한 적이 있다.    

나온 김에 이유를 한번 살펴보자.    

다음에 배울 내용이지만 join을 한번 걸어보자.     

```
QueryResults<Brand> qResult = query.select(brand)
				       		 	.from(brand)
				       		 	.join(brand.partner)
				       		 	.offset(5)
				       		 	.limit(10)
				       		 	.fetchResults();
System.out.println("paging start");
System.out.println("qResult offset : " +  qResult.getOffset());
System.out.println("qResult limit : " +  qResult.getLimit());
System.out.println("qResult totalCount : " +  qResult.getTotal());
qResult.getResults().stream().map(s -> s.toString())
  	     		  		   .forEach(System.out::println);
```

자 저렇게 해놓고 결과를 한번 보자.    

```
Hibernate: 
    /* select
        count(brand) 
    from
        Brand brand   
    inner join
        brand.partner */ select
            count(brand0_.br_code) as col_0_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    inner join
        brand.partner */ select
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
                on brand0_.partner_id=partner1_.id limit ?,
            ?
paging start
qResult offset : 5
qResult limit : 10
qResult totalCount : 6
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```
자 위에 날아간 카운터 쿼리를 보자.    

```
Hibernate: 
    /* select
        count(brand) 
    from
        Brand brand   
    inner join
        brand.partner */ select
            count(brand0_.br_code) as col_0_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
```

카운트 쿼리가 실제로 작성된 queryDSL의 쿼리를 토대로 구하는 것을 볼 수 있다. 하지만 totalCount는 6건으로 조인을 하든 안하든 똑같다.    

당연한 이야기이긴 하지만 그렇다면 페이징처리를 위한 전체 카운트를 구하는데 굳이 조인을 해서 구할 필요가 있냐는 의문이 든다.    

그냥 전체 카운터 쿼리를 다음과 같이    

```
long total = query.select(brand)
				.from(brand)
				.fetchCount();
```

처럼 쿼리를 날리는게 성능상 이점을 갖는다.    

그래서 카운트 쿼리는 따로 작성해서 구하는 것이 성능면에서도 유리하다고 말하는 것이다.    

지금 예제는 워낙 간단해서 와닿지 않을 수 있다. 하지만 실무에서는 특히 내가 재직하고 있는 회사에서는 상품과 관련되서 많은 테이블들이 JOIN으로 걸려 있지만 전체 카운트를 가져올 때는 그 많은 테이블들과 조인해서 가져올 필요가 없는 경우로 카운트 구하는 쿼리를 따로 작성해서 가져온다.    

### offset생략    

위에서 sql을 예로 들면서 offset 생략시 0으로 기본 세팅된다고 언급했다.    

queryDSL도 그럴까?    

```
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
```

offset을 제거하고    

```
Hibernate: 
    /* select
        count(brand) 
    from
        Brand brand   
    inner join
        brand.partner */ select
            count(brand0_.br_code) as col_0_0_ 
        from
            basquiat_brand brand0_ 
        inner join
            basquiat_partner partner1_ 
                on brand0_.partner_id=partner1_.id
Hibernate: 
    /* select
        brand 
    from
        Brand brand   
    inner join
        brand.partner */ select
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
                on brand0_.partner_id=partner1_.id limit ?
paging start
qResult offset : 0
qResult limit : 10
qResult totalCount : 6
Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=null)
```
결과를 확인하면 그렇다는 것을 알 수 있다.    

이렇게 해서 페이징과 관련된 API를 살펴봤다.    

번외이긴 하지만    

[Pagination을 위한 최적화 SQL](https://blog.lulab.net/database/optimize-pagination-sql-by-join-instead-of-limit/)          

이런 것도 있다.     

그냥 알아두면 언젠가는 적용해 볼 수 있는 내용이니 한번 훝어 보는 것도 괜찮다.    

다음 브랜치에서는 SELECT절에서 사용할 수 있는 기능과 sub query에 대해서 알아볼 까 한다.

# At A Glance     

정렬, 조건 검색 그리고 페이징과 관련해서 우리는 지금 테스트로는 사실 성능 저하에 대한 부분을 경험하지 못한다.     

또한 DB 성능과 관련해서는 백엔드에서 아무리 잘 짜여진 쿼리라 할지라도 DB 테이블의 인덱스라든가 최적화를 하지 않으면 성능을 제대로 뽑아내지 않는다.     

꾸준히 이야기하는 거지만 JPA는 만능이 아니다. 그와 함께 DB에 대해서 공부를 해야한다. 그렇다고 DBA 수준이 되라는 이야기는 아니다.    

적어도 기본적인 것에 충실해야 한다고 말하고 싶다.    