package org.sxb.mail.mailet;

import org.sxb.mail.fetch.ReceivedMail;

/**
 * Mailetインターフェース。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: Mailet.java,v 1.3
 */
public interface Mailet {

	void service(ReceivedMail mail);

}