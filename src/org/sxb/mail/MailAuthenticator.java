package org.sxb.mail;
/**
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * 
 * @version $Id: MailAuthenticator.java,v 2.0
 */
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MailAuthenticator extends Authenticator {
	private String username;
	private String password;
	
	public MailAuthenticator(final String username, final String password){
		this.username = username;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		
		return new PasswordAuthentication(username, password);
	}

}
