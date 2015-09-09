package org.sxb.mail;

import java.io.File;

import org.apache.velocity.VelocityContext;

/**
 * Velocityと連携して動的にメールデータを生成し、そのデータからMailインスタンスを生成するインターフェース。
 * 
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: VelocityMailBuilder.java,v 1.2
 */
public interface VelocityMailBuilder extends MailBuilder {

	/**
	 * 指定されたクラスパス上のファイルを読み込んでMailインスタンスを生成します。
	 * 指定されたVelocityContextを使って、XMLファイルの内容を動的に生成できます。
	 * 
	 * @param classPath メール内容を記述したファイルのパス
	 * @param context VelocityContext
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(String classPath, VelocityContext context) throws MailBuildException;

	/**
	 * 指定されたファイルを読み込んでMailインスタンスを生成します。
	 * 指定されたVelocityContextを使って、XMLファイルの内容を動的に生成できます。
	 * 
	 * @param file メール内容を記述したファイル
	 * @param context VelocityContext
	 * @return 生成されたMailインスタンス
	 * @throws MailBuildException Mailインスタンスの生成に失敗した場合
	 */
	Mail buildMail(File file, VelocityContext context) throws MailBuildException;

	/**
	 * メールデータキャッシュをクリアします。
	 * 
	 * @since 1.1.2
	 */
	void clearCache();

	/**
	 * VelocityContextとマージする前のメールデータをキャッシュするかどうかを設定します。
	 * デフォルトはキャッシュしない設定です。
	 * <p>
	 * キャッシュのキーは、<code>buildMail()</code>メソッド引数のメールデータファイルのクラスパス或いはファイルパスです。
	 * キャッシュに有効期限はありません。
	 * また、メールデータファイルの内容が途中で更新されても、キャッシュされているメールデータは更新されませんので注意してください。
	 * <p>
	 * <code>false</code>を指定してこのメソッドを呼ぶとメールデータキャッシュはクリアされます。
	 * 
	 * @since 1.1.2
	 * @param cacheEnabled メールデータをキャッシュする場合は true
	 */
	void setCacheEnabled(boolean cacheEnabled);

	/**
	 * VelocityContextとマージする前のメールデータをキャッシュする設定かどうか判定します。
	 * 
	 * @since 1.1.2
	 * @return メールデータをキャッシュする設定の場合は true
	 */
	boolean isCacheEnabled();

}