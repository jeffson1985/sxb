/*
 * @(#) $Id: PartHandler.java,v 1.1.2.1 2004/09/29 00:57:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;
import javax.mail.Part;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;

/**
 * PartHandlerです。
 * <p>
 * MultipartUtility#process()に渡すことで、Message内の各Partオブジェクト
 * に対してprocessPartが呼び出されます。<BR>
 * 特定のMIMEタイプに対してのみ処理を行う場合などに有効です。
 * </p><p>
 * 使用方法としては、実装クラス上に各パートの処理結果を蓄積していき、
 * MultipartUtility#process()メソッド復帰後にそのオブジェクトから最終結果を
 * 取り出すような形式が考えられます。
 * </p>
 * @version $Revision: 1.1.2.1 $ $Date: 2004/09/29 00:57:59 $
 * @author Shin
 */
public interface PartHandler {

	/**
	 * パートに対して処理を行います。
	 * <P>
	 * contextにはそのパートがmultipart/*の子パートである場合に、
	 * そのmultipart/*のMIMEタイプが渡されます。<BR>
	 * 続けて次のパートを処理するか否かを復帰値で返してください。
	 * </P><P>
	 * message/rfc822パートの内部も走査したい場合は、実装クラス内で
	 * 以下のようにコーディングしてください。
	 * </P>
	 * <PRE>if (part.isMimeType("message/rfc822")) {
	 *     // message/rfc822パートの処理オブジェクト
	 *     AnyHandler h = new AnyHandler();
	 *     MultipartUtility.process(part, h);
	 * }
	 * </PRE>
	 * @return true:次のパート、或いは内包メッセージ内部も処理する
	 */
	boolean processPart(Part part, ContentType context) throws MessagingException, IOException;
}