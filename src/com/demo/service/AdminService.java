package com.demo.service;


import com.demo.model.Admin;

public class AdminService {
	
	public Admin checkLogin(Admin admin){
		
		String sql = "select a.*, r.name as role_name from admin a, role r  where a.role_id = r.id and  a.username = ? and a.password = ? ";
		admin = admin.findFirst(sql, admin.getStr("username"), admin.getStr("password"));
		return admin == null? null:admin;
	}
}
