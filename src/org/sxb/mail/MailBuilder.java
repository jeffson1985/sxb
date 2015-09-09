package org.sxb.mail;

import java.io.File;

/**
 * メールデータが記述されたファイルからMailインスタンスを生成するインスターフェース。
 * サポートするファイルの種類やメールデータ書式は実装クラスに依存します。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailBuilder.java,v 1.0
 */
public interface MailBuilder {

	/**
	 * 指定されたクラスパス上のファイルを読み込んでMailインスタンスを生成します。
	 * 
	 * @param classPath メール内容を記述したファイルのパス
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(String classPath) throws MailBuildException;

	/**
	 * 指定されたファイルを読み込んでMailインスタンスを生成します。
	 * 
	 * @param file メール内容を記述したファイル
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(File file) throws MailBuildException;

}