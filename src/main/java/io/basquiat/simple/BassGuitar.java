package io.basquiat.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BassGuitar {

	private String name;
	
	private int price;
	
	private String model;
	
	private String color;
	
}
