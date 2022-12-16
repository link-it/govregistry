package it.govhub.govregistry.commons.utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

public class Base64StringValidator implements ConstraintValidator<Size, Base64String> {

	private long maxSize;
	private long minSize;
	
	@Override
	public void initialize(Size constraint) {
		this.minSize = constraint.min();
		this.maxSize = constraint.max();
	}
	
	@Override
	public boolean isValid(Base64String content, ConstraintValidatorContext context) {
		if (content == null || content.getValue() == null) {
			return this.minSize == 0;
		} else {
			return content.getValue().length() <= this.maxSize && content.getValue().length() >= this.minSize;
		}
	}

}
