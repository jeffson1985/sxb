package org.sxb.mail.support;

import java.util.Properties;

import org.sxb.mail.support.javamail.JavaMailSenderImpl;

public class TestMail {
	public static void main(String args[]){
		Properties properties = System.getProperties();
		properties.put("mail.smtp.starttls.enable", "true"); 
		properties.put("mail.smtp.host", "smtp.126.com");
		properties.put("mail.smtp.user", "jeffson1985@126.com"); // User name
		properties.put("mail.smtp.password", "nnakpcpwrljtdkwt"); // password
		//properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		
		JavaMailSenderImpl jms = new JavaMailSenderImpl();
		jms.setJavaMailProperties(properties);
		//jms.setHost("tls://smtp.gmail.com");
		jms.setUsername("jeffson1985@126.com");
		jms.setPassword("nnakpcpwrljtdkwt");
		//jms.setProtocol("imap");
		//jms.setPort(995);
		
		SimpleMailMessage msg = new SimpleMailMessage();
		
		msg.setTo("kevionsun@gmail.com");
		msg.setText("test by son spring");
		msg.setSubject("test by son");
		jms.send(msg);
	}

}
