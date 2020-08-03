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
import lombok.Setter;
import lombok.ToString;

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

	public void changeBrandName(String name) {
		this.name = name;
	}
	
	/** 브랜드 영문 명 */
	@Column(name = "br_en_name")
	private String enName;
	
	/** 테스트 용 */
	@Setter
	private int number;

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
