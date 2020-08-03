# SQL Function    

Q-Type으로 생성된 엔티티의 내부 구조를 보면 StringPath나 NumberPath같은 객체에 extends된 NumberExpression, StringExpression을 따라가면 지금까지 우리가 컬럼을 대상으로 사용할 수 있는 많은 메소드들, concat이나 trim, min, max, lower을 볼 수 있는데 대부분 이것은 쿼리에서 직접적으로 사용가능한 것들이다.     

하지만 SQL에서 지원하는 함수들을 꼭 사용해야 하는 경우들이 있다면 어떻게 해야 할까?     

예제로 그렇긴 하지만 

```
SELECT launched_at,
	   DATE_FORMAT(launched_at, '%Y-%m-%d %H:%i:%s'),
       DATE_FORMAT(launched_at, '%Y-%m-%d %H:%i'),
       DATE_FORMAT(launched_at, '%Y-%m-%d %H'),
       DATE_FORMAT(launched_at, '%Y-%m-%d'),
       DATE_FORMAT(launched_at, '%Y-%m')
	FROM basquiat_brand;
```

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/9.query-dsl-sql-function-bulk/capture/capture1.png)     

위 이미지처럼 날짜에 대한 정보를 좀 잘라서 사용하고 싶은 경우가 생길 수 도 있다.      

물론 substring으로 자르면 그만이지만 쉽게 사용할 수 없을까?     

그것을 queryDSL에서도 지원한다.     

다음 이미지를 한번 보자.      

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/9.query-dsl-sql-function-bulk/capture/capture2.png)     

우리가 persistence.xml에서 hibernate.dialect옵션에 걸어둔 클래스를 살펴 보자.      

쭉 따라가면 이미지처럼 해당 방언관련 클래스가 registerFunction에서 지원하는 것을 사용할 수 있다.     

이것은 mySql뿐만 아니라 제공하는 수많은 다른 벤더들의 관련 클래스를 찾아가면 어떤 것을 지원하는지 알 수 있다.    

mySql의 경우에는 

```
registerFunction( "date_format", new StandardSQLFunction( "date_format", StandardBasicTypes.STRING ) );
```
처럼 지원하는 것을 알 수 있다.      

자 위에서 맨 처음 쿼리를 보면 알겠지만 사용법은 

```
DATE_FORMAT(날짜컬럼, '%Y-%m-%d %H') // 보여줄 표현
```
과 같이 사용할 수 있다.     

그럼 궁금하니 바로 코드로 보자.    

```
List<Tuple> brandSqlFunc = query.select(
									brand.launchedAt,
									Expressions.stringTemplate("function('date_format', {0}, {1})", brand.launchedAt, "%Y-%m-%d %H:%i:%s"),
									Expressions.stringTemplate("function('date_format', {0}, {1})", brand.launchedAt, "%Y-%m-%d %H:%i"),
									Expressions.stringTemplate("function('date_format', {0}, {1})", brand.launchedAt, "%Y-%m-%d %H"),
									Expressions.stringTemplate("function('date_format', {0}, {1})", brand.launchedAt, "%Y-%m-%d"),
									Expressions.stringTemplate("function('date_format', {0}, {1})", brand.launchedAt, "%Y-%m")
									)
							 .from(brand)
							 .fetch();
System.out.println(brandSqlFunc.toString());

result grid

[
	[2020-07-10T10:49:09, 2020-07-10 10:49:09, 2020-07-10 10:49, 2020-07-10 10, 2020-07-10, 2020-07], 
	[2020-07-10T10:49:09, 2020-07-10 10:49:09, 2020-07-10 10:49, 2020-07-10 10, 2020-07-10, 2020-07], 
	[2020-07-10T10:49:09, 2020-07-10 10:49:09, 2020-07-10 10:49, 2020-07-10 10, 2020-07-10, 2020-07], 
	[2020-07-10T10:49:09, 2020-07-10 10:49:09, 2020-07-10 10:49, 2020-07-10 10, 2020-07-10, 2020-07], 
	[2020-07-10T10:49:09, 2020-07-10 10:49:09, 2020-07-10 10:49, 2020-07-10 10, 2020-07-10, 2020-07], 
	[2020-07-21T14:35:09, 2020-07-21 14:35:09, 2020-07-21 14:35, 2020-07-21 14, 2020-07-21, 2020-07], 
	[2020-07-10T10:49:09, 2020-07-10 10:49:09, 2020-07-10 10:49, 2020-07-10 10, 2020-07-10, 2020-07]
]
```
이게 참 번거롭다. 우리는 이전 브랜치에서 @QueryDelegate을 배웠으니 이것을 그쪽으로 위임시켜서 캡슐화 시키고 사용해 보자.     

SQLExtensions

```
@QueryDelegate(Brand.class)
public static StringTemplate dateFormat(QBrand brand, DateTimePath<LocalDateTime> date, String datePattern) {
    return Expressions.stringTemplate("function('date_format', {0}, {1})", date, datePattern);
}
```
이렇게 하나를 추가해 보자.     

그리고 젠이 되면 

```
public StringTemplate dateFormat(DateTimePath<java.time.LocalDateTime> date, String datePattern) {
    return SQLExtensions.dateFormat(this, date, datePattern);
}
```
이런게 생겼으니 우리는 코드 몇줄로 그냥 끝내보자.

```
List<Tuple> brandSqlFunc = query.select(
									brand.launchedAt,
									brand.dateFormat(brand.launchedAt, "%Y-%m-%d %H:%i"),
									brand.dateFormat(brand.updatedAt, "%Y-%m-%d %H:%i")
									)
								 .from(brand)
								 .fetch();
System.out.println(brandSqlFunc.toString());

result grid
[
	[2020-07-10T10:49:09, 2020-07-10 10:49, null], 
	[2020-07-10T10:49:09, 2020-07-10 10:49, 2020-07-27 14:36], 
	[2020-07-10T10:49:09, 2020-07-10 10:49, null], 
	[2020-07-10T10:49:09, 2020-07-10 10:49, 2020-07-24 18:18], 
	[2020-07-10T10:49:09, 2020-07-10 10:49, null], 
	[2020-07-21T14:35:09, 2020-07-21 14:35, null], 
	[2020-07-10T10:49:09, 2020-07-10 10:49, null]
]
```
원하는 방식으로 잘 나온다.     

## 번외편     
MySql REPLACE 함수 

```
REPLACE(yourclolum, origianlData, changeData) // 순서대로 컬럼과 그다음에는 컬럼에서 조회된 데이터중 바꾸고 싶은 데이터, 바꿀 데이터 순이다.
```

현재 mySQL의 dialect클래스를 보면 다른 벤더에는 있는데 이넘만 replace가 없다. ~~아니 왜???~~     

```
List<String> brandSqlFunc = query.select(
									 Expressions.stringTemplate("function('replace', {0}, {1}, {2})", brand.name, "펜더", "휀더") //펜더면 휀더로 바꾸기
									 )
								 .from(brand)
								 .fetch();
System.out.println(brandSqlFunc.toString());

result grid
Caused by: org.hibernate.QueryException: No data type for node: org.hibernate.hql.internal.ast.tree.MethodNode 
 \-[METHOD_CALL] MethodNode: 'function (replace)'
    +-[METHOD_NAME] IdentNode: 'replace' {originalText=replace}
    \-[EXPR_LIST] SqlNode: 'exprList'
       +-[DOT] DotNode: 'brand0_.br_name' {propertyName=name,dereferenceType=PRIMITIVE,getPropertyPath=name,path=brand.name,tableAlias=brand0_,className=io.basquiat.model.Brand,classAlias=brand}
       |  +-[ALIAS_REF] IdentNode: 'brand0_.br_code' {alias=brand, className=io.basquiat.model.Brand, tableAlias=brand0_}
       |  \-[IDENT] IdentNode: 'name' {originalText=name}
       +-[PARAM] ParameterNode: '?' {label=1, expectedType=null}
       \-[PARAM] ParameterNode: '?' {label=2, expectedType=null}
 [select function('replace', brand.name, ?1, ?2)
```

없다니... 그래서 커스터마이징을 해야 한다.

CustomMySqlDialect     

```
package io.basquiat.model;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CustomMySqlDialect extends MySQL8Dialect {
	
	public CustomMySqlDialect() {
		
		super();
		registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
	}

}
```

그리고 나서 persistence.xml을 수정해야 한다.

```
<property name="hibernate.dialect" value="io.basquiat.model.CustomMySqlDialect"/>
```

그리고 다시 실행해 보면    

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        function('replace',
        brand.name,
        ?1,
        ?2) 
    from
        Brand brand */ select
            replace(brand0_.br_name,
            ?,
            ?) as col_0_0_ 
        from
            basquiat_brand brand0_
[에프베이스, 휀더, 포데라, 말로우, 매티슨, 마요네즈, 샌드버그]
```
잘 된다.     

자 그러면 우리는 또 이것을 @QueryDelegate을 이용해서 캡슐화 할 수 있다.     

```
@QueryDelegate(Brand.class)
public static StringTemplate replace(QBrand brand, StringPath column, String target, String replace) {
    return Expressions.stringTemplate("function('replace', {0}, {1}, {2})", column, target, replace);
}
```

그리고 실행하면

```
List<Tuple> brandSqlFunc = query.select(
									 Expressions.stringTemplate("function('replace', {0}, {1}, {2})", brand.name, "펜더", "휀더"),
									 brand.replace(brand.name, "에프베이스", "에픠베이스")
									)
							  .from(brand)
							  .fetch();
System.out.println(brandSqlFunc.toString());

result grid
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        function('replace',
        brand.name,
        ?1,
        ?2),
        function('replace',
        brand.name,
        ?3,
        ?4) 
    from
        Brand brand */ select
            replace(brand0_.br_name,
            ?,
            ?) as col_0_0_,
            replace(brand0_.br_name,
            ?,
            ?) as col_1_0_ 
        from
            basquiat_brand brand0_
[	
	[에프베이스, 에픠베이스], 
	[휀더, 펜더],
	[포데라, 포데라], 
	[말로우, 말로우], 
	[매티슨, 매티슨], 
	[마요네즈, 마요네즈], 
	[샌드버그, 샌드버그]
]
```
참 쉽죠~      

# Using Data manipulation commands And Bulk

## .insert()

일단 4.3.1버전의 reference document가 없어서 이상하긴 한데 이 버전의 경우 insert부분이 삭제되었다.     

3.x대만 해도 사용했던 기억이 나는데....       

아마도 이유가 있을텐데 그래서 insert batch를 사용할 수가 없다. ~~럴수럴수 이럴수가?~~     

좀 의아스럽긴 하지만... 게다가 왠지 이것은 Hibernate 버전과도 좀 연관이 있는듯 싶다.    

업데이트좀 하자... reference doc가 왜 4.1.3까지만 있는지도 의문이고 API 스펙도 변경이 되었는데도.... 일을 안하는 것인가?     

결국 jpa를 이용하기 위해서 몇 가지 설정으로 넣거나 차라리 배치성 인서트의 경우에는 JDBC방식을 사용하는게 차라리 나아 보인다.

## .update()

이것도 일단 스펙이 바꼈다. 문서처럼 .addBatch()를 활용하는 방법은 사용할 수 없다.      

근데 이건 좀 곰곰히 생각해 보면

```
UPDATE basquiat_brand
   SET brand_name = ?
 WHERE br_code in (?, ?, ...) // or number > ? or number < ? 등등등 업데이트할 데이터의 조건에 따라 where절을 사용하면 된다.
```
그냥 이렇게 하는게 더 낫지 않을까?     

### 시나리오

테스트 시나리오에서는 .where()절에 조건을 주고 하지 않겠다. 이것은 여러분들에게 맡기겠다. 어차피 데이터는 7개뿐... ~~귀찮아~~     

1. 모든 브랜드의 브랜드명 뒤에 _update를 붙일 것     

일단 우리가 생각할 수 있는 dirty checking을 이용하게 된다면 다음과 같이 코드를 짤 수 있다.    

그 전에 Brand 엔티티에는 

```
public void changeBrandName(String name) {
	this.name = name;
}
```
를 추가하자. ~~@Setter가 없으니~~     

```
String suffix = "_update";

List<Brand> brandList = query.selectFrom(brand).fetch();
brandList.stream().forEach(b -> b.changeBrandName(b.getName() + suffix));

result grid

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
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_
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
음.... 고민이 된다.       

아무래도 건건히 업데이트 쿼리가 날아가기 때문인데 이것은 좀 위험해 보인다. ~~데이터수가 7개니까 망정이지 만일 엄청 많은 데이터를 저렇게 한다??? 어림도 없지!~~     

그래서 마치 쿼리로 한번에 날리는 방식처럼 작성하는게 좋아 보인다.      

다음과 같이 말이다.

```
String suffix = "_update";
        	
List<Brand> brandList = query.selectFrom(brand).fetch();
//brandList.stream().forEach(b -> b.changeBrandName(b.getName() + suffix));
System.out.println(brandList);

long result = query.update(brand)
				 .set(brand.name, brand.name.concat(suffix))
				 .execute();
	
List<Brand> againBrandList = query.selectFrom(brand).fetch();
System.out.println(result);
System.out.println(againBrandList);
```
자 결과를 살펴보자.    

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
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_
[
	Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=7, launchedAt=2020-07-21T14:35:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55)
]
Hibernate: 
    /* update
        Brand brand 
    set
        brand.name = concat(brand.name,
        ?1) */ update
            basquiat_brand 
        set
            br_name=concat(br_name,
            ?)
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
            basquiat_brand brand0_
7
[
	Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=7, launchedAt=2020-07-21T14:35:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55)
]
```
잘 되었다고 생각하는 순간 결과가 이상하다.     

분명 DB를 살펴보면 잘 바뀐것을 알 수 있다.     

그리고 업데이트된 결과를 확인하기 위해 다시 셀렉트 해왔는데 결과는 업데이트된 값을 가져오지 않는다???      

다시 코드를 살펴보자.      

```
String suffix = "_update";
        	
List<Brand> brandList = query.selectFrom(brand).fetch();
//brandList.stream().forEach(b -> b.changeBrandName(b.getName() + suffix));
System.out.println(brandList);

long result = query.update(brand)
				 .set(brand.name, brand.name.concat(suffix))
				 .execute();
	
List<Brand> againBrandList = query.selectFrom(brand).fetch();
System.out.println(result);
System.out.println(againBrandList);
```
처음에 brandList를 통해서 가져온 정보는 영속성 컨텍스트에 담기게 된다.     

그리고 일종의 bulk업데이트를 치는 코드가 밑에 나가게 되는데 이것은 영속성 컨텍스트와는 관련이 바로 쿼리를 때려박게 된다.    

그리고 다시 셀렉트를 해왔다. 하지만 이 경우에는 'REPEATABLE READ'에 의해서 난중에 가져온 정보를 그냥 버리게 된다.     

따라서 이럴 떄는 다음과 같이     

```
String suffix = "_update";
        	
List<Brand> brandList = query.selectFrom(brand).fetch();
//brandList.stream().forEach(b -> b.changeBrandName(b.getName() + suffix));
System.out.println(brandList);

em.flush();
em.clear();

long result = query.update(brand)
				   .set(brand.name, brand.name.concat(suffix))
				   .execute();
	
List<Brand> againBrandList = query.selectFrom(brand).fetch();
System.out.println(result);
System.out.println(againBrandList);

result grid
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
            brand0_.number as number5_0_,
            brand0_.partner_id as partner_7_0_,
            brand0_.updated_at as updated_6_0_ 
        from
            basquiat_brand brand0_
[
	Brand(code=FBASS, name=에프베이스, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FODERA, name=포데라, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MARLEAUX, name=말로우, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MATTISSON, name=매티슨, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MAYONES, name=마요네즈, enName=Mayones, number=7, launchedAt=2020-07-21T14:35:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=SANDBERG, name=샌드버그, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55)
]
Hibernate: 
    /* update
        Brand brand 
    set
        brand.name = concat(brand.name,
        ?1) */ update
            basquiat_brand 
        set
            br_name=concat(br_name,
            ?)
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
            basquiat_brand brand0_
7
[
	Brand(code=FBASS, name=에프베이스_update, enName=FBass, number=1, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FENDER, name=펜더_update, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=FODERA, name=포데라_update, enName=Fodera, number=3, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MARLEAUX, name=말로우_update, enName=Marleaux, number=4, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MATTISSON, name=매티슨_update, enName=Mattisson, number=5, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=MAYONES, name=마요네즈_update, enName=Mayones, number=7, launchedAt=2020-07-21T14:35:09, updatedAt=2020-08-03T13:56:55), 
	Brand(code=SANDBERG, name=샌드버그_update, enName=Sandberg, number=6, launchedAt=2020-07-10T10:49:09, updatedAt=2020-08-03T13:56:55)
]
```
일종의 꼼수처럼 새로 셀렉트한 녀석을 영속성 컨텍스트에 넣기 위해서 기존의 것을 초기화 해주면 된다.     

뭐 이런 것도 가능할 것이다.     

일종의 좋아요 같은 기능을 구현한다면 

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
        	
        	String suffix = "_update";
        	
        	List<Brand> brandList = query.selectFrom(brand).fetch();
        	//brandList.stream().forEach(b -> b.changeBrandName(b.getName() + suffix));
        	System.out.println(brandList);
        	
        	em.flush();
        	em.clear();
        	
        	long result = query.update(brand)
        					   //.set(brand.number, brand.number.add(1)) // 좋아요 1을 더한다.
        						 .set(brand.number, brand.number.subtract(1)) // 좋아요 해제 1을 뺀다.
        					   .execute();
        		
        	List<Brand> againBrandList = query.selectFrom(brand).fetch();
        	System.out.println(result);
        	System.out.println(againBrandList);
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
이와 관련해서 [문자열 concat 및 number형식의 연산](https://github.com/basquiat78/jpa-with-querydsl/tree/4.query-dsl-select-sub-query#%EB%AC%B8%EC%9E%90%EC%97%B4-concat-%EB%B0%8F-number%ED%98%95%EC%8B%9D%EC%9D%98-%EC%97%B0%EC%82%B0) 참조하면 된다.      

## .delete()

이것도 .update()와 같은 방식을 사용한다.     

특히 em.remove(entity)로 건건히 지우는 것은 좀 위험하다.      

```
DELETE FROM basquiat_brand
 	WHERE br_code = ? // or  br_code in (?, ?, ...) or number > ? or number < ? 등등등 지울 데이터의 조건에 따라 where절을 사용하면 된다.
```
차라리 이게 낫겠지?      

이제 마지막 브랜치이기 때문에 그냥 queryDSL에 대한 전반적인 학습을 다 한 의미로 모든 데이터를 속시원하게 지워보자.

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
        	
        	long goodbyeBrand = query.delete(brand)
        					   		 .execute();
        	
        	long goodbyePartner = query.delete(partner)
			   		 				 .execute();
        	
        	System.out.println(goodbyeBrand);
        	System.out.println(goodbyePartner);
        		
        	List<Brand> brandList = query.selectFrom(brand).fetch();
        	List<Partner> partnerList = query.selectFrom(partner).fetch();
        	System.out.println(brandList);
        	System.out.println(partnerList);
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

마지막 결과!!

```
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* delete 
    from
        Brand brand */ delete 
        from
            basquiat_brand
Hibernate: 
    /* delete 
    from
        Partner partner */ delete 
        from
            basquiat_partner
7
2
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
            basquiat_brand brand0_
Hibernate: 
    /* select
        partner 
    from
        Partner partner */ select
            partner0_.id as id1_1_,
            partner0_.address as address2_1_,
            partner0_.entry_at as entry_at3_1_,
            partner0_.partner_name as partner_4_1_,
            partner0_.updated_at as updated_5_1_ 
        from
            basquiat_partner partner0_
[]
[]
```

# At A Glance     
지금까지 queryDSL에 대한 전반적인 스펙들을 살펴보았다.      

이전에 사용했던 버전과는 다르게 많은 게 바뀌었고 내 나름대로는 다시 공부한 느낌이라 마무리를 한 이 순간은 너무나 상쾌하다.      

~~하지만 queryDSL은 문서를 좀 업데이트하는게 어떨까? 정기석도 아니고...아니다. 사이먼 도미닉은 쌓이면 돈이니 광고도 찍고 일 많이 하네~~     

이제부터는 다른 레파지토리를 통해서 SpringBoot와 연계해서 간단한 토이프로젝트를 한번 만들어 볼까 한다.      

P.S. 그동안 수고많으셨습니다. 많은 도움이 되었으면 좋겠네요!
