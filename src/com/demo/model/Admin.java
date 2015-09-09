package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name="admin")
public class Admin extends Model<Admin> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2084480666956199392L;
	
	public static final Admin  dao = new Admin();
	public Role getRole(Long roleId){
		Role role = Role.dao.findById(roleId);
		return role;
	}
}
