/*
 * @(#) $Id: JISDataSource.java,v 1.1.2.1 2005/01/18 07:20:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

import org.sxb.mail.fetch.impl.sk_jp.io.CharCodeConverter;
import org.sxb.mail.fetch.impl.sk_jp.io.UnicodeCorrector;

/**
 * テキストの本文を送信するための DataSource です。
 */
public class JISDataSource implements DataSource {

	private byte[] data;

	public JISDataSource(String s) {
		try {
			data = CharCodeConverter.sjisToJis(UnicodeCorrector.getInstance("Windows-31J").correct(
					s).getBytes("Windows-31J"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("CANT HAPPEN");
		}
	}

	public String getContentType() {
		return "text/plain; charset=ISO-2022-JP";
	}

	public InputStream getInputStream() throws IOException {
		if (data == null)
			throw new IOException("no data");
		return new ByteArrayInputStream(data);
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("cannot do this");
	}

	public String getName() {
		return "dummy";
	}
}