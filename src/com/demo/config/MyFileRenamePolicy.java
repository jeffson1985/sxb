package com.demo.config;

import org.sxb.upload.SxbFileRenamePolicy;

public class MyFileRenamePolicy extends SxbFileRenamePolicy {

	protected MyFileRenamePolicy(boolean isDeleteOldFile) {
		super(isDeleteOldFile);
	}

	@Override
	protected String createFileName() {
		StringBuilder name = new StringBuilder();		
		// 添加命名规则
		name.append("rakutabi_")
		.append(System.currentTimeMillis());
		return name.toString();
	}

}
