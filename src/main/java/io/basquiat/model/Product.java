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
