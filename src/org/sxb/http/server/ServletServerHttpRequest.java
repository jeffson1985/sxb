/*
 * Copyright 2002-2014 the original author or authors.
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

package org.sxb.http.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sxb.http.HttpHeaders;
import org.sxb.http.HttpMethod;
import org.sxb.http.MediaType;
import org.sxb.kit.Assert;
import org.sxb.kit.LinkedCaseInsensitiveMap;
import org.sxb.kit.StringKits;




/**
 * {@link ServerHttpRequest} implementation that is based on a {@link HttpServletRequest}.
 *
 * @author Jeffson
 * @since 2.0
 */
public class ServletServerHttpRequest implements ServerHttpRequest {

	protected static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

	protected static final String FORM_CHARSET = "UTF-8";

	private static final String METHOD_POST = "POST";


	private final HttpServletRequest servletRequest;

	private HttpHeaders headers;

	private ServerHttpAsyncRequestControl asyncRequestControl;


	/**
	 * Construct a new instance of the ServletServerHttpRequest based on the given {@link HttpServletRequest}.
	 * @param servletRequest the servlet request
	 */
	public ServletServerHttpRequest(HttpServletRequest servletRequest) {
		Assert.notNull(servletRequest, "HttpServletRequest must not be null");
		this.servletRequest = servletRequest;
	}


	/**
	 * Returns the {@code HttpServletRequest} this object is based on.
	 */
	public HttpServletRequest getServletRequest() {
		return this.servletRequest;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.valueOf(this.servletRequest.getMethod());
	}

	@Override
	public URI getURI() {
		try {
			return new URI(this.servletRequest.getScheme(), null, this.servletRequest.getServerName(),
					this.servletRequest.getServerPort(), this.servletRequest.getRequestURI(),
					this.servletRequest.getQueryString(), null);
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException("Could not get HttpServletRequest URI: " + ex.getMessage(), ex);
		}
	}

	@Override
	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			for (Enumeration<?> headerNames = this.servletRequest.getHeaderNames(); headerNames.hasMoreElements();) {
				String headerName = (String) headerNames.nextElement();
				for (Enumeration<?> headerValues = this.servletRequest.getHeaders(headerName);
						headerValues.hasMoreElements();) {
					String headerValue = (String) headerValues.nextElement();
					this.headers.add(headerName, headerValue);
				}
			}
			// HttpServletRequest exposes some headers as properties: we should include those if not already present
			MediaType contentType = this.headers.getContentType();
			if (contentType == null) {
				String requestContentType = this.servletRequest.getContentType();
				if (StringKits.hasLength(requestContentType)) {
					contentType = MediaType.parseMediaType(requestContentType);
					this.headers.setContentType(contentType);
				}
			}
			if (contentType != null && contentType.getCharSet() == null) {
				String requestEncoding = this.servletRequest.getCharacterEncoding();
				if (StringKits.hasLength(requestEncoding)) {
					Charset charSet = Charset.forName(requestEncoding);
					Map<String, String> params = new LinkedCaseInsensitiveMap<String>();
					params.putAll(contentType.getParameters());
					params.put("charset", charSet.toString());
					MediaType newContentType = new MediaType(contentType.getType(), contentType.getSubtype(), params);
					this.headers.setContentType(newContentType);
				}
			}
			if (this.headers.getContentLength() == -1) {
				int requestContentLength = this.servletRequest.getContentLength();
				if (requestContentLength != -1) {
					this.headers.setContentLength(requestContentLength);
				}
			}
		}
		return this.headers;
	}

	@Override
	public Principal getPrincipal() {
		return this.servletRequest.getUserPrincipal();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return new InetSocketAddress(this.servletRequest.getLocalName(), this.servletRequest.getLocalPort());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return new InetSocketAddress(this.servletRequest.getRemoteHost(), this.servletRequest.getRemotePort());
	}

	@Override
	public InputStream getBody() throws IOException {
		//System.out.println("Jeffson->ContentType" + servletRequest.getContentType());
		if (isFormPost(this.servletRequest)) {
		
			return getBodyFromServletRequestParameters(this.servletRequest);
		}
		else {
			return this.servletRequest.getInputStream();
		}
	}

	private boolean isFormPost(HttpServletRequest request) {
		return (request.getContentType() != null && request.getContentType().contains(FORM_CONTENT_TYPE) &&
				METHOD_POST.equalsIgnoreCase(request.getMethod()));
	}

	/**
	 * Use {@link javax.servlet.ServletRequest#getParameterMap()} to reconstruct the
	 * body of a form 'POST' providing a predictable outcome as opposed to reading
	 * from the body, which can fail if any other code has used ServletRequest
	 * to access a parameter thus causing the input stream to be "consumed".
	 */
	private InputStream getBodyFromServletRequestParameters(HttpServletRequest request) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		Writer writer = new OutputStreamWriter(bos, FORM_CHARSET);

		Map<String, String[]> form = request.getParameterMap();
		for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext();) {
			String name = nameIterator.next();
			//System.out.println("name: " + name);
			List<String> values = Arrays.asList(form.get(name));
			for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext();) {
				String value = valueIterator.next();
				//System.out.println(value);
				writer.write(URLEncoder.encode(name, FORM_CHARSET));
				if (value != null) {
					writer.write('=');
					writer.write(URLEncoder.encode(value, FORM_CHARSET));
					if (valueIterator.hasNext()) {
						writer.write('&');
					}
				}
			}
			if (nameIterator.hasNext()) {
				writer.append('&');
			}
		}
		writer.flush();

		return new ByteArrayInputStream(bos.toByteArray());
	}

	@Override
	public ServerHttpAsyncRequestControl getAsyncRequestControl(ServerHttpResponse response) {
		if (this.asyncRequestControl == null) {
			Assert.isInstanceOf(ServletServerHttpResponse.class, response);
			ServletServerHttpResponse servletServerResponse = (ServletServerHttpResponse) response;
			this.asyncRequestControl = new ServletServerHttpAsyncRequestControl(this, servletServerResponse);
		}
		return this.asyncRequestControl;
	}

}
