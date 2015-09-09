package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name="Article_category")
public class ArticleCategory extends Model<ArticleCategory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5711503330835340496L;
	
	public static final ArticleCategory  dao = new ArticleCategory();

}
