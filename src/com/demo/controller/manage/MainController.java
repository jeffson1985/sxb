package com.demo.controller.manage;

import org.sxb.aop.Before;
import org.sxb.aop.Clear;
import org.sxb.core.Controller;
import org.sxb.ext.route.ControllerBind;

import com.demo.interceptor.AccessInterceptor;
import com.demo.model.Admin;
import com.demo.service.AdminService;
import com.demo.validator.LoginValidator;
@ControllerBind(controllerKey = "/manage")
@Before(AccessInterceptor.class)
public class MainController extends Controller {
	private AdminService adminService = new AdminService();
	
	/**
	 * <p>{@code controllerKey} : /manage のデフォルトページ <br>
	 * view ファイル -> /webroot/WEB-INF/template/manage/index.html <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	public void index() {
		Admin admin = getSessionAttr("logined_admin");
		if(admin != null){
			setAttr("loginedAdmin",admin);
		}
		
		setAttr("title", "SXBフレムワーク管理側");
		setAttr("keyword", "SXBフレムワーク管理側");
		setAttr("description", "SXBフレムワーク管理側");
		render("index.html");
	}

	@Clear(AccessInterceptor.class)
	public void login() {
		//Redis.use().set("redisKey", "保存在缓存");
		Admin admin = new Admin();
		String username = getCookie("username");
		String password = getCookie("password");
		if(username != null && password != null){
			admin.set("password", password);
			admin.set("username", username);
			setAttr("admin", admin);
			setAttr("checked", "checked");
		}
		
		render("login.html");
	}

	@Clear(AccessInterceptor.class)
	@Before(LoginValidator.class)
	public void checkLogin() {
		//System.out.println("测试Redis缓存" + Redis.use().get("redisKey"));
		Admin admin = getModel(Admin.class);
		Admin ad = adminService.checkLogin(admin);
		
		if (admin.getCount() == 0) {
			admin.save();
		}
		if (ad != null) {
			System.out.println("Login Successed");
			setSessionAttr("status", "success");
			setSessionAttr("logined_admin", ad);
			redirect("/manage");
		} else {
			keepModel(Admin.class);
			this.setAttr("usernameMsg", "ユーザーとパスワードは一致ではらりません");
			render("login.html");
		}
	}

	@Clear(AccessInterceptor.class)
	public void logout() {
		this.removeSessionAttr("logined_admin");
		redirect("/manage/login");
	}
	
	public void main() {
		
		setAttr("title", "SXBフレムワーク管理側");
		setAttr("keyword", "SXBフレムワーク管理側");
		setAttr("description", "SXBフレムワーク管理側");
		render("index.html");
	}
}
