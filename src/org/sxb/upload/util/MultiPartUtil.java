package org.sxb.upload.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.common.Logger;

import org.sxb.core.Sxb;
import org.sxb.kit.WebKits;
import org.sxb.upload.ext.multipart.MaxUploadSizeExceededException;
import org.sxb.upload.ext.multipart.MultipartException;
import org.sxb.upload.ext.multipart.MultipartHttpServletRequest;
import org.sxb.upload.ext.multipart.MultipartResolver;
import org.sxb.upload.ext.multipart.commons.CommonsMultipartResolver;
/**
 * Common fileupload上传组件调用入口
 * 根据spring 上传原理，重新封装HttpServletRequest
 * 
 * @author Sun
 *
 */
public class MultiPartUtil {
	/** MultipartResolver used by this servlet */
	private static CommonsMultipartResolver multipartResolver;
	private static final Logger logger = Logger.getLogger(MultiPartUtil.class);

	static {
		multipartResolver = new CommonsMultipartResolver(Sxb.me()
				.getServletContext());
		multipartResolver.setDefaultEncoding("UTF-8");
		// 上传最大500k
		multipartResolver.setMaxUploadSize(500000);
		//multipartResolver.setResolveLazily(true);
	}

	public static HttpServletRequest wrapMultiPartRequest(
			HttpServletRequest request, HttpServletResponse response) {
		HttpServletRequest processedRequest = request;
		boolean multipartRequestParsed = false;

		try {

			processedRequest = checkMultipart(request);
			multipartRequestParsed = (processedRequest != request);

		} catch (MaxUploadSizeExceededException ex) {
			throw ex;
		} finally {

			if (multipartRequestParsed) {
				/*
				 * 将来需要追加次功能
				 * 为了全面支持restful功能
				 * 处于安全以及系统资源问题
				 * ajax的非同期请求时
				 * 用户的访问如果长期得不到响应则在规定的过时时间后
				 * 清除httServletprequest所有的内容尤其时multipartfile
				 * 请求
				 */
				// cleanupMultipart(processedRequest);
			}

		}

		return processedRequest;
	}

	/**
	 * Convert the request into a multipart request, and make multipart resolver
	 * available.
	 * <p>
	 * If no multipart resolver is set, simply use the existing request.
	 * 
	 * @param request
	 *            current HTTP request
	 * @return the processed request (multipart wrapper if necessary)
	 * @see MultipartResolver#resolveMultipart
	 */
	protected static HttpServletRequest checkMultipart(
			HttpServletRequest request) throws MultipartException {
		if (multipartResolver != null && multipartResolver.isMultipart(request)) {
			if (WebKits.getNativeRequest(request,
					MultipartHttpServletRequest.class) != null) {
				logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, "
						+ "this typically results from an additional MultipartFilter in web.xml");
			} else if (request.getAttribute(WebKits.ERROR_EXCEPTION_ATTRIBUTE) instanceof MultipartException) {
				logger.debug("Multipart resolution failed for current request before - "
						+ "skipping re-resolution for undisturbed error rendering");
			} else {
				return multipartResolver.resolveMultipart(request);
			}
		}
		// If not returned before: return original request.
		return request;
	}

	/**
	 * Clean up any resources used by the given multipart request (if any).
	 * 
	 * @param request
	 *            current HTTP request
	 * @see MultipartResolver#cleanupMultipart
	 */
	protected static void cleanupMultipart(HttpServletRequest request) {
		MultipartHttpServletRequest multipartRequest = WebKits
				.getNativeRequest(request, MultipartHttpServletRequest.class);
		if (multipartRequest != null) {
			multipartResolver.cleanupMultipart(multipartRequest);
		}
	}

}
