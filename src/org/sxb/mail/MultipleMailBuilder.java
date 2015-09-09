package org.sxb.mail;

import java.io.File;

/**
 * 複数のメールデータが記述されたファイルからMailインスタンスを生成するインスターフェース。
 * 
 * @since 1.0
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MultipleMailBuilder.java,v 1.0.1
 */
public interface MultipleMailBuilder extends MailBuilder {

	/**
	 * 指定されたクラスパス上のファイルを読み込み、mailIdが示すデータからMailインスタンスを生成します。
	 * 
	 * @param classPath メール内容を記述したファイルのパス
	 * @param mailId 生成するMailのメールデータを示すID
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(String classPath, String mailId) throws MailBuildException;

	/**
	 * 指定されたファイルを読み込み、mailIdが示すデータからMailインスタンスを生成します。
	 * 
	 * @param file メール内容を記述したファイル
	 * @param mailId 生成するMailのメールデータを示すID
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(File file, String mailId) throws MailBuildException;

}