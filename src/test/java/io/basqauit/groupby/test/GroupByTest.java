package io.basqauit.groupby.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.basquiat.simple.BassGuitar;

public class GroupByTest {

	@Test
	public void itemEntityTest() {
		
		BassGuitar bass1 = BassGuitar.builder()
									 .name("Fodera")
									 .price(15000000)
									 .model("Emperor Deluxe 5")
									 .color("Blueburst")
									 .build();

		BassGuitar bass2 = BassGuitar.builder()
									 .name("Fender")
									 .price(5400000)
									 .model("Custom Shop 63 Journeyman")
									 .color("Fiesta Red")
									 .build();
		BassGuitar bass3 = BassGuitar.builder()
									 .name("Sandberg")
									 .price(5500000)
									 .model("California TT 5 Masterpiece")
									 .color("Twotone Sunburst")
									 .build();
		
		BassGuitar bass4 = BassGuitar.builder()
									 .name("Fender")
									 .price(3200000)
									 .model("Elite 5")
									 .color("Candy Apple Red")
									 .build();
		
		
		BassGuitar bass5 = BassGuitar.builder()
									 .name("Fender")
									 .price(3000000)
									 .model("Elite 4")
									 .color("Candy Apple Red")
									 .build();
		
		
		BassGuitar bass6 = BassGuitar.builder()
									 .name("Sandberg")
									 .price(5500000)
									 .model("California VS 5 Masterpiece")
									 .color("Black")
									 .build();
		
		
		Map<String, List<BassGuitar>> groupByName = Arrays.asList(bass1, bass2, bass3, bass4, bass5, bass6)
												   .stream()
												   .collect(Collectors.groupingBy(BassGuitar::getName));
		System.out.println(groupByName);
		
		
		System.out.println("====================================");
		
		Map<Integer, List<BassGuitar>> groupByPrice = Arrays.asList(bass1, bass2, bass3, bass4, bass5, bass6)
															.stream()
															.collect(Collectors.groupingBy(BassGuitar::getPrice));
		System.out.println(groupByPrice);
		
	}
	
}
