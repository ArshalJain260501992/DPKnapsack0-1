package com.mobiquityinc.packer;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Thing {

	private Integer index;
	private Double weight;

	private Double cost;

	private Integer formattedWeight;
}
