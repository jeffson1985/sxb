package org.sxb.mail.impl;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.sxb.log.Logger;

/**
 * VelocityのログメッセージをCommonsLoggingを通して出力させるクラス。
 * 
 * 
 * @see XMLVelocityMailBuilderImpl
 * @see JDomXMLMailBuilder
 * 
 * @author    Jeffson  (jeffson.app@gmail.com).3
 * 
 * @version $Id: VelocityLogSystem.java,v 1.2
 */
public class VelocityLogSystem implements LogChute {

	private static Logger log = Logger.getLogger(Velocity.class);

	/**
	 * @see org.apache.velocity.runtime.log.LogSystem#init(org.apache.velocity.runtime.RuntimeServices)
	 */
	public void init(RuntimeServices rsvc) throws Exception {
	// do nothing
	}

	/**
	 * @see org.apache.velocity.runtime.log.LogSystem#logVelocityMessage(int, java.lang.String)
	 */
	public void logVelocityMessage(int level, String message) {
		switch (level) {
			case DEBUG_ID:
				log.debug(message);
				break;
			case INFO_ID:
				log.info(message);
				break;
			case WARN_ID:
				log.warn(message);
				break;
			case ERROR_ID:
				log.error(message);
				break;
		}
	}

	@Override
	public boolean isLevelEnabled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void log(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(int arg0, String arg1, Throwable arg2) {
		// TODO Auto-generated method stub
		
	}

}