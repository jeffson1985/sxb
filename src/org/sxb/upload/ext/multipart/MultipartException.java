/*
 * Copyright 2002-2012 the original author or authors.
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

package org.sxb.upload.ext.multipart;

import org.sxb.exception.NestedRuntimeException;
import org.sxb.render.Render;

/**
 * Exception thrown when multipart resolution fails.
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see MultipartResolver#resolveMultipart
 * @see org.springframework.web.multipart.support.MultipartFilter
 */
@SuppressWarnings("serial")
public class MultipartException extends NestedRuntimeException {

	/**
	 * Constructor for MultipartException.
	 * @param msg the detail message
	 */
	public MultipartException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for MultipartException.
	 * @param msg the detail message
	 * @param cause the root cause from the multipart parsing API in use
	 */
	public MultipartException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public MultipartException(int errorCode, Render errorRender) {
		super(errorCode, errorRender);
	}
	
	public MultipartException(int errorCode, String errorView) {
		super(errorCode, errorView);
	}
}
