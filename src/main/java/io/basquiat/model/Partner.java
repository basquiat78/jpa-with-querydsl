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
import lombok.ToString;

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
	
	/** 파트너사가 소유한 브랜드리스트 연관관계 주인은 Brand로 둔다. */
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
