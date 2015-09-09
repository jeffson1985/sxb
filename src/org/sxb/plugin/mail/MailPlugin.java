package org.sxb.plugin.mail;

import org.sxb.core.Const;
import org.sxb.kit.Prop;
import org.sxb.kit.PropKit;
import org.sxb.plugin.IPlugin;


/**
 * Created by son on 14-5-6.
 */
public class MailPlugin implements IPlugin {

  private static Mail mail;
  private String config;

  public MailPlugin() {
    this(Const.DEFATLT_SXB_CONFIG_NAME);
  }

  public MailPlugin(String config) {
    this.config = config;
  }

  public static Mail getMail() {
    return mail;
  }

  public boolean start() {
    Prop prop = PropKit.use(config);
    String charset = prop.get("smtp.charset", "utf-8");
    String host = prop.get("smtp.host", "");
    if (host == null || host.isEmpty()) {
      throw new MailException("email host has not found!");
    }
    String port = prop.get("smtp.port", "");

    boolean ssl = prop.getBoolean("smtp.ssl", false);
    String sslport = prop.get("smtp.sslport", "");
    int timeout = prop.getInt("smtp.timeout", 60000);
    int connectout = prop.getInt("smtp.connectout", 60000);
    boolean tls = prop.getBoolean("smtp.tls", false);
    boolean debug = prop.getBoolean("smtp.debug", false);
    String user = prop.get("smtp.user");

    if (user == null || user.isEmpty()) {
      throw new MailException("email user has not found!");
    }
    String password = prop.get("smtp.password");
    if (password == null || password.isEmpty()) {
      throw new MailException("email password has not found!");
    }

    String name = prop.get("smtp.name");

    String from = prop.get("smtp.from", user);
    if (from == null || from.isEmpty()) {
      throw new MailException("email from has not found!");
    }
    mail = new Mail(charset, host, sslport, timeout, connectout, port, ssl, tls, debug, user, password, name, from);
    return true;
  }

  public boolean stop() {
    mail = null;
    return true;
  }
}
