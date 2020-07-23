# JPA With queryDSL

이 프로젝트는 completedJPA와 같은 환경구성으로 되어 있다.    

[completedJPA](https://github.com/basquiat78/completedJPA)    

## 이거 왜 쓰는건데?    

현재 이 Repository를 작성하는 시점을 기준으로 completedJPA의 진행 현황은 엔티티매핑정도까지뿐이다.    

따라서 이제 JPA에 막 입문한 분은 아직 JPQL이나 @Query를 이용한 native query 또는 Query Builder인 criteria를 경험하지 못했기 왜 이것을 써야 하는지 모를수 도 있다.    

사실 여기서는 그 이유에 대해 설명하지 않을 예정이다. 어짜피 저 위에 것들을 경험하게 될테니 말이다.    

또한 queryDSL을 씹고 뜯고 맛보기 위한 프로젝트이기 때문에 SpringBoot와 jpa를 연계하지 않는다.    

왜냐하면 최종적인 목적인 SpringBoot와의 연계할 예정이기 때문에 기본적인 방법들을 알아가는데 의의를 두고 싶다.    

사실 queryDSL은 혜성처럼 등장한 신기술은 아니다. 제법 연식이 된 녀석이고 또한 jOOQ같은 비슷한 녀석도 존재한다.    

나의 경우에는 몇 년전 jOOQ와 queryDSL을 선택하기 위해 테스트로 해본 정도의 경험이고 깊게 파고들기 전에 queryDSL을 쓰자는 의견으로 통합되면서 그냥 맛만 본 경우라 깊은 비교를 하진 못한다.     

다만 queryDSL이 좀 더 직관적이긴 한데 그 이유는 jOOQ의 경우에는 코딩 스타일이 아스트랄한건 아니지만 좀 번거롭다. table, record라는 개념이 있기도 하고...     

그에 반해 queryDSL은 좀 더 심플하다.     

## 초반 설정이 쉽지 않다    

아주 어려운 것은 아닌데 혹시 gRPC를 경험해 본적이 있는지 모르겠다. 경험이 없어도 그냥 쉽게 설명하자면 이 녀석은 proto라는 서비스 명세서를 가지고 있다. 앞뒤 다 짜르고 결론부터 말하자면 이 파일을 generate해서 비지니스 로직에서 사용할 수 있도록 자바파일로 gen을 하게 되는데 이 방법이 상당이 번거롭기도 하다. 그리고 이 서비스 명세서가 바뀌면 통신하는 서버/클라이트 양쪽에서 다시 gen을 해야하는 번거로움이 있다. 그럼에도 장점이 많으니 은근히 많이 사용하는 녀석이다.    

또한 언어의 벽이 없다. proto파일을 각 언어에 맞게 gen하는 것들이 상당히 많아서 잘 만들어진 Proto파일은 언어 상관없이 구현이 가능하다.      

'왜 알고 싶지도 않은 그런 이야기를 하시는거에요?'     

왜 일까?    

이 녀석도 그런 과정을 거쳐야 하기 때문이다.    

무슨 말이냐면 queryDSL 내부에서 엔티티에 매핑되는 특정 객체를 생성하고 그것을 활용하는 방식으로 작동하는 것처럼 보인다.    

그래서 Q가 붙는 객체를 보게 되는데 가령 Item이라는 엔티티가 있다고 하면 queryDSL은 gen을 하는 과정을 통해 QItem객체를 만들고 이것으로 내부적으로 처리를 하는 방식이다.    

대충 이런 이야기를 했으니 그러면 이제는 설정을 해보자.    


### pom.xml 설정 추가    

SpringBoot2.3.1을 기준으로 다음 버전을 추가할 것이다.

```

<dependency>
	<groupId>com.querydsl</groupId>
	<artifactId>querydsl-apt</artifactId>
	<version>4.3.1</version>
</dependency>

<dependency>
	<groupId>com.querydsl</groupId>
	<artifactId>querydsl-jpa</artifactId>
	<version>4.3.1</version>
</dependency>

<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-simple</artifactId>
	<version>1.7.30</version>
</dependency>

```

slf4j-simple의 경우에는 querydsl-jpa가 디펜던시로 끌고 오는데 이것을 추가하지 않으면 에러가 발생한다.    

물론 에러 자체가 문제가 될 것은 없지만 에러가 나는 것이 거슬려서 일단 추가를 한다.     

gen을 위해서는 maven에 plugin설정을 해줘야 한다.    

pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	
	<modelVersion>4.0.0</modelVersion>
	<groupId>io.basquiat</groupId>
	<artifactId>jpa-with-querydsl</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<maven.test.skip>true</maven.test.skip>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-entitymanager</artifactId>
			<version>5.4.17.Final</version>
		</dependency>
		
		<dependency>
			<groupId>com.querydsl</groupId>
			<artifactId>querydsl-jpa</artifactId>
			<version>4.3.1</version>
		</dependency>
		
		<dependency>
			<groupId>com.querydsl</groupId>
			<artifactId>querydsl-apt</artifactId>
			<version>4.3.1</version>
		</dependency>
		
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-simple</artifactId>
			<version>1.7.30</version>
		</dependency>
		
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>8.0.20</version>
		</dependency>
	
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<version>42.2.14</version>
		</dependency>
	
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.10</version>
		</dependency>
	
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.12</version>
			<scope>provided</scope>
		</dependency>
	
		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-api</artifactId>
			<version>5.6.2</version>
			<scope>test</scope>
		</dependency>
	
		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-engine</artifactId>
			<version>5.6.2</version>
			<scope>test</scope>
		</dependency>
	
	</dependencies>

	<build>
		<plugins>
			
			<plugin>
				<groupId>com.mysema.maven</groupId>
				<artifactId>apt-maven-plugin</artifactId>
				<version>1.1.3</version>
				<executions>
					<execution>
						<goals>
							<goal>process</goal>
						</goals>
						<configuration>
							<outputDirectory>target/generated-sources/java</outputDirectory>
							<processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
						</configuration>
					</execution>
				</executions>
			</plugin>
			
		</plugins>
	</build>

</project>

```

아참 테스트를 하다보니 

```
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.

```

뭔지 모르게 눈에 거슬리길래 봤더니 이렇게 경고를 날려주셔서 persistence.xml에 수정을 좀 해주자.     

```

 <property name="javax.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>

```

기존 completedJPA 환경을 그대로 가져왔지만 일단 다른것들은 싹 다 지우고 Item과 JpaMain클래스만 남겨두었다.

Item.java

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
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Item {
	
	@Builder
	public Item(String name, Integer price) {
		this.name = name;
		this.price = price;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpCreatedAt() {
    	createdAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdate() {
    	updatedAt = LocalDateTime.now();
    }

}


```

JpaMain.java

```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Item;
import io.basquiat.model.QItem;

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

기존과는 크게 달라진 것이 없다.    

어쩌다 보니 sts3로 테스트하고 있는데 깊은 빡침으로 인해서 차후 SpringBoot와 연계할 때는 인텔리제이와 gradle로 갈아탈 것이다.     

일단 sts3에서 엔티티들을 gen을 해보자.

## 저기요? 잠...잠깐만요?    

'혹시 그럼 엔티티가 변경될때마다 gen을 실행해야 하는건가요?'

네. 그렇읍니다.    

그러면 sts3에서는 gen을 해보자.        

## JUST maven install    

처음 설정이 완료되면 다음 이미지처럼 아주 깔끔한 구성을 할 것이다.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/master/capture/capture1.png)    

그러면 다음 이미지처럼 maven install을 실행해 주자.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/master/capture/capture2.png)    

그러면 plugin에 설정한 대로 리소스 폴더가 하나 생기고 그곳에 엔티티의 패키지명을 그대로 옮겨 놓은 Q타입의 객체가 생성된 것을 볼 수 있다.     

여기서는 Item이니깐 QItem이 하나 생성된 것이 보일 것이다.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/master/capture/capture3.png)    

만일 이렇게 했는데 그냥 빌드 로그만 올라오고 폴더 생성이 되지 않았다면 maven update를 해줘야 한다.     

다음 이미지를 참조해서 실행하자. 참고로 source level은 1.8, 즉 자바8이어야만 한다는 것을 일단 언급하고 넘어가자.    

간혹 이렇게 해도 안되는 경우에는 java build path가 제대로 되어 있지 않아서이다. 만일 그렇다면 구글신에게 물어서 제대로 잡아야 한다.    

그리고 maven install 또는 maven update를 해주자.    

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/master/capture/capture4.png) 

이것도 plugin설정으로 prefix을 Q가 아닌 다른 것으로 바꿀 수 있는데 설정 방법을 찾아서 공유를 해보겠다.    

## queryDSL 살짝 핥아보기. ~~하앜하앗~~    

일단 코드부터 보자.    

```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Item;
import io.basquiat.model.QItem;

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
        	
        	Item item = Item.builder().name("Fodera").price(15000000).build();
        	em.persist(item);
        	em.flush();
        	em.clear();
        	System.out.println("일단 DB에 날렸어~");

        	JPAQueryFactory query = new JPAQueryFactory(em);
        	QItem qItem = QItem.item;
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");
        	Item selected = query.selectFrom(qItem).fetchFirst();
        	System.out.println("queryDSL로 일단 조회했어~");
        	System.out.println(selected.toString());

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

JPAQueryFactory으로부터 entitiyManager를 주입해서 queryDSL문법을 사용할 수 있는 객체를 생성했다.    

그리고 우리는 무언가를 하고자 하는 엔티티로부터 queryDSL이 gen을 해준 QItem객체를 꺼내온다.     

그리고 다음과 같은 형식으로 코딩을 하게 된다.     

```
query.// 내가 이제 쿼리를 할껀데    
selectFrom // 어디에서 조회를 해올꺼야 
(qItem)    // 그게 어디인데? -> 조회해올 대상은 item이야 (JPA는 디비 테이블을 객체로 추상화하기 때문에 item이 대상이 된다.)
.fetchFirst() // 조회해온 결과중 가장 첫번째 대상을 가져올께. 또는 fetchOne()을 쓸 수 있다.

```

더 많은 기능이 있지만 우리는 일단 핥아보고 살짝 맛보는게 우선이니깐 문법이 어떻게 되는지만 훝어 본 것이다.   

자 그럼 실행 결과도 확인해 봐야겠지?


```

Jul 08, 2020 4:57:51 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 08, 2020 4:57:52 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 08, 2020 4:57:52 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 08, 2020 4:57:52 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 08, 2020 4:57:52 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jul 08, 2020 4:57:52 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jul 08, 2020 4:57:52 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 08, 2020 4:57:52 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 08, 2020 4:57:52 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    drop table if exists basquiat_item
Jul 08, 2020 4:57:53 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@273c947f] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        createdAt datetime,
        it_name varchar(255),
        it_price integer,
        updatedAt datetime,
        primary key (id)
    ) engine=InnoDB
Jul 08, 2020 4:57:53 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@130a0f66] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Jul 08, 2020 4:57:53 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (createdAt, it_name, it_price, updatedAt) 
        values
            (?, ?, ?, ?)
일단 DB에 날렸어~
queryDSL로 뭔가 하기 직전!!!
Hibernate: 
    /* select
        item 
    from
        Item item */ select
            item0_.id as id1_0_,
            item0_.createdAt as createda2_0_,
            item0_.it_name as it_name3_0_,
            item0_.it_price as it_price4_0_,
            item0_.updatedAt as updateda5_0_ 
        from
            basquiat_item item0_ limit ?
queryDSL로 일단 조회했어~
Item(id=1, name=Fodera, price=15000000, createdAt=2020-07-08T16:57:53, updatedAt=null)
Jul 08, 2020 4:57:53 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    740582 nanoseconds spent acquiring 1 JDBC connections;
    369262 nanoseconds spent releasing 1 JDBC connections;
    13120296 nanoseconds spent preparing 2 JDBC statements;
    4221733 nanoseconds spent executing 2 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    6970680 nanoseconds spent executing 2 flushes (flushing a total of 2 entities and 0 collections);
    34656 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 08, 2020 4:57:53 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]



```

오호~ 설정이 잘 되었다. 또한 queryDSL을 이용해 조회한 로그를 보면 살짝 다르다.   

```

Hibernate: 
    /* select
        item 
    from
        Item item */ select
            item0_.id as id1_0_,
            item0_.createdAt as createda2_0_,
            item0_.it_name as it_name3_0_,
            item0_.it_price as it_price4_0_,
            item0_.updatedAt as updateda5_0_ 
        from
            basquiat_item item0_ limit ?


```

찍힌 로그도 살짝 다르다.  

'뭐가 다른가요?' ~~잘봐요! 뭔지 모르겠는데 살짝 달라요~~    

마치 쿼리를 짜듯이 작성도 가능하다.


```

package io.basquiat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.basquiat.model.Item;
import io.basquiat.model.QItem;

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
        	
        	Item item = Item.builder().name("Fodera").price(15000000).build();
        	Item item1 = Item.builder().name("Fender").price(2500000).build();
        	em.persist(item);
        	em.persist(item1);
        	em.flush();
        	em.clear();
        	System.out.println("일단 DB에 날렸어~");

        	JPAQueryFactory query = new JPAQueryFactory(em);
        	QItem qItem = QItem.item;
        	System.out.println("queryDSL로 뭔가 하기 직전!!!");
        	JPAQuery<Tuple> names = query.select(qItem.name, qItem.price).from(qItem);
        	System.out.println("queryDSL로 일단 조회했어~");
        	System.out.println(names.fetch());

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

이 부분을 살펴보자.    

```

JPAQuery<Tuple> names = query.select(qItem.name, qItem.price).from(qItem);

```

마치 이거랑 비슷하지 않나?

```

SELECT name, price FROM basquit_item;

```
tuple로 받아칠 테니 결과는 다음과 같이 나올 것이다.    

튜..튜플은 뭔데?    

[관계 (데이터베이스)](https://ko.wikipedia.org/wiki/%EA%B4%80%EA%B3%84_(%EB%8D%B0%EC%9D%B4%ED%84%B0%EB%B2%A0%EC%9D%B4%EC%8A%A4))

```
Jul 08, 2020 5:19:17 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 08, 2020 5:19:17 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 08, 2020 5:19:17 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 08, 2020 5:19:18 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 08, 2020 5:19:18 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jul 08, 2020 5:19:18 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jul 08, 2020 5:19:18 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 08, 2020 5:19:18 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 08, 2020 5:19:18 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    drop table if exists basquiat_item
Jul 08, 2020 5:19:18 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@273c947f] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        createdAt datetime,
        it_name varchar(255),
        it_price integer,
        updatedAt datetime,
        primary key (id)
    ) engine=InnoDB
Jul 08, 2020 5:19:18 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@130a0f66] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Jul 08, 2020 5:19:18 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (createdAt, it_name, it_price, updatedAt) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (createdAt, it_name, it_price, updatedAt) 
        values
            (?, ?, ?, ?)
일단 DB에 날렸어~
queryDSL로 뭔가 하기 직전!!!
queryDSL로 일단 조회했어~
Hibernate: 
    /* select
        item.name,
        item.price 
    from
        Item item */ select
            item0_.it_name as col_0_0_,
            item0_.it_price as col_1_0_ 
        from
            basquiat_item item0_
[[Fodera, 15000000], [Fender, 2500000]]
Jul 08, 2020 5:19:19 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    530238 nanoseconds spent acquiring 1 JDBC connections;
    465305 nanoseconds spent releasing 1 JDBC connections;
    13590152 nanoseconds spent preparing 3 JDBC statements;
    5475007 nanoseconds spent executing 3 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    6499116 nanoseconds spent executing 1 flushes (flushing a total of 2 entities and 0 collections);
    35518 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 08, 2020 5:19:19 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]


```



## 저기요? 근데 이거 써야 할 장점이 안보이는데요?    

'아니? em.find(Item.class, 1L)을 하는게 더 쉬워보이는데? 왜 이딴 걸 쓰나요? 번거롭게 gen도 해야하고?'    

네. 지금은 구구절절 옳습니다.    

아마도 sql 고수분이라면 이렇게 말할 수도 있겠다.     

'이럴거면 그냥 myBatis가 더 좋아보이는데?'

myBatis도 사실 무시할 수 없다. 충분히 객체지향적인 방식으로 정말 간지나게 사용할 수 있는 좋은 프레임워크이다.     

어쨰든 JPA를 쓴다면 특히 SpringBoot와 함께라면 spring-data-jpa가 제공하는 다양한 방식으로도 어느 정도 커버할 수 있고 앞에서도 말했듯이 criteria, JPQL, native Query도 활용할 수 있다.    

하지만 실무에서는 단순하게 CRUD만으로 절대 끝나지도 않고 원하는 데이터를 뽑아오기 위해서는 정형화되지 않거나 연관관계가 없는 테이블로부터 조인하고 지지고 볶고 해야한다.     

특히 동적 쿼리와 관련된 이슈 하나만으로도 충분히 queryDSL을 써야 하는 충분한 이유가 되기도 하고 특히 type safe하다. 엄격하게 적용되기 때문에 오타같은 것들은 이미 코드레벨에서 알 수 있다.     

아직까지는 장점이 크게 와 닿지 않을지도 모른다. 하지만 이렇게 연계해서 쓰는것이 인기인 것에는 다 그 이유가 있기 때문이다.    

그리고 배워두면 다 도움이 되는 것들 아니겠는가?   

# At A Glance    

자바 진영에서 JPA는 확실히 러닝커브가 있다. 그렇다고 다른 진영의 ORM보다 멋지냐? 그것도 아니다.     

같이 회사를 다녔던 후배 녀석은 파이썬으로 넘어갔는데 나보고 그러더라?

"형, 파이썬하자!. Django 엄청 좋아. ORM? JPA구려서 못써! 드루와~"    

뭐가 되었든 이것도 재미있으니깐~    

P.S 잘 안되면 issue에 남겨주세요.    

# COMPLETE BRANCH    

[basic queryDSL](https://github.com/basquiat78/jpa-with-querydsl/tree/1.query-dsl-basic)    

[SEARCH queryDSL](https://github.com/basquiat78/jpa-with-querydsl/tree/2.query-dsl-select)    

[Order By And Paging](https://github.com/basquiat78/jpa-with-querydsl/tree/3.query-dsl-orderby-n-paging)    

[Select Clause And Sub Query](https://github.com/basquiat78/jpa-with-querydsl/tree/4.query-dsl-select-sub-query)     

[JOIN](https://github.com/basquiat78/jpa-with-querydsl/tree/5.query-dsl-join-and-aggregation)     

[GROUP BY](https://github.com/basquiat78/jpa-with-querydsl/tree/6.query-dsl-groupby-aggregation)    

# WINDOW10 With STS3 MAVEN apt-maven-plugin     

맥에서는 이런 문제가 발생하지 않는데 윈도우10과 관련해서는 이런 오류가 있다는 피드백을 받았다. 

그래서 집에 있는 윈도우에서 클론을 받았더니 진짜 에러가 발생했다.

pom.xml에 <execution>에 에러가 뜬다!!!!!.     

테스트해본 결과 작동은 잘되는데 엔티티가 바뀔 때마다 자동으로 젠을 하는 기능이 막히게 되는 문제이다.     

이것은
 
```
You need to run build with JDK or have tools.jar on the classpath.If this occures during eclipse build make sure you run eclipse under JDK as well (com.mysema.maven:apt-maven-plugin:1.1.3:process:default:generate-sources)
```
다음과 같은 에러가 발생한다. 

해결 방법은 보통 윈도우에서 STS3를 깔면  'sts-3.9.7.RELEASE'폴더 밑으로 관련 파일들이 들어있는데 

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/master/capture/capture5.png)    

이미지에서 맨 밑에 STS.ini파일을 열어서 다음과 같이 추가하면 된다.    

```
-startup
plugins/org.eclipse.equinox.launcher_1.5.200.v20180922-1751.jar
// 추가되는 부분
-vm
C:/Program Files/ojdkbuild/java-1.8.0-openjdk-1.8.0.252-2/bin/javaw.exe
// 추가되는 부분
--launcher.library
plugins/org.eclipse.equinox.launcher.win32.win32.x86_64_1.1.900.v20180922-1751
-product
org.springsource.sts.ide
--launcher.defaultAction
openFile
-vmargs
-Dosgi.requiredJavaVersion=1.8
--add-modules=ALL-SYSTEM
-Xms40m
-Dosgi.module.lock.timeout=10
-Dorg.eclipse.swt.browser.IEVersion=10001
-Xmx1200m
-javaagent:C:\Users\basquiat\Documents\sts\sts-3.9.7.RELEASE\lombok.jar
```
재시작 하면 사라진다.

![실행이미지](https://github.com/basquiat78/jpa-with-querydsl/blob/master/capture/DEEPBBAK.jpg)    

queryDSL과 관련된 내용이 끝나면 SpringBoot와 연계할 때는 인텔리제이와 그래들로 진행할 것이다.     