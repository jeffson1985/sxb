/**
 * Sxb邮件服务类
 * {@code org.sxb.mail.impl.SendMailImpl}  普通送信类<br>
 * {@code org.sxb.mail.impl.SendMailProImpl} 专业送信类   加强用户操作<br>
 * {@package org.sxb.mail.fetch}   邮件接收包<br>
 * {@code org.sxb.mail.fetch.impl.FetchMailImpl} 收信类  一次全部接收<br>
 * {@code org.sxb.mail.fetch.impl.FetchMailProImpl} 专业收信类   加强用户操作  如因邮件太多占用内存太大时，可以逐个接收<br>
 *
 * {@package org.sxb.mail.mailet}  邮件信使小助手 利用此类必须实现此包下的{@code Mailet  Matcher}两个接口<br>
 * 符合条件的邮件将被处理 例如 接收，保存等<br>
 *
 * 要使用此服务必须加入一下包<br>
 * java  mail.jar   version 1.4.7<br>
 *       activation.jar
 */
package org.sxb.mail;

/*
 * 使用方法
public static void getMail(){
	
	FetchMailProImpl fmi = new FetchMailProImpl();
	fmi.setHost("cetvision.com");
	fmi.setUsername("sun@cetvision.com");
	fmi.setPassword("jeffson8203");
	fmi.setProtocol("pop3");
	fmi.setPort(110);
	fmi.connect();
		System.out.println(fmi.getMail(74).getText());
	
	fmi.disconnect();
	//ReceivedMail[] mails = fmi.getMails(false);
	//for(ReceivedMail mail: mails){
	//System.out.println(mail.getSubject());
	//}
}

public static void sendMail() {
	

	Mail mail = new Mail();
	mail.setFrom("sun@cetvision.com");
	// mail.setReplyTo("sun@cetvision.com");
	mail.setSubject("Test form jeffson");
	mail.setText("Test by son with sxb.mail");
	mail.addTo("kevionsun@gmail.com");

	SendMailImpl mailer = new SendMailImpl("smtp.web-db.ws");
	mailer.setHost("smtp.web-db.ws");
	mailer.setProtocol("smtp");
	mailer.setPort(587);
	mailer.setUsername("son@hope123.co.jp");
	mailer.setPassword("TsJuttOC");
	mailer.send(mail);
}

*/

