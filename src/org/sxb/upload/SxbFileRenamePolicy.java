package org.sxb.upload;

import java.io.File;
import java.io.IOException;

import org.sxb.core.Sxb;
import org.sxb.kit.PathKit;
import org.sxb.kit.StrKit;

import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * Sxb 文件上传重命名接口 为了提供给那些有特别需求的企业而设 如果不定义自己的规则，那么sxb将利用cos的默认规则，
 * 利用文件原名保存，如果存在就文件则在后边加数字来防止覆盖 如没有特别需求，只需要利用UploadFile.getFile().renameTo(File
 * newFile)即可
 * 
 * @author Sun
 *
 */
public abstract class SxbFileRenamePolicy implements FileRenamePolicy {

	// 是否删除原有文件，也就是是否允许覆盖
	private boolean isDeleteOldFile = false;

	protected SxbFileRenamePolicy(boolean isDeleteOldFile) {
		this.isDeleteOldFile = isDeleteOldFile;
	}

	public File rename(File f) {
		// 获取webRoot目录
		String webRoot = PathKit.getWebRootPath();
		// 用户设置的默认上传目录
		String saveDir = Sxb.me().getConstants().getUploadedFileSaveDirectory();
		//
		StringBuilder newFileName = new StringBuilder(webRoot)
				.append(File.separator)
				.append(StrKit.isBlank(saveDir) ? "upload" : saveDir)
				.append(File.separator).append(createFileName())
				.append(getFileExt(f.getName()));

		f = new File(newFileName.toString());
		// 创建上层目录
		File dir = f.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// 如果存在同名文件，并且用户希望删除，则执行删除操作
		if (isDeleteOldFile && f.exists()) {
			f.delete();
		}
		// 如果存在同名文件，但是不允许删除就文件，则重命名文件
		// 命名规则在原名字后面加数字
		if (!isDeleteOldFile && f.exists()) {
			f = autoCreateFileName(f);
		}
		return f;
	}

	/**
	 * 获取文件后缀
	 * 
	 * @param fileName
	 * 			设定文件
	 * @return String 返回类型
	 */
	public static String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.'), fileName.length());
	}

	/**
	 * 文件重命名规则 抽象方法，由用户复制定制重命名规则 ＊＊ 在此处请定制文件名字，后缀将有系统自行添加
	 * 例如以下规则，添加日期文件夹／然后重命名文件为时间戳 /xxx/sxb-demo/upload/20150710/1436542837568 //
	 * 添加时间作为目录 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	 * .append(File.separator).append(dateFormat.format(new Date()))
	 * .append(File.separator).append(System.currentTimeMillis())
	 * 
	 * @return
	 */
	protected abstract String createFileName();

	public boolean isDeleteOldFile() {
		return isDeleteOldFile;
	}

	public void setDeleteOldFile(boolean isDeleteOldFile) {
		this.isDeleteOldFile = isDeleteOldFile;
	}

	/**
	 * 如果设定不可以重命名，则会在用户规定规则名字的后面追加数字
	 * @param f
	 * @return
	 */
	private File autoCreateFileName(File f) {
		String name = f.getName();
		String body = null;
		String ext = null;

		int dot = name.lastIndexOf(".");
		if (dot != -1) {
			body = name.substring(0, dot);
			ext = name.substring(dot); // includes "."
		} else {
			body = name;
			ext = "";
		}
		// Increase the count until an empty spot is found.
		// Max out at 9999 to avoid an infinite loop caused by a persistent
		// IOException, like when the destination dir becomes non-writable.
		// We don't pass the exception up because our job is just to rename,
		// and the caller will hit any IOException in normal processing.
		int count = 0;
		while (!createNewFile(f) && count < 9999) {
			count++;
			String newName = body + count + ext;
			f = new File(f.getParent(), newName);
		}
		return f;
	}

	/**
	 * 判断是否存在，并建立新文件
	 * @param f
	 * @return
	 */
	private boolean createNewFile(File f) {
		try {
			return f.createNewFile();
		} catch (IOException ignored) {
			return false;
		}
	}

}
