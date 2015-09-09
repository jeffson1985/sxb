/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
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

package org.sxb.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.sxb.common.Collections;
import org.sxb.kit.StringKits;

import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * MultipartRequest.
 * 上传文件请求类
 * 重新包装HttpServletRequest
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MultipartRequest extends HttpServletRequestWrapper {
	
	private static String saveDirectory;
	private static int maxPostSize;
	private static String encoding;
	private static Set<String> allowUploadFileType = Collections.set("jpg", "jpeg", "gif","png","bmp","csv", "pdf");
	static FileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy();
	
	private List<UploadFile> uploadFiles;
	private com.oreilly.servlet.MultipartRequest multipartRequest;
	
	static void init(String saveDirectory, int maxPostSize, String encoding) {
		MultipartRequest.saveDirectory = saveDirectory;
		MultipartRequest.maxPostSize = maxPostSize;
		MultipartRequest.encoding = encoding;
	}
	
	public MultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize, String encoding) {
		super(request);
		wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
	}
	
	public MultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize) {
		super(request);
		wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
	}
	
	public MultipartRequest(HttpServletRequest request, String saveDirectory) {
		super(request);
		wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
	}
	
	public MultipartRequest(HttpServletRequest request) {
		super(request);
		
		wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
	}
	
	/**
	 * 添加对相对路径的支持
	 * 1: 以 "/" 开头或者以 "x:开头的目录被认为是绝对路径
	 * 2: 其它路径被认为是相对路径, 需要 SxbConfig.uploadedFileSaveDirectory 结合
	 */
	private String handleSaveDirectory(String saveDirectory) {
		if (saveDirectory.startsWith("/") || saveDirectory.indexOf(":") == 1)
			return saveDirectory;
		else 
			return MultipartRequest.saveDirectory + saveDirectory;
	}
	
	private void wrapMultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize, String encoding) {
		saveDirectory = handleSaveDirectory(saveDirectory);
		
		File dir = new File(saveDirectory);
		if ( !dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Directory " + saveDirectory + " not exists and can not create directory.");
			}
		}
		
//		String content_type = request.getContentType();
//        if (content_type == null || content_type.indexOf("multipart/form-data") == -1) {
//        	throw new RuntimeException("Not multipart request, enctype=\"multipart/form-data\" is not found of form.");
//        }
		
        uploadFiles = new ArrayList<UploadFile>();
		
		try {
			// common file upload 跟RelliyCos并存测试
			//HttpServletRequest nativeRequest = (HttpServletRequest)((MultipartHttpServletRequest)request).getAsyncContext().getRequest();
			multipartRequest = new  com.oreilly.servlet.MultipartRequest(request, saveDirectory, maxPostSize, encoding, fileRenamePolicy);
			Enumeration files = multipartRequest.getFileNames();
			while (files.hasMoreElements()) {
				String name = (String)files.nextElement();
				String filesystemName = multipartRequest.getFilesystemName(name);
				// 文件没有上传则不生成 UploadFile, 这与 cos的解决方案不一样
				if (filesystemName != null) {
					String originalFileName = multipartRequest.getOriginalFileName(name);
					
					String contentType = multipartRequest.getContentType(name);
					UploadFile uploadFile = new UploadFile(name, saveDirectory, filesystemName, originalFileName, contentType);
					if (isSafeFile(uploadFile)){
						isAllowUploadFile(uploadFile);
						uploadFiles.add(uploadFile);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	// 判断文件是否安全
	private boolean isSafeFile(UploadFile uploadFile) {
		String fileName = uploadFile.getFileName().trim().toLowerCase();
		if (fileName.endsWith(".jsp") || fileName.endsWith(".jspx")) {
			uploadFile.getFile().delete();
			return false;
		}
		return true;
	}
	
	// 判断是否为用户指定允许上传文件
	private boolean isAllowUploadFile(UploadFile uploadFile){
		String fileName = uploadFile.getFileName().trim().toLowerCase();
		String extName = StringKits.getFilenameExtension(fileName);
		
		if(allowUploadFileType.contains(extName)){
			uploadFile.setAllow(true);
			return true;
		}else{
			uploadFile.getFile().delete();
			uploadFile.setAllow(false);
			return false;
		}
	}
	
	
	
	public List<UploadFile> getFiles() {
		return uploadFiles;
	}
	
	/**
	 * Methods to replace HttpServletRequest methods
	 */
	public Enumeration getParameterNames() {
		return multipartRequest.getParameterNames();
	}
	
	public String getParameter(String name) {
		return multipartRequest.getParameter(name);
	}
	
	public String[] getParameterValues(String name) {
		return multipartRequest.getParameterValues(name);
	}
	
	public Map getParameterMap() {
		Map map = new HashMap();
		Enumeration enumm = getParameterNames();
		while (enumm.hasMoreElements()) {
			String name = (String) enumm.nextElement();
			map.put(name, multipartRequest.getParameterValues(name));
		}
		return map;
	}
	// 设定上传文件命名规则
	public static void setFileRenamePolicy(FileRenamePolicy fileRenamePolicy){
		MultipartRequest.fileRenamePolicy = fileRenamePolicy;
	}
	
	// 添加允许上传文件类型
	public static void setAllowUploadFileType(Set<String> suffixes){
		allowUploadFileType.addAll(suffixes);
	}
	
	public static void addAllowUploadFileType(String ... suffixes){
		for(String str: suffixes){
			allowUploadFileType.add(str);
		}
	}
}






