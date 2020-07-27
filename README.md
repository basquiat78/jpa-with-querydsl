# Complex Predicates

## Agenda

여기서는 다음과 같은 방식을 배워볼 것이다.

1. BooleanBuilder을 이용한 방식 사용하기         

2. .where()절에 Predicate, BooleanExpress를 이용한 동적 쿼리 메소드 생성하기          

3. 동적 쿼리 메소드를 위임할 클래스를 만들고 @QueryEntity와 @QueryDelegate를 이용해 사용하기      

1과 2번은 우리가 익히 알고 있는 방식으로 다룬다.         

하지만 3번의 경우에는 1, 2로 작성할 경우 필연적으로 늘어날 수 있는 메소드들을 아예 클래스로 빼놓는 방식이다.     

개인적으로 3번을 선호하다.           

## 시작하기 전에     

Java8 이후 즉, 모던 자바이후에는 함수형 프로그래밍이 가능하게 되면서 그에 따른 몇가지 변화가 생겼다.     

여기서는 이와 관련된 이론을 이야기하기에는 좀 거시기하지만 일단 람다 표현식과 Functional Interface가 추가되고 이제는 함수 자체나 인터페이스를 파라미터로 넘길 수 있다.     

결국 모든 언어의 최종 지점이 함수형 언어로 향하고 있는가라는 생각이 들긴 하는데......       

암튼... 이 이야기를 하는 이유는 우리가 앞서 .where절을 사용하면서 보는 조건식들 떄문이다.    

예를 들면 다음과 같은 코드를 한번 보자.     

```
        	JPAQueryFactory query = new JPAQueryFactory(em);
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");

        	List<Brand> brandList = query.selectFrom(brand)
        								 .where(
        										 brand.enName.eq("Fender"),
        										 brand.name.eq("펜더"))
        								 .fetch();
        	System.out.println(brandList.toString());
        	tx.commit();
```

이 .where메소드를 쭉 따라가면 where절에 들어가는 조건에 따라서 단일이거나 spread parameter sytax에 따라서 배열로 받아치는데 그 타입이 뭔지 한번 확인해 보면 Predicate라는 인터페이스를 받게 되어 있다.    

실제로 QBrand같이 생성된 Q-type의 객체를 들어가보면 엔티티에 정의된 필드 타입에 따라 StringPath, NumberPath등등 변환된 것을 볼 수 있다. 그 객체를 또 따라가다 보면 BooleanExpress같은 객체를 만나게 되고 결국에는 마주하게 되는 것이 Predicate라는 인터페이스이다.     

BooleanExpress는 바로 이 Predicate을 implements, 즉 구현하고 있는 추상 클래스이다.     

이 말인 즉 BooleanExpress나 Predicate타입을 받아드릴 수 있다는 말이다.      

물론 이것은 자바8의 functional interface인 Predicate와는 다르긴 하지면 결국 인터페이스와 깊은 관련이 있다.      

그리고 쭈욱 쭈욱 따라가다보면 다음과 같은

```
@Override
public void addWhere(Predicate e) {
    if (e == null) {
        return;
    }
    e = (Predicate) ExpressionUtils.extract(e);
    if (e != null) {
        validate(e);
        where = and(where, e);
    }
}
```
코드가 있는데, 이 코드를 보면 만일 null이 들어오면은 쿼리에 생성할 때 추가하지 않는다는 것을 알 수 있다.     

```
List<Brand> brandList = query.selectFrom(brand)
						   .where(
									 brand.enName.eq("Fender"),
									 null, 
									 brand.name.eq("펜더"))
						   .fetch();
System.out.println(brandList.toString());

result
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName = ?1 
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
            brand0_.br_en_name=? 
            and brand0_.br_name=?
[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
```
이런 특징이 있다는 것을 먼저 필히 알아두고 가자.     

그러면 실제로 우리가 개발하게되는 API의 경우에는 다양한 값들이 넘어올 것이고 그 중에는 where조건을 위한 값들도 있을 것이다.     

이제는 지금까지 단일 테스트 코드가 아니라 하나의 Repository를 상상해 보자.     

하나의 Repository에는 CRUD를 위한 다양한 코드들이 들어가 있을 것이고 그에 따라서 .where()안에 들어가는 조건들은 동적으로 생성될 필요가 있다.     

그렇다면 위와 같은 경우에는 어떤식으로 할까?     

## BooleanBuilder 

그 중에 하나는 바로 queryDSL에서 제공하는 BooleanBuilder를 사용하는 것이다.     

그럼 어떻게 사용하는 것인가?    

```
JPAQueryFactory query = new JPAQueryFactory(em);
System.out.println("queryDSL로 뭔가 하기 직전!!!");

String enName = "Fender";
String name = "펜더";

BooleanBuilder booleanBuller = new BooleanBuilder();
if(StringUtils.isNotEmpty(enName)) {
	booleanBuller.and(brand.enName.eq(enName));
}

if(StringUtils.isNotEmpty(name)) {
	booleanBuller.and(brand.name.eq(name));
}

List<Brand> brandList = query.selectFrom(brand)
							 .where(booleanBuller)
							 .fetch();
System.out.println(brandList.toString());
tx.commit();
```
위와 같이 사용할 수 있다.     

위 코드는 결국 다음과 같다.     

```
.where(brand.enName.eq(enName).and(brand.name.eq(name)))

또는 

.where(
	   brand.enName.eq(enName),
        brand.name.eq(name)
      )
```
그리고 다음과 같이 

```
String enName = "Fender";
String name = "펜더";

BooleanBuilder booleanBuilder = new BooleanBuilder();
if(StringUtils.isNotEmpty(enName)) {
	booleanBuilder.and(brand.enName.eq(enName));
}

if(StringUtils.isNotEmpty(name)) {
	booleanBuilder.or(brand.name.eq(name));
}

List<Brand> brandList = query.selectFrom(brand)
							 .where(booleanBuilder)
							 .fetch();

result

queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName = ?1 
        or brand.name = ?2 */ select
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
            or brand0_.br_name=?
[Brand(code=FENDER, name=펜더, enName=Fender, number=2, launchedAt=2020-07-10T10:49:09, updatedAt=null)]
```
or 조건도 줄 수 있다.      

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Brand;

/**
 * 
 * created by basquiat
 *
 */
public class JpaMain {

	public static BooleanBuilder makeCondition(String enName, String name) {
		
		BooleanBuilder booleanBuilder = new BooleanBuilder();
	    	if(StringUtils.isNotEmpty(enName)) {
	    		booleanBuilder.and(brand.enName.eq(enName));
	    	}
	    	
	    	if(StringUtils.isNotEmpty(name)) {
	    		booleanBuilder.or(brand.name.eq(name));
	    	}
	    	
	    	return booleanBuilder;
		
	}
	
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
        	JPAQueryFactory query = new JPAQueryFactory(em);
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");

        	String enName = "Fender";
        	String name = "펜더";
        	
        	List<Brand> brandList = query.selectFrom(brand)
        								 .where(makeCondition(enName, name))
        								 .fetch();
        	System.out.println(brandList.toString());
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
이렇게 메소드로 빼서 호출해도 무방하다.     

## Predicate or BooleanExpress

위에서 BooleanExpress가 바로 Predicate Interface를 구현한 추상 클래스라고 언급했다.      

그럼 이제 기존에 작성했던     

```
where(
	 brand.enName.eq("Fender"),
	 brand.name.eq("펜더")
	 )
```
이것도 좀 더 우아하게 만들 수 있지 않을까?     

위에서 언급했던 .where()가 받는 타입이 queryDSL의 Predicate Interface라는 것을 우리는 이미 알고 있다.     

자 그럼 이렇게 코드를 작성할 수 있다.     

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Brand;

/**
 * 
 * created by basquiat
 *
 */
public class JpaMain {

	public static BooleanBuilder makeCondition(String enName, String name) {
		
		BooleanBuilder booleanBuilder = new BooleanBuilder();
    	if(StringUtils.isNotEmpty(enName)) {
    		booleanBuilder.and(brand.enName.eq(enName));
    	}
    	
    	if(StringUtils.isNotEmpty(name)) {
    		booleanBuilder.or(brand.name.eq(name));
    	}
    	
    	return booleanBuilder;
		
	}
	
	public static Predicate enNameEqCondition(String enName) {
		Predicate predicateCondition = null;
		if(StringUtils.isNoneEmpty(enName)) {
			predicateCondition = brand.enName.eq(enName);
		}
		return predicateCondition;	
	}
	
	public static Predicate nameEqCondition(String name) {
		Predicate predicateCondition = null;
		if(StringUtils.isNoneEmpty(name)) {
			predicateCondition = brand.name.eq(name);
		}
		return predicateCondition;	
	}
	
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
        	JPAQueryFactory query = new JPAQueryFactory(em);
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");

        	String enName = "Fender";
        	String name = "펜더";
        	
        	List<Brand> brandList = query.selectFrom(brand)
        								 .where(
        										enNameEqCondition(enName),
        										nameEqCondition(name)
        										)
        								 .fetch();
        	System.out.println(brandList.toString());
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

그리고 이 부분은 

```
public static Predicate enNameEqCondition(String enName) {
	return StringUtils.isNotEmpty(enName) ? brand.enName.eq(enName) : null;	
}

public static Predicate nameEqCondition(String name) {
	return StringUtils.isNotEmpty(name) ? brand.name.eq(name) : null;	
}
```
이렇게 좀 간략하게 표현할 수 있다.     

그리고 이것은 해당 메소드를 조합을 할 수 있다.      

```
package io.basquiat;

import static io.basquiat.model.QBrand.brand;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Brand;

/**
 * 
 * created by basquiat
 *
 */
public class JpaMain {

	public static BooleanBuilder makeCondition(String enName, String name) {
		BooleanBuilder booleanBuilder = new BooleanBuilder();
	    	if(StringUtils.isNotEmpty(enName)) {
	    		booleanBuilder.and(brand.enName.eq(enName));
	    	}
	    	if(StringUtils.isNotEmpty(name)) {
	    		booleanBuilder.or(brand.name.eq(name));
	    	}
	    	return booleanBuilder;
	}
	
	public static BooleanExpression enNameEqCondition(String enName) {
		return StringUtils.isNotEmpty(enName) ? brand.enName.eq(enName) : null;	
	}
	
	public static BooleanExpression nameEqCondition(String name) {
		return StringUtils.isNotEmpty(name) ? brand.name.eq(name) : null;	
	}
	
	public static BooleanExpression enNameAndNameEqCondition(String enName, String name) {
		return enNameEqCondition(enName).and(nameEqCondition(name));
	}
	
	public static BooleanExpression enNameOrNameEqCondition(String enName, String name) {
		return enNameEqCondition(enName).or(nameEqCondition(name));
	}
	
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
        	JPAQueryFactory query = new JPAQueryFactory(em);
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");

        	String enName = "Fender";
        	String name = "펜더";
        	
        	List<Brand> brandList = query.selectFrom(brand)
        								 .where(
        										 //enNameAndNameEqCondition(enName, name)
        										 enNameOrNameEqCondition(enName, name)
        										)
        								 .fetch();
        	System.out.println(brandList.toString());
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
.where() 조건문에 들어가는 여러 비교 메소드들은 코드를 따라가게 되면 BooleanExpress객체를 반환하게 되어 있다.      

따라서 기존의 반환 타입인 Predicate를 BooleanExpress로 변경해도 무방하다. ~~자바 기초입니다.~~     

Java8과 관련해서 2016년에 구입해서 오랜 기간 옆에 두고 봤던 책이 나의 경우에는 Java 8 In Action이다.      

이게 지금은 절판되건 같은데 암튼....      

요즘은 이 출판사에서 나온 Modern Java In Action을 보고 있는데 이게 참... java버전이 어느 순간 벌써 13이 나오고 올해 예정으로 15까지 나온다고 한다.       

이야기가 삼천포로 빠졌는데....     

이렇게 메소드로 쭉 뺴놓으면 언제든지 원하는 메소드를 조합해서 사용할 수 있는 장점이 있다.      

'아니 그러면 클래스에 잡다한 private메소드가 난무할 수 있잖아요???'     

어짜피 우리가 집중할 것은 관련된 비지니스 로직일 뿐이다.     

자 그런데 여러분들은 이런 메소드가 비지니스 로직이 들어있는 클래스에 난무하는게 싫다면 다음과 같은 방식을 통해서 해결할 수 있다.     

## @QueryEntity And @QueryDelegate
나라면 이런 방식을 고려해 볼 수 있는데 다음과 같이 한번 코드를 짜보자.     

나는 이 방법을 선호하는 편이다.    

SQLExtensions

```
package io.basquiat.model;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.annotations.QueryEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

@QueryEntity
public class SQLExtensions {

	/** =================================================== Brand Entity 위임 메소드 작성 =================================================== */
	
	@QueryDelegate(Brand.class)
    public static BooleanExpression codeEq(QBrand brand, String code) {
        return brand.code.eq(code);
    }
	
	@QueryDelegate(Brand.class)
    public static BooleanExpression nameEq(QBrand brand, String name) {
        return brand.name.eq(name);
    }
    
    @QueryDelegate(Brand.class)
    public static BooleanExpression enNameEq(QBrand brand, String enName) {
        return brand.enName.eq(enName);
    }
    
    @QueryDelegate(Brand.class)
    public static BooleanExpression enNameAndNameEq(QBrand brand, String enName, String name) {
        return nameEq(brand, name).and(enNameEq(brand, enName));
    }

    @QueryDelegate(Brand.class)
    public static BooleanExpression enNameOrNameEq(QBrand brand, String enName, String name) {
        return nameEq(brand, name).or(enNameEq(brand, enName));
    }
	
    /** =================================================== Partner Entity 위임 메소드 작성 =================================================== */
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression idEq(QPartner partner, String name) {
        return partner.name.eq(name);
    }
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression nameEq(QPartner partner, String name) {
        return partner.name.eq(name);
    }
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression idAndNameEq(QPartner partner, String id, String name) {
        return idEq(partner, id).or(nameEq(partner, name));
    }
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression idOrNameEq(QPartner partner, String id, String name) {
        return idEq(partner, id).or(nameEq(partner, name));
    }
    
}
```
SQLExtensions라는 클래스를 만들고 위와 같이 코드를 짰다.     

자 그럼 코드를 하나씩 설명해 보겠다.     

```
@QueryDelegate(Brand.class)
public static BooleanExpression codeEq(QBrand brand, String code) {
        return brand.code.eq(code);
    }
```
자 이것은 해당 메소드가 위임받을 클래스를 명시한다.     

즉, codeEq라는 메소드를 위임을 받는 것인데 메소드의 로직은 기존에 해왔던 방식과 크게 다를 바 없지만 파라미터로 QBrand를 받는것을 볼 수 있다.      

그리고 나서 gradle로 새로 젠을 하든 자동으로 생성되는 기능이 활성화되든 QBrand로 가면 어떤 일이 벌어질까?????

```
package io.basquiat.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBrand is a Querydsl query type for Brand
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBrand extends EntityPathBase<Brand> {

    private static final long serialVersionUID = 1611996000L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBrand brand = new QBrand("brand");

    public final StringPath code = createString("code");

    public final StringPath enName = createString("enName");

    public final DateTimePath<java.time.LocalDateTime> launchedAt = createDateTime("launchedAt", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> number = createNumber("number", Integer.class);

    public final QPartner partner;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QBrand(String variable) {
        this(Brand.class, forVariable(variable), INITS);
    }

    public QBrand(Path<? extends Brand> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBrand(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBrand(PathMetadata metadata, PathInits inits) {
        this(Brand.class, metadata, inits);
    }

    public QBrand(Class<? extends Brand> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.partner = inits.isInitialized("partner") ? new QPartner(forProperty("partner")) : null;
    }

    public BooleanExpression enNameAndNameEq(String enName, String name) {
        return SQLExtensions.enNameAndNameEq(this, enName, name);
    }

    public BooleanExpression codeEq(String code) {
        return SQLExtensions.codeEq(this, code);
    }

    public BooleanExpression enNameOrNameEq(String enName, String name) {
        return SQLExtensions.enNameOrNameEq(this, enName, name);
    }

    public BooleanExpression nameEq(String name) {
        return SQLExtensions.nameEq(this, name);
    }

    public BooleanExpression enNameEq(String enName) {
        return SQLExtensions.enNameEq(this, enName);
    }

}
```
어라? SQLExtensions에 작성한 코드가 QBrand로 모두 옮겨진 것을 알 수 있다.     

자 SQLExtensions클래스에서 Partner에 대한 코드로 넣어놨다. 이름은 같아도 상관없다. 어짜피 받는 파라미터가 다르고 타겟 클래스도 다르기 때문인데 그럼 

```
package io.basquiat.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPartner is a Querydsl query type for Partner
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPartner extends EntityPathBase<Partner> {

    private static final long serialVersionUID = 2009274977L;

    public static final QPartner partner = new QPartner("partner");

    public final StringPath address = createString("address");

    public final ListPath<Brand, QBrand> brands = this.<Brand, QBrand>createList("brands", Brand.class, QBrand.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> entryAt = createDateTime("entryAt", java.time.LocalDateTime.class);

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QPartner(String variable) {
        super(Partner.class, forVariable(variable));
    }

    public QPartner(Path<? extends Partner> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPartner(PathMetadata metadata) {
        super(Partner.class, metadata);
    }

    public BooleanExpression idAndNameEq(String id, String name) {
        return SQLExtensions.idAndNameEq(this, id, name);
    }

    public BooleanExpression idEq(String name) {
        return SQLExtensions.idEq(this, name);
    }

    public BooleanExpression nameEq(String name) {
        return SQLExtensions.nameEq(this, name);
    }

    public BooleanExpression idOrNameEq(String id, String name) {
        return SQLExtensions.idOrNameEq(this, id, name);
    }

}
```
오호!!     

사용법은 뭐 안봐도 비디오이다.     

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

        	String brandCode = "FENDER";
        	String enName = "Fender";
        	String name = "펜더";
        	String partnerId = "RIDINBASS";
        	String partnerName = "라이딩 베이스";
        	
        	List<Brand> brandList = query.selectFrom(brand)
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
```
주석된 부분을 하나씩 테스트해보기 바란다. 원하는데로 쿼리가 나가게 된다.     

물론 여기서는 하나의 클래스에 모든 것을 때려박았지만 각 레파지토리의 Root 엔티티를 기준으로 나눠서 작성할 수 있다.      

가령 예를들면 BrandExtensions, PartnerExtensions처럼 따로 클래스를 뺴놓는 방식을 사용할 수 있다.     

그리고 이 방법의 경우에는 단순하게 where조건에만 국한하지 않는다. Path에 대해서도 가능한데 한번 예를 들어보겠다.     

```

@QueryDelegate(Brand.class)
public static <T> Expression<T> constant(QBrand brand, T constant) {
    return Expressions.constant(constant);
}

@QueryDelegate(Brand.class)
public static <T> Expression<T> constant(QBrand brand, T constant, String alias) {
    return ExpressionUtils.as(Expressions.constant(constant), alias);
}
```
다음과 같이도 작성이 가능하다.     

만일 Projection을 사용할 때 저렇게 Expressions같은 유틸을 이용해서 코드를 짜야하는 경우 다음과 같이 처리도 가능하다.      

그럼 실행할때는 


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
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
```
과 같이 아주 우아한 코드로 사용할 수 있다.      

물론 넘어온 파라미터에 대한 null처리를 하지 않았지만 방어적인 코드는 필요하면 사용하면 된다.     

이렇게 queryDSL에서는 @QueryDelegate를 통해서 캡슐화를 제공한다.      

아마도 다음 브랜치는 마지막이 될거 같은데 queryDSL에서 제공하는 CRUD와 물리적인 DB에서 사용하는 함수를 직접적으로 호출하는 방식을 한번 알아보고자 한다.     