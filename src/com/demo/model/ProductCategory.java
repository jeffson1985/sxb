package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name="product_category")
public class ProductCategory extends Model<ProductCategory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -577109182069333183L;
	
	public static final ProductCategory  dao = new ProductCategory();

}
