package io.basquiat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO {
	
	/** 생산품 고유 아이디 */
	private Long id;

	/** 생산품 명 */
	private String productName;
	
	/** 생산품 가격 */
	private int price;
	
	/** 브랜드 명 */
	private String brandName;
	
	/** 모델 */
	private String model;
	
	/** 색상 */
	private String color;
	
	/** product count */
	private long productCount;
	
}
