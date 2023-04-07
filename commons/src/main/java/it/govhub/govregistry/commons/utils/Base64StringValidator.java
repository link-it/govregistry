/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
