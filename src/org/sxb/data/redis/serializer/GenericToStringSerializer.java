/*
 * Copyright 2011-2013 the original author or authors.
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
package org.sxb.data.redis.serializer;

import java.nio.charset.Charset;

import org.sxb.bean.BeanNameAware;

import org.sxb.convert.ConversionService;
import org.sxb.convert.support.DefaultConversionService;
import org.sxb.data.redis.serializer.bean.TypeConverter;
import org.sxb.kit.Assert;

/**
 * Generic String to byte[] (and back) serializer. Relies on the Spring {@link ConversionService} to transform objects
 * into String and vice versa. The Strings are convert into bytes and vice-versa using the specified charset (by default
 * UTF-8). <b>Note:</b> The conversion service initialization happens automatically if the class is defined as a Spring
 * bean. <b>Note:</b> Does not handle nulls in any special way delegating everything to the container.
 * 
 * @author Costin Leau
 */
public class GenericToStringSerializer<T> implements RedisSerializer<T>, BeanNameAware {

	private final Charset charset;
	private Converter converter = new Converter(new DefaultConversionService());
	private Class<T> type;

	public GenericToStringSerializer(Class<T> type) {
		this(type, Charset.forName("UTF8"));
	}

	public GenericToStringSerializer(Class<T> type, Charset charset) {
		Assert.notNull(type);
		this.type = type;
		this.charset = charset;
	}

	public void setConversionService(ConversionService conversionService) {
		Assert.notNull(conversionService, "non null conversion service required");
		converter = new Converter(conversionService);
	}

	public void setTypeConverter(TypeConverter typeConverter) {
		Assert.notNull(typeConverter, "non null type converter required");
		converter = new Converter(typeConverter);
	}

	public T deserialize(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		String string = new String(bytes, charset);
		return converter.convert(string, type);
	}

	public byte[] serialize(T object) {
		if (object == null) {
			return null;
		}
		String string = converter.convert(object, String.class);
		return string.getBytes(charset);
	}
/*
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (converter == null && beanFactory instanceof ConfigurableBeanFactory) {
			ConfigurableBeanFactory cFB = (ConfigurableBeanFactory) beanFactory;
			ConversionService conversionService = cFB.getConversionService();

			converter = (conversionService != null ? new Converter(conversionService) : new Converter(cFB.getTypeConverter()));
		}
	}
*/
	private class Converter {
		private final ConversionService conversionService;
		private final TypeConverter typeConverter;

		public Converter(ConversionService conversionService) {
			this.conversionService = conversionService;
			this.typeConverter = null;
		}

		public Converter(TypeConverter typeConverter) {
			this.conversionService = null;
			this.typeConverter = typeConverter;
		}

		<E> E convert(Object value, Class<E> targetType) {
			if (conversionService != null) {
				return conversionService.convert(value, targetType);
			}
			return typeConverter.convertIfNecessary(value, targetType);
		}
	}
	@Override
	public void setBeanName(String name) {
		
	}
}
