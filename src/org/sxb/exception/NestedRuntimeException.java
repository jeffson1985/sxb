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

package org.sxb.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sxb.kit.StrKit;
import org.sxb.log.Logger;
import org.sxb.render.Render;
import org.sxb.render.RenderFactory;

/**
 * sxb 全体执行异常类
 * 强制程序员必须继承此类，方便sxb框架整体管理异常
 * 此类异常并不会停止服务器，因此可以在handler中拦截处理
 * 为了提示信息的友好统一错误输出
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * <p>This class is {@code abstract} to force the programmer to extend
 * the class. {@code getMessage} will include nested exception
 * information; {@code printStackTrace} and other like methods will
 * delegate to the wrapped exception, if any.
 *
 * <p>The similarity between this class and the {@link NestedCheckedException}
 * class is unavoidable, as Java forces these two classes to have different
 * superclasses (ah, the inflexibility of concrete inheritance!).
 *
 * @author Rod Johnson
 * @author  Jeffson
 * @see #getMessage
 * @see #printStackTrace
 * @see NestedCheckedException
 */
public abstract class NestedRuntimeException extends RuntimeException {

	/** Use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = 5439915454935047936L;
	private static final Logger log = Logger.getLogger(NestedRuntimeException.class);
	private int errorCode;
	private Render errorRender;

	static {
		// Eagerly load the NestedExceptionUtils class to avoid classloader deadlock
		// issues on OSGi when calling getMessage(). Reported by Don Brown; SPR-5607.
		NestedExceptionUtils.class.getName();
	}


	/**
	 * Construct a {@code NestedRuntimeException} with the specified detail message.
	 * @param msg the detail message
	 */
	public NestedRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Construct a {@code NestedRuntimeException} with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
	public NestedRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 *  Construct a {@code NestedRuntimeException} with the specified error code and error render.
	 * @param errorCode
	 * @param errorRender
	 */
	public NestedRuntimeException(final int errorCode, final Render errorRender) {
		if (errorRender == null)
			throw new IllegalArgumentException("The parameter errorRender can not be null.");
		
		this.errorCode = errorCode;
		
		if (errorRender instanceof org.sxb.render.ErrorRender) {
			this.errorRender = errorRender;
		}
		else {
			this.errorRender = new Render() {
				public Render setContext(HttpServletRequest req, HttpServletResponse res, String viewPath) {
					errorRender.setContext(req, res, viewPath);
					res.setStatus(errorCode);	// important
					return this;
				}
				
				public void render() {
					errorRender.render();
				}
			};
		}
	}
	/**
	 *  Construct a {@code NestedRuntimeException} with the specified error code and error view name.
	 * @param errorCode
	 * @param errorView
	 */
	public NestedRuntimeException(int errorCode, String errorView) {
		if (StrKit.isBlank(errorView))
			throw new IllegalArgumentException("The parameter errorView can not be blank.");
		
		this.errorCode = errorCode;
		this.errorRender = RenderFactory.me().getErrorRender(errorCode, errorView);
	}
	
	/**
	 *  Construct a {@code NestedRuntimeException} with the specified error code and error render and detail message.
	 * @param errorCode
	 * @param errorRender
	 * @param errorMessage
	 */
	public NestedRuntimeException(int errorCode, Render errorRender, String errorMessage) {
		this(errorCode, errorRender);
		log.warn(errorMessage);
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public Render getErrorRender() {
		return errorRender;
	}


	/**
	 * Return the detail message, including the message from the nested exception
	 * if there is one.
	 */
	@Override
	public String getMessage() {
		return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
	}


	/**
	 * Retrieve the innermost cause of this exception, if any.
	 * @return the innermost exception, or {@code null} if none
	 * @since 2.0
	 */
	public Throwable getRootCause() {
		Throwable rootCause = null;
		Throwable cause = getCause();
		while (cause != null && cause != rootCause) {
			rootCause = cause;
			cause = cause.getCause();
		}
		return rootCause;
	}

	/**
	 * Retrieve the most specific cause of this exception, that is,
	 * either the innermost cause (root cause) or this exception itself.
	 * <p>Differs from {@link #getRootCause()} in that it falls back
	 * to the present exception if there is no root cause.
	 * @return the most specific cause (never {@code null})
	 * @since 2.0.3
	 */
	public Throwable getMostSpecificCause() {
		Throwable rootCause = getRootCause();
		return (rootCause != null ? rootCause : this);
	}

	/**
	 * Check whether this exception contains an exception of the given type:
	 * either it is of the given class itself or it contains a nested cause
	 * of the given type.
	 * @param exType the exception type to look for
	 * @return whether there is a nested exception of the specified type
	 */
	public boolean contains(Class<?> exType) {
		if (exType == null) {
			return false;
		}
		if (exType.isInstance(this)) {
			return true;
		}
		Throwable cause = getCause();
		if (cause == this) {
			return false;
		}
		if (cause instanceof NestedRuntimeException) {
			return ((NestedRuntimeException) cause).contains(exType);
		}
		else {
			while (cause != null) {
				if (exType.isInstance(cause)) {
					return true;
				}
				if (cause.getCause() == cause) {
					break;
				}
				cause = cause.getCause();
			}
			return false;
		}
	}

}
