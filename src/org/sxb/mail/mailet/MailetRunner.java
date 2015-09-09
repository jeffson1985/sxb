package org.sxb.mail.mailet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sxb.mail.fetch.FetchMailPro;
import org.sxb.mail.fetch.ReceivedMail;

/**
 * メールの受信とMailetの起動を行うクラス。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailetRunner.java,v 1.1.4
 */
public class MailetRunner {

	private List<Object> mailetWrapperList;

	private FetchMailPro fetchMailPro;

	/**
	 * コンストラクタ。
	 */
	public MailetRunner() {
		mailetWrapperList = new ArrayList<Object>();
	}

	/**
	 * メール受信とMailetの起動を行います。
	 */
	public void run() {
		fetchMailPro.connect();
		try {
			int count = fetchMailPro.getMailCount();
			for (int i = 1; i <= count; i++) {
				ReceivedMail mail = fetchMailPro.getMail(i);
				processMail(mail);
			}
		} finally {
			fetchMailPro.disconnect();
		}
	}

	/**
	 * 指定された受信メールに対してMailetを適用します。
	 * 
	 * @param mail MailetWrapperに渡す受信メール
	 */
	private void processMail(ReceivedMail mail) {
		for (Iterator<Object> itr = mailetWrapperList.iterator(); itr.hasNext();) {
			MailetWrapper mailetWrapper = (MailetWrapper)itr.next();
			mailetWrapper.execute(mail);
		}
	}

	/**
	 * メールの受信に使用するFetchMailProインターフェースの実装インスタンスをセットします。
	 * 
	 * @param fetchMailPro FetchMailProインターフェースの実装インスタンス
	 */
	public void setFetchMailPro(FetchMailPro fetchMailPro) {
		this.fetchMailPro = fetchMailPro;
	}

	/**
	 * 実行するMailetのMailetWrapperリストをセットします。
	 * 
	 * @param mailetWrapperList 実行するMailetのMailetWrapperリスト
	 */
	public void setMailetWrapperList(List<Object> mailetWrapperList) {
		this.mailetWrapperList = mailetWrapperList;
	}
}