package com.mobiquityinc.packer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class APIException extends Exception {

	/**
	 * Default Serialization ID
	 */
	private static final long serialVersionUID = 1L;

	@NonNull
	final String errorMessage;

}
