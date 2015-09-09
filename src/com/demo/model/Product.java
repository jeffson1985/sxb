package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name="product")
public class Product extends Model<Product> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4494633549705299723L;
	
	public static final Product dao = new Product();

}
