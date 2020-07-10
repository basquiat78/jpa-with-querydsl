# Basic queryDSL    


## 시작하기 전에    

우리는 실제로 queryDSL을 활용하기 전에 앞에서 언급했던 JPQL, criteria와 한번 비교하는 시간을 가져볼까 한다.


## 도메인 설계     

회사에서 실제로 사용하고 있는 테이블을 흉내내려고 한다.    

실제로는 컬럼이 많지만 필요한 것만 두었다.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/1.query-dsl-basic/capture/capture1.png)    

다음과 같은 도메인을 보고 얘기를 하자.    

브랜드라는 테이블과 파트너사, 즉 입점 업체 정보를 담고 있는 테이블이 존재한다.    

원래라면 브랜드는 또 여러 아이템을 가질 수 있어서 1:N 연관관계로 맺어져 있다.    

일단은 심플하게 가져가는 것이고 아직은 completedJPA에서는 조만간 이와 관련된 내용을 업데이트 할 예정이기에 그냥 가볍게 듣고 가면 된다.    

여기서는 JPA보다는 queryDSL에 포커스를 맞추고 있기 때문이다.     

Brand.java

```
package io.basquiat.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_brand")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(exclude = "partner")
public class Brand {
	
	@Builder
	public Brand(String code, String name, String enName, Partner partner) {
		this.code = code;
		this.name = name;
		this.enName = enName;
		this.partner = partner;
	}

	/** 브랜드 코드 */
	@Id
	@Column(name = "br_code")
	private String code;

	/** 브랜드 명 */
	@Column(name = "br_name")
	private String name;

	/** 브랜드 영문 명 */
	@Column(name = "br_en_name")
	private String enName;

	/** 브랜드를 소유한 업체 */
	// brand입장에서 partner는 N:1이다.
	// partner는 여러개의 brand를 소유할 수 있다. 1:N
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_id")
	private Partner partner;

	@Column(name = "launched_at")
	private LocalDateTime launchedAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void onLaunchedAt() {
    	launchedAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdatedAt() {
    	updatedAt = LocalDateTime.now();
    }

}

```


Parnter.java

```
package io.basquiat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "basquiat_partner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "brands")
public class Partner {

	@Builder
	public Partner(String id, String name, String address, List<Brand> brands) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.brands = brands;
	}	
	
	/** 파트너 아이디 */
	@Id
	private String id;

	/** 파트너 명 */
	@Column(name = "partner_name")
	private String name;
	
	/** 파트너 주소 */
	private String address;
	
	/** 파트너사가 소유한 브랜드리스트 */
	@OneToMany(mappedBy = "partner", fetch = FetchType.LAZY)
	private List<Brand> brands = new ArrayList<>();
	
	@Column(name = "entry_at")
	private LocalDateTime entryAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void onEntryAt() {
    	entryAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdatedAt() {
    	updatedAt = LocalDateTime.now();
    }
	
}

```

간략하게 설명하면 파트너사는 여러개의 브랜드를 가질 수 있다. 그래서 Partner에서는 @OneToMany로 연관관계를 맺고 있다.   

또한 Brand.java에서는 @ManyToOne으로 Partner와 연관관계를 맺고 잇다.    

사실 양방향 매핑을 할 이유는 없다. Brand.java에서만 단방향 매핑 설정만으로도 충분하다.     

하지만 예제차원에서 양방향 매핑을 했다.    

이런 이야기는 차후 completedJPA에서 자세하게 설명하기로 하고 다음 코드를 실행해서 데이터를 일단 밀어넣어 보자.    

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	Partner musicForce = Partner.builder().id("MUSICFORCE")
        										  .name("뮤직포스")
        										  .address("청담동 어딘가 있다.")
        										  .build();
        	
        	Partner ridinBass = Partner.builder().id("RIDINBASS")
												 .name("라이딩 베이스")
												 .address("합정동 어딘가 있다.")
												 .build();
							
        	em.persist(ridinBass);
        	em.persist(musicForce);

        	Brand fodera = Brand.builder().code("FODERA")
        								  .name("포데라")
        								  .enName("Fodera")
        								  .partner(musicForce)
        								  .build();

        	Brand fender = Brand.builder().code("FENDER")
										  .name("펜더")
										  .enName("Fender")
										  .partner(musicForce)
										  .build();
        	
        	Brand fBass = Brand.builder().code("FBASS")
										 .name("에프베이스")
										 .enName("FBass")
										 .partner(musicForce)
										 .build();
        	
        	Brand sandberg = Brand.builder().code("SANDBERG")
										  	.name("샌드버그")
											.enName("Sandberg")
											.partner(ridinBass)
											.build();
        	
        	Brand marleaux = Brand.builder().code("MARLEAUX")
											.name("말로우")
											.enName("Marleaux")
											.partner(ridinBass)
											.build();
        	
        	Brand mattisson = Brand.builder().code("MATTISSON")
											 .name("매티슨")
											 .enName("Mattisson")
											 .partner(ridinBass)
											 .build();
        	
        	em.persist(fodera);
        	em.persist(fender);
        	em.persist(fBass);
        	em.persist(sandberg);
        	em.persist(marleaux);
        	em.persist(mattisson);
        	
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

기본 키 전략매핑은 브랜드와 파트너사의 경우에는 코드로 관리하기 위해서 직접 매핑하는 방식을 취하고 있다.    

이제 데이터를 밀어넣어봤다.

# JPQL(Java Persistence Query Language) 방식    

실무에서 가장 골치아픈 것은 CUD가 아니다. 오히려 Read에 해당하는 조회 부분이다.     

자세한 설명보다는 일단 어떤 방식인지 확인만 하고 지나가자.    

그 전에 앞서 데이터를 밀어넣었기 때문에 hibernate.hbm2ddl.auto를 none으로 하고 테스트하자.    

```
package io.basquiat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

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
        	
        	StringBuffer brandSql = new StringBuffer();
        	brandSql.append("SELECT ")
		        	.append(" brand ")
		        	.append("FROM ")
		        	.append(" Brand brand");
        	
        	//String brandSql = "SELECT brand FROM Brand brand";
        	
        	
        	TypedQuery<Brand> queryBrands = em.createQuery(brandSql.toString(), Brand.class);
        	List<Brand> brandResult = queryBrands.getResultList();
        	for(Brand brand : brandResult) {
        		System.out.println("======================");
        		System.out.println(brand.toString());
        		System.out.println(brand.getPartner().toString());
        		System.out.println("======================");
        	}
        	
        	StringBuffer partnerSql = new StringBuffer();
        	partnerSql.append("SELECT ")
		        	  .append(" partner.id, ")
		        	  .append(" partner.name, ")
		        	  .append(" partner.address ")
		        	  .append("FROM ")
		        	  .append(" Partner partner");
        	
        	//String partnerSql = "SELECT partner FROM Partner partner";
        	
        	Query queryPartner = em.createQuery(partnerSql.toString());
        	
        	@SuppressWarnings("rawtypes")
			List resultList = queryPartner.getResultList();
        	for(Object object : resultList) {
    			Object[] results = (Object[]) object;
    			System.out.println("One Row start");
    			for(Object result : results) {
    				System.out.println(result);
    			}
    			System.out.println("One Row end");
    		}
        	
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
Jul 10, 2020 11:51:55 AM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 10, 2020 11:51:56 AM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 10, 2020 11:51:56 AM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 10, 2020 11:51:56 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 10, 2020 11:51:56 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jul 10, 2020 11:51:56 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jul 10, 2020 11:51:56 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 10, 2020 11:51:56 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 10, 2020 11:51:56 AM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Jul 10, 2020 11:51:57 AM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* SELECT
        brand 
    FROM
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
Hibernate: 
    /* SELECT
        partner.id,
        partner.name,
        partner.address 
    FROM
        Partner partner */ select
            partner0_.id as col_0_0_,
            partner0_.partner_name as col_1_0_,
            partner0_.address as col_2_0_ 
        from
            basquiat_partner partner0_
One Row start
MUSICFORCE
뮤직포스
청담동 어딘가 있다.
One Row end
One Row start
RIDINBASS
라이딩 베이스
합정동 어딘가 있다.
One Row end
Jul 10, 2020 11:51:57 AM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    698855 nanoseconds spent acquiring 1 JDBC connections;
    438452 nanoseconds spent releasing 1 JDBC connections;
    10696643 nanoseconds spent preparing 4 JDBC statements;
    4235843 nanoseconds spent executing 4 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    1479770 nanoseconds spent executing 1 flushes (flushing a total of 8 entities and 2 collections);
    8795948 nanoseconds spent executing 2 partial-flushes (flushing a total of 8 entities and 8 collections)
}
Jul 10, 2020 11:51:57 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]


```

검색 조건의 경우에는 파라미터 바인딩 방식을 활용할 수 있으며 암튼 저런 방식이다.

프로젝션과 관련해서 객체 자체를 반환하는 TypedQuery에 경우에는 그나마 괜찮다.    

하지만 튜플 형식일 경우? 저거 실무에서 절대 못쓴다. 캐스팅을 해야하기 때문에 데이터를 꺼내 쓰는거 자체도 힘들다. 가독성은 확실히 떨어질 것이다.        

또한 쿼리 작성시 오타 및 잘못된 매핑의 경우에는 코드레벨에서 알 수 가 없다. 만일 API의 경우에는 누군가가 해당 쿼리를 사용하는 API를 날리기 전까지는 또 에러가 났는지도 모른다는 것이다.    

물론 장점중 하나는 DTO에 바로 매핑해서 가져올 수 있는 방법이 있다.    

보통 엔티티를 반환하지 말고 DTO을 반환하라는 말이 책에도 언급된다. 하지만 그 장점이 또 단점이 되는 것이 쿼리를 만들 때 해당 DTO의 패키지을 전부 써야 한다. 만일 패키지명이 길면???    

![실행이미지](https://www.newiki.net/w/images/5/53/More_details_be_omitted.jpg)

어째든 동적 쿼리 생성시에는 더 복잡해 질 것이다. 

# Criteria    

일단 이 녀석은 좀 자바스럽게 코드를 짤 수 있다. 어떻게 보면 queryDSL과 비스무리하지만 일단 코드부터 살펴보자.     


```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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
        	CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        	CriteriaQuery<Brand> queryBrand = criteriaBuilder.createQuery(Brand.class);
        	
        	Root<Brand> brand = queryBrand.from(Brand.class);
        	CriteriaQuery<Brand> criteriaQuery = queryBrand.select(brand)
        													.where(criteriaBuilder.equal(brand.get("enName"), "Marleaux"));
			
        	Brand marleaux = em.createQuery(criteriaQuery).getSingleResult();
        	
        	System.out.println(marleaux.toString());
        	
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

입코딩으로 설명하자면     

1. entityManager로부터 CriteriaBuilder를 가져온다.    

2. CriteriaBuilder에게 어떤 엔티티인지 알려준다.    

3. 반환 타입, Root를 통 쿼리 결과를 어떤 녀석이랑 매핑할지 정의해 준다. (queryDSL이랑 약간 비스무리??)    

4. 실제로 쿼리를 메써드 체이닝 방식으로 작성한다.    

5. 그 이후에는 JPQL이랑 비슷하다.    

이 방법은 어느 정도 동적 쿼리 생성에 대한 나름대로의 좋은 방법론을 제시한다.    

하지만 코드 자체를 보면 뭔가 막 꼬아서 작성하게 되는데 이것이 지금처럼 단순하면 상관없지만 좀 더 복잡해지면 상당히 어려워지고 가독성 측면에서 엄청 떨어진다.    

결과 한번 확인해 보자.    

```    

Jul 10, 2020 1:03:18 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 10, 2020 1:03:18 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 10, 2020 1:03:19 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 10, 2020 1:03:19 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 10, 2020 1:03:19 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jul 10, 2020 1:03:19 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jul 10, 2020 1:03:19 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 10, 2020 1:03:19 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 10, 2020 1:03:19 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Jul 10, 2020 1:03:20 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* select
        generatedAlias0 
    from
        Brand as generatedAlias0 
    where
        generatedAlias0.enName=:param0 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.partner_id as partner_6_0_,
            brand0_.updated_at as updated_5_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name=?
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Jul 10, 2020 1:03:20 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    633453 nanoseconds spent acquiring 1 JDBC connections;
    408221 nanoseconds spent releasing 1 JDBC connections;
    11608731 nanoseconds spent preparing 1 JDBC statements;
    1510847 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    7297120 nanoseconds spent executing 1 flushes (flushing a total of 1 entities and 0 collections);
    39055 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 10, 2020 1:03:20 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]


```

일단 뭐가 되었든 로마로 가고는 있다.     

~~여러분 저렇게 쓰고 싶어요?~~

# queryDSL    

그럼 너님께서 그렇게 떠들어 대는 queryDSL을 쓰면 저 위에것들보다 훨씬 쉽고 가독성도 좋고 생산성도 좋고 그렇다는 건가요? ~~응~~    

일단 우리는 코드로 이야기하는거다.    


```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Brand;
import io.basquiat.model.Partner;
import static io.basquiat.model.QBrand.*;

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
        	Brand selectedBrand = query.select(brand)
        					   .from(brand)
        					   .where(brand.enName.eq("Marleaux"))
        					   .fetchOne();
			System.out.println(selectedBrand.toString());
			
			System.out.println("Lazy Loadging, 곧 쿼리가 날아가겠지.");
			Partner partner = selectedBrand.getPartner();
			System.out.println(partner.toString());
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

결과?

```
Jul 10, 2020 3:59:09 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 10, 2020 3:59:09 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 10, 2020 3:59:09 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 10, 2020 3:59:09 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 10, 2020 3:59:09 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jul 10, 2020 3:59:09 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jul 10, 2020 3:59:09 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 10, 2020 3:59:09 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 10, 2020 3:59:09 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Jul 10, 2020 3:59:10 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        brand 
    from
        Brand brand 
    where
        brand.enName = ?1 */ select
            brand0_.br_code as br_code1_0_,
            brand0_.br_en_name as br_en_na2_0_,
            brand0_.launched_at as launched3_0_,
            brand0_.br_name as br_name4_0_,
            brand0_.partner_id as partner_6_0_,
            brand0_.updated_at as updated_5_0_ 
        from
            basquiat_brand brand0_ 
        where
            brand0_.br_en_name=?
Brand(code=MARLEAUX, name=말로우, enName=Marleaux, launchedAt=2020-07-10T10:49:09, updatedAt=null)
Lazy Loadging, 곧 쿼리가 날아가겠지.
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
Jul 10, 2020 3:59:10 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    670158 nanoseconds spent acquiring 1 JDBC connections;
    453821 nanoseconds spent releasing 1 JDBC connections;
    9914547 nanoseconds spent preparing 2 JDBC statements;
    2571232 nanoseconds spent executing 2 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    8412027 nanoseconds spent executing 1 flushes (flushing a total of 2 entities and 1 collections);
    41572 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 10, 2020 3:59:10 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```

이 코드 부분을 살펴보자.     

```
JPAQueryFactory query = new JPAQueryFactory(em);
Brand selectedBrand = query.select(brand)
        					   .from(brand)
        					   .where(brand.enName.eq("Marleaux"))
        					   .fetchOne();

```

마치 criteria	와 비슷해 보이지만 과정 자체가 상당히 심플해졌다.

또한 현재는 JPAQueryFactory를 통해서 객체를 하나 꺼내오는 코드가 있지만 실제 SpringBoot와 연계하게 되면 다 사라진다.     

따라서 실제로는 쿼리를 생성하는데 최적화된다.    

### 잠깐만요? 이전 브랜치랑 뭐가 좀 다르다구욧!!!   

눈썰미가 좋군요!    

아마도 이런 코드가 없어졌을 것이다.    

```
QBrand qBrand = QBrand.brand;
```

gen이 된 코드를 한번 따라가 보자.    

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

}

```

생성된 코드에서 이 부분을 살펴보자.

```
public static final QBrand brand = new QBrand("brand");
```

new QBrand("brand"); <-- brand를 주입하고 있다.    


원래 저 부분은 QType의 alias를 주는 부분이다. 이것은 셀프 조인하는 경우에는 별칭을 각기 줘야 하기 때문에 이런 방식으로 코드를 짜게 된다.    

내부적으로 gen을 할 때 클래스의 명을 lowerCase로 변환해서 기본적인 값으로 세팅하는 듯하다.    

```
JPAQueryFactory query = new JPAQueryFactory(em);
QBrand qBrand = new QBrand("brand"); // 그냥 고유한 값이면 된다. 말 그대로 구분하기 위한 별칭을 세팅하는 부분이다.

Brand selectedBrand = query.select(qBrand)
        					   .from(qBrand)
        					   .where(qBrand.enName.eq("Marleaux"))
        					   .fetchOne();

```

하지만 특별한 경우가 아니라면 'QBrand qBrand = QBrand.brand'로 간략하게 쓸 수 있다.     

그리고 이것을 더 간략하게 쓸 수 있는 방법은 import static으로 해당 객체를 올리는 방법이 있다.     

SpringBoot의 WebFlux를 구성할 떄 Functional Endpoint 방식을 사용해 본 사람이거나 혹시 CouchBase를 사용하면 자체 queryBuilder를 사용할 떄 저런 식으로 임포트해서 사용하는 것을 봤을 것이다.     


그것은 코드에서 public static final로 정의한 필드들에 대해서 선언한 변수명으로 그냥 접근 할 수 있도록 한다.    

따라서 비지니스 로직 내부에 한줄이라도 아끼고 싶으면 저런 방식을 사용하는 것도 좋다. 그리고 그게 좀 더 가독성에서 유리한 부분도 있기도 하다.    

지금까지는 그냥 기본적으로 사용하는 방법들과 JPA에서 제공하는 JPQL, Criteria와의 비교를 한 번 해보았다.     

다음 브랜치에서는 queryDSL에서 제공하는 각종 기능들을 통해서 좀 더 복잡한 쿼리를 마치 자바 코드로 익숙하게 짜는 방식을 세세하게 볼 예정이다.    

# At A Glance     

일단 macos와 sts3의 궁합이 정말 저질이다. 뭐 하긴 예전부터 이클립스랑 메이븐의 궁합이 않 좋긴 했다.         

그래서 pom.xml을 좀 살짝 바꿨는데도 문제는 비지니스 로직을 짜는 도중 오류가 나면 생성한 QType의 코드가 사라졌다고 오류가 해결되면 다시 나타나는 이상한 버그가 있다.    
    
결국 인텔리제이를 써야 하나?    

아니면 gradle?     

그리고 JPA와 queryDSL관련해서 솔직히 나의 생각을 말하면 나는 JPA나 queryDSL같은 것이 모든 것을 다 커버한다고 생각하지 않는다.     

queryDSL도 결국 JPA에서 제공하는 JPQL, Criteria 문법을 편하게 쓰기 위해 사용하는 것이다.    

요즘은 어떤지 모르겠는데 대기업 프로젝트의 경우 조직도  ~~크....조직도 쿼리를 위해 재귀성 함수의 오라클 connect by는 정말...~~ 같은 특수한 경우도 만날 수 있는데 또 이런건 지원을 하지 않는다.    

만능이 아니라는 것이다. 그래서 native SQL을 지원한다. ~~결국 어떤 부분에서는 SQL을 써야 하는거군요~~    

뿐만 아니라 그런 부분을 해소하기 위해서 Spring에서 myBatis, JDBCTemplate을 함께 쓸 수 있도록 지원한다.

데이터베이스의 테이블을 객체로 추상화한 것이 JPA라고 하지만 SQL을 잘 하시는 분들이라면 그 사이의 갭이 존재한다는 것을 알 수 있다.     

하지만 객체 지향 관점에서 그것을 하나의 패러다임으로 묶기위한 노력의 산물이 아닌가 싶다.   

그리고 queryDSL은 질의 방식을 자바 개발자의 패턴에 맞춰서 작성하는데 있어서 그 장점을 찾아 볼 수 있다.