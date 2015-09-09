package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name = "article")
public class Article extends Model<Article> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4500411480945644447L;
	
	public static final Article dao = new Article();

}
