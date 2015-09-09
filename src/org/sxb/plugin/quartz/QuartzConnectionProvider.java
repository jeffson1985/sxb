package org.sxb.plugin.quartz;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;
import org.sxb.log.Logger;
import org.sxb.plugin.activerecord.Config;
import org.sxb.plugin.activerecord.DbKit;

/**
 * @author son
 * @date 2015-05-04
 * @what 为quartz提供数据源
 */
public class QuartzConnectionProvider implements ConnectionProvider {
	private Logger logger = Logger.getLogger(getClass());
	private Config config = null;
	private Connection conn = null;

	public Connection getConnection() throws SQLException {
		conn = config.getConnection();
		
		return conn;
	}

	public void shutdown() throws SQLException {
		if (QuartzPlugin.isDsAlone()) {
			conn.close();
		    if (config == null) {
		    	logger.error("Quartz connection provider shut down error!!");
		      throw new RuntimeException("quartz datasource  not found");
		    }
		}
	}

	public void initialize() throws SQLException {
		config = DbKit.getConfig(QuartzPlugin.dsName);
	    if (config == null) {
	      throw new RuntimeException("quartz datasource  not found");
	    }
	}
}
