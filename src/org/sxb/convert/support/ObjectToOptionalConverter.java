/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sxb.convert.support;

import java.util.Collections;
import java.util.Set;

import org.sxb.convert.ConversionService;
import org.sxb.convert.TypeDescriptor;
import org.sxb.convert.converter.ConditionalGenericConverter;
import org.sxb.kit.ClassKits;
import org.sxb.lang.UsesJava8;

/**
 * Convert an Object to {@code java.util.Optional<T>} if necessary using the
 * {@code ConversionService} to convert the source Object to the generic type
 * of Optional when known.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 4.1
 */
@UsesJava8
final class ObjectToOptionalConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;
	/** Java 8's java.util.Optional.empty() */
	private static Object javaUtilOptionalEmpty = null;
	private static Class<?> clazz = null;
	static {
		try {
			clazz = ClassKits.forName("java.util.Optional", GenericConversionService.class.getClassLoader());
			javaUtilOptionalEmpty = ClassKits.getMethod(clazz, "empty").invoke(null);
		}
		catch (Exception ex) {
			// Java 8 not available - conversion to Optional not supported then.
		}
	}

	public ObjectToOptionalConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}


	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object.class, javaUtilOptionalEmpty.getClass()));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (targetType.getResolvableType() != null) {
			return this.conversionService.canConvert(sourceType, new GenericTypeDescriptor(targetType));
		}
		else {
			return true;
		}
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		
		try {
			if (source == null) {
				return ClassKits.getMethod(clazz, "empty").invoke(null);
				//return Optional.empty();
			}
			//else if (source instanceof Optiona) {
			else if(sourceType.getObjectType().equals(javaUtilOptionalEmpty.getClass())){
				return source;
			}
			else if (targetType.getResolvableType() == null) {
				return ClassKits.getMethod(clazz, "of").invoke(source);
				//return Optional.of(source);
			}
			else {
				Object target = this.conversionService.convert(source, sourceType, new GenericTypeDescriptor(targetType));
				return ClassKits.getMethod(clazz, "ofNullable").invoke(target);
				//return Optional.ofNullable(target);
			}
		}
		catch (Exception ex) {
			// Java 8 not available - conversion to Optional not supported then.
		}
		return clazz;
	}


	@SuppressWarnings("serial")
	private static class GenericTypeDescriptor extends TypeDescriptor {

		public GenericTypeDescriptor(TypeDescriptor typeDescriptor) {
			super(typeDescriptor.getResolvableType().getGeneric(0), null, typeDescriptor.getAnnotations());
		}
	}

}
