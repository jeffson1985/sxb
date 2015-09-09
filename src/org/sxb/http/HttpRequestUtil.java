package org.sxb.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import javax.servlet.http.HttpServletRequest;

import org.sxb.http.server.ServletServerHttpRequest;

/**
 * HttpServletRequest包装类<br>
 * 实现不同类型间的自动转换
 * @author Jeffson
 *
 */

public class HttpRequestUtil {
	
public static  HttpInputMessage readWithMessageConverters(HttpServletRequest request) throws IOException {
	
		HttpInputMessage inputMessage = new ServletServerHttpRequest(request);

		InputStream inputStream = inputMessage.getBody();
		if(inputStream == null){
			return null;
		}
		else if (inputStream.markSupported()) {
			inputStream.mark(1);
			if (inputStream.read() == -1) {
				return null;
			}
			inputStream.reset();
		}
		else {
			final PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
			int b = pushbackInputStream.read();
			if (b == -1) {
				return null;
			}
			else {
				pushbackInputStream.unread(b);
			}
			inputMessage = new ServletServerHttpRequest(request) {
				@Override
				public InputStream getBody() {
					// Form POST should not get here
					return pushbackInputStream;
				}
			};
		}
		return  inputMessage;

	}
}
