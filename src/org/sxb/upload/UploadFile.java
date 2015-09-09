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

/**
 * UploadFile.
 * 上传文件类
 * 此处sxb并未采用传统的apache fileupload组件，原因是cos上传组件，比其更快更容易使用
 * 一改往常原理，sxb一旦在controller一旦调用getFile， getFiles 函数则会根据开发者事前设定的命名规则，
 * 自动上传，然后返回文件的信息
 */
public class UploadFile {
	// form表单 input name
	private String parameterName;
	
	// 文件保持路径
	private String saveDirectory;
	
	// 文件名
	private String fileName;
	
	// 原始文件名
	private String originalFileName;
	
	// 是否是允许上传文件
	private boolean isAllow;
	
	// 文件类型 
	private String contentType;
	
	public UploadFile(String parameterName, String saveDirectory, String filesystemName, String originalFileName, String contentType) {
		this.parameterName = parameterName;
		this.saveDirectory = saveDirectory;
		this.fileName = filesystemName;
		this.originalFileName = originalFileName;
		this.contentType = contentType;
	}
	
	public String getParameterName() {
		return parameterName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String newName){
		this.fileName = newName;
	}
	
	public String getOriginalFileName() {
		return originalFileName;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getSaveDirectory() {
		return saveDirectory;
	}
	
	public void setSaveDirectory(String newDir){
		this.saveDirectory = newDir;
	}
	
	public File getFile() {
		if (saveDirectory == null || fileName == null) {
			return null;
		} else {
			return new File(saveDirectory + File.separator + fileName);
		}
	}

	public boolean isAllow() {
		return isAllow;
	}

	public void setAllow(boolean isAllow) {
		this.isAllow = isAllow;
	}
}






