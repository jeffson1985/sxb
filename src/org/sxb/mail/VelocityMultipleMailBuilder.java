package org.sxb.mail;

import java.io.File;

import org.apache.velocity.VelocityContext;

/**
 * Velocityと連携して動的にメールデータを生成し、そのデータからMailインスタンスを生成するインターフェース。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: VelocityMultipleMailBuilder.java,v 1.2
 */
public interface VelocityMultipleMailBuilder extends VelocityMailBuilder {

	/**
	 * 指定されたクラスパス上のファイルを読み込み、mailIdが示すデータからMailインスタンスを生成します。
	 * 指定されたVelocityContextを使って、XMLファイルの内容を動的に生成できます。
	 * 
	 * @param classPath メール内容を記述したファイルのパス
	 * @param context VelocityContext
	 * @param mailId 生成するMailのメールデータを示すID
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(String classPath, VelocityContext context, String mailId)
																			throws MailBuildException;

	/**
	 * 指定されたファイルを読み込み、mailIdが示すデータからMailインスタンスを生成します。
	 * 指定されたVelocityContextを使って、XMLファイルの内容を動的に生成できます。
	 * 
	 * @param file メール内容を記述したファイル
	 * @param context VelocityContext
	 * @param mailId 生成するMailのメールデータを示すID
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(File file, VelocityContext context, String mailId) throws MailBuildException;

}