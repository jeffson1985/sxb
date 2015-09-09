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

package org.sxb.http.converter;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.sxb.http.HttpInputMessage;
import org.sxb.http.HttpOutputMessage;
import org.sxb.http.MediaType;
import org.sxb.kit.ClassKits;
import org.sxb.kit.StreamKits;
import org.sxb.kit.StringKits;
import org.sxb.kit.io.ByteArrayResource;
import org.sxb.kit.io.ClassPathResource;
import org.sxb.kit.io.InputStreamResource;
import org.sxb.kit.io.Resource;


/**
 * Implementation of {@link HttpMessageConverter} that can read and write {@link Resource Resources}.
 *
 * <p>By default, this converter can read all media types. The Java Activation Framework (JAF) -
 * if available - is used to determine the {@code Content-Type} of written resources.
 * If JAF is not available, {@code application/octet-stream} is used.
 *
 * @author Jeffson
 * @since 2.0
 */
public class ResourceHttpMessageConverter extends AbstractHttpMessageConverter<Resource> {

	private static final boolean jafPresent = ClassKits.isPresent(
			"javax.activation.FileTypeMap", ResourceHttpMessageConverter.class.getClassLoader());


	public ResourceHttpMessageConverter() {
		super(MediaType.ALL);
	}


	@Override
	protected boolean supports(Class<?> clazz) {
		return Resource.class.isAssignableFrom(clazz);
	}

	@Override
	protected Resource readInternal(Class<? extends Resource> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		byte[] body = StreamKits.copyToByteArray(inputMessage.getBody());
		return new ByteArrayResource(body);
	}

	@Override
	protected MediaType getDefaultContentType(Resource resource) {
		if (jafPresent) {
			return ActivationMediaTypeFactory.getMediaType(resource);
		}
		else {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	@Override
	protected Long getContentLength(Resource resource, MediaType contentType) throws IOException {
		// Don't try to determine contentLength on InputStreamResource - cannot be read afterwards...
		// Note: custom InputStreamResource subclasses could provide a pre-calculated content length!
		return (InputStreamResource.class.equals(resource.getClass()) ? null : resource.contentLength());
	}

	@Override
	protected void writeInternal(Resource resource, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		InputStream in = resource.getInputStream();
		try {
			StreamKits.copy(in, outputMessage.getBody());
		}
		finally {
			try {
				in.close();
			}
			catch (IOException ex) {
			}
		}
		outputMessage.getBody().flush();
	}


	/**
	 * Inner class to avoid a hard-coded JAF dependency.
	 */
	private static class ActivationMediaTypeFactory {

		private static final FileTypeMap fileTypeMap;

		static {
			fileTypeMap = loadFileTypeMapFromContextSupportModule();
		}

		private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
			// See if we can find the extended mime.types from the context-support module...
			Resource mappingLocation = new ClassPathResource("org/sxb/web/mail/javamail/mime.types");
			if (mappingLocation.exists()) {
				InputStream inputStream = null;
				try {
					inputStream = mappingLocation.getInputStream();
					return new MimetypesFileTypeMap(inputStream);
				}
				catch (IOException ex) {
					// ignore
				}
				finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						}
						catch (IOException ex) {
							// ignore
						}
					}
				}
			}
			return FileTypeMap.getDefaultFileTypeMap();
		}

		public static MediaType getMediaType(Resource resource) {
			String filename = resource.getFilename();
			if (filename != null) {
				String mediaType = fileTypeMap.getContentType(filename);
				if (StringKits.hasText(mediaType)) {
					return MediaType.parseMediaType(mediaType);
				}
			}
			return null;
		}
	}

}
