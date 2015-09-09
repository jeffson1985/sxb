package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name="role")
public class Role extends Model<Role> {

	/**
	 * Role Model
	 */
	private static final long serialVersionUID = 7914692565231214182L;
	
	public static final Role dao = new Role();

}
