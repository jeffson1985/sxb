/*
 * Copyright 2013-2015 cetvision.com. All rights reserved.
 * Support: http://www.cetvision.com
 * License: http://www.cetvision.com/license
 */
package org.sxb.mail.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.sxb.core.task.ThreadPoolTaskExecutor;
import org.sxb.kit.Assert;
import org.sxb.mail.Mail;
import org.sxb.mail.impl.SendMailImpl;
import org.sxb.mail.support.javamail.JavaMailSenderImpl;
import org.sxb.mail.support.javamail.MimeMessageHelper;
import org.sxb.plugin.mail.MailPlugin;
import org.sxb.plugin.mail.MailSender;
import org.sxb.render.FreeMarkerRender;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Send mail service class<br>
 * Service - 邮件发送服务提供类<br>
 * Service - メール発送サービス提供クラス<br>
 * 
 * @author Jeffson
 * @version 2.0.3
 */
public class MailServiceImpl implements MailService {

	public static enum MailerType {
		SXB, PLUGIN, NATIVE
	}

	private JavaMailSenderImpl javaMailSender;
	private SendMailImpl sendMailImpl;
	private ThreadPoolTaskExecutor taskExecutor;
	private MailerType type = MailerType.SXB;

	public MailServiceImpl() {
		initAsyncExecutor();
	}

	public MailServiceImpl(MailerType type) {
		this.type = type;
		initAsyncExecutor();
	}

	private void initAsyncExecutor() {
		taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.initialize();
		taskExecutor.setCorePoolSize(5);
		taskExecutor.setMaxPoolSize(50);
		taskExecutor.setKeepAliveSeconds(60);
		taskExecutor.setQueueCapacity(1000);
		taskExecutor.setAllowCoreThreadTimeOut(true);
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);

	}

	private void initSender(String smtpHost, Integer smtpPort, String userName,
			String password) {
		switch (this.type) {
		case SXB: {
			sendMailImpl = new SendMailImpl();
			sendMailImpl.setHost(smtpHost);
			sendMailImpl.setUsername(userName);
			sendMailImpl.setPassword(password);
			sendMailImpl.setPort(smtpPort);
			break;
		}
		case PLUGIN: {
			new MailPlugin().start();
			break;
		}
		case NATIVE: {
			javaMailSender = new JavaMailSenderImpl();
			javaMailSender.setHost(smtpHost);
			javaMailSender.setPort(smtpPort);
			javaMailSender.setUsername(userName);
			javaMailSender.setPassword(password);
			break;
		}
		default:
			break;
		}

	}

	/**
	 * 添加邮件发送任务
	 * 
	 * @param mimeMessage
	 *            MimeMessage
	 */
	private void addSendTask(final MimeMessage mimeMessage) {
		try {
			taskExecutor.execute(new Runnable() {
				public void run() {
					javaMailSender.send(mimeMessage);

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addSendTask(final Mail mail) {
		try {
			taskExecutor.execute(new Runnable() {
				public void run() {

					sendMailImpl.send(mail);

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addSendTask(final String subject, final String text,
			final String from) {
		try {
			taskExecutor.execute(new Runnable() {
				public void run() {

					MailSender.sendText(subject, text, from);

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(String smtpFromMail, String smtpHost, Integer smtpPort,
			String smtpUsername, String smtpPassword, String toMail,
			String subject, String templatePath, Map<String, Object> model,
			boolean async) {
		Assert.hasText(smtpFromMail);
		Assert.hasText(smtpHost);
		Assert.notNull(smtpPort);
		Assert.hasText(smtpUsername);
		Assert.hasText(smtpPassword);
		Assert.hasText(toMail);
		Assert.hasText(subject);
		Assert.hasText(templatePath);
		initSender(smtpHost, smtpPort,smtpUsername, smtpPassword);
		try {
			Configuration configuration = FreeMarkerRender.getConfiguration();
			Template template = configuration.getTemplate(templatePath);
			StringWriter out = new StringWriter();
			template.process(model, out);

			String text = out.toString();
			javaMailSender.setHost(smtpHost);
			javaMailSender.setPort(smtpPort);
			javaMailSender.setUsername(smtpUsername);
			javaMailSender.setPassword(smtpPassword);
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
					mimeMessage, false, "utf-8");
			mimeMessageHelper.setFrom(MimeUtility.encodeWord(" <"
					+ smtpFromMail + ">"));
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setTo(toMail);
			mimeMessageHelper.setText(text, true);
			if (async) {
				addSendTask(mimeMessage);
			} else {
				javaMailSender.send(mimeMessage);
			}
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void send(String smtpFromMail, String smtpHost, Integer smtpPort,
			String smtpUsername, String smtpPassword, String toMail,
			String subject, String text, boolean async) {
		Assert.hasText(smtpFromMail);
		Assert.hasText(smtpHost);
		Assert.notNull(smtpPort);
		Assert.hasText(smtpUsername);
		Assert.hasText(smtpPassword);
		Assert.hasText(toMail);
		Assert.hasText(subject);
		initSender(smtpHost, smtpPort,smtpUsername, smtpPassword);
		try {

			switch (this.type) {
			case SXB: {
				Mail mail = new Mail();
				mail.setFrom(MimeUtility.encodeWord(" <"
						+ smtpFromMail + ">"));
				mail.addTo(toMail);
				mail.setSubject(subject);
				mail.setText(text);
				if (async) {
					addSendTask(mail);
				} else {
					sendMailImpl.send(mail);
				}
				break;
			}
			case NATIVE: {
				MimeMessage mimeMessage = javaMailSender.createMimeMessage();
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
						mimeMessage, false, "utf-8");
				mimeMessageHelper.setFrom(MimeUtility.encodeWord(" <"
						+ smtpFromMail + ">"));
				mimeMessageHelper.setSubject(subject);
				mimeMessageHelper.setTo(toMail);
				mimeMessageHelper.setText(text, true);

				if (async) {
					addSendTask(mimeMessage);
				} else {
					javaMailSender.send(mimeMessage);
				}
				break;
			}
			case PLUGIN: {
				if (async) {
					addSendTask(subject, text, toMail);
				} else {
					MailSender.sendText(subject, text, toMail);
				}
				break;
			}
			default:
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void send(String toMail, String subject, String templatePath,
			Map<String, Object> model, boolean async) {

		throw new RuntimeException("Not finish. Do not use this method.");
	}

	public void send(String toMail, String subject, String templatePath,
			Map<String, Object> model) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}

	public void send(String toMail, String subject, String templatePath) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}

	public void sendFindPasswordMail(String toMail, String username,
			SafeKey safeKey) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}

	@Override
	public void sendTestMail(String smtpFromMail, String smtpHost,
			Integer smtpPort, String smtpUsername, String smtpPassword,
			String toMail) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}

	public static void main(String args[]) {
		String host = "smtp.web-db.ws";
		String username = "son@hope123.co.jp";
		String password = "TsJuttOC";
		Integer port = 587;
		String toMail = "kevionsun@gmail.com";
		MailServiceImpl sender = new MailServiceImpl(MailServiceImpl.MailerType.NATIVE);
		for (int i = 0; i < 10; i++) {
			sender.send(username, host, port, username, password, toMail, "これはテストメールです、第"+ i + "通目", "ホープ社様フレムワークSXBから", false);
		}
	}
}