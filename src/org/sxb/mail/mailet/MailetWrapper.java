package org.sxb.mail.mailet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sxb.mail.fetch.ReceivedMail;

/**
 * MailetインスタンスとMatcherインスタンスのリストを持つMailetの実行単位となるクラス。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailetWrapper.java,v 1.1.2
 */
public class MailetWrapper {

	private Mailet mailet;

	private List<Object> matcherList;

	/**
	 * コンストラクタ。
	 */
	public MailetWrapper() {
		matcherList = new ArrayList<Object>();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param mailet Mailetインスタンス
	 * @param matcherList Matcherインスタンスのリスト
	 */
	public MailetWrapper(Mailet mailet, List<Object> matcherList) {
		this();
		this.mailet = mailet;
		this.matcherList = matcherList;
	}

	/**
	 * リストされているMatcherの条件をクリアしたMailetを実行します。
	 * 
	 * @param mail 受信メール
	 */
	public void execute(ReceivedMail mail) {
		for (Iterator<?> itr = matcherList.iterator(); itr.hasNext();) {
			Matcher m = (Matcher)itr.next();
			if (!m.match(mail)) {
				return;
			}
		}
		mailet.service(mail);
	}

	/**
	 * Mailetインスタンスを返します。
	 * 
	 * @return Mailetインスタンス
	 */
	public Mailet getMailet() {
		return mailet;
	}

	/**
	 * Mailetインスタンスをセットします。
	 * 
	 * @param mailet Mailetインスタンス
	 */
	public void setMailet(Mailet mailet) {
		this.mailet = mailet;
	}

	/**
	 * Matcherインスタンスのリストを返します。
	 * 
	 * @return Matcherインスタンスのリスト
	 */
	public List<?> getMatcherList() {
		return matcherList;
	}

	/**
	 * Matcherインスタンスのリストをセットします。
	 * 
	 * @param matcherList Matcherインスタンスのリスト
	 */
	public void setMatcherList(List<Object> matcherList) {
		this.matcherList = matcherList;
	}
}