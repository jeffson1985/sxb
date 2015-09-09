package com.demo.controller.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sxb.aop.Before;
import org.sxb.aop.Clear;
import org.sxb.core.Controller;
import org.sxb.ext.interceptor.POST;
import org.sxb.ext.route.ControllerBind;
import org.sxb.plugin.activerecord.Page;
import org.sxb.plugin.activerecord.tx.Tx;
import org.sxb.plugin.ehcache.CacheInterceptor;
import org.sxb.plugin.ehcache.CacheKit;
import org.sxb.plugin.ehcache.CacheName;
import org.sxb.render.report.ReportType;
import org.sxb.upload.UploadFile;

import com.alibaba.fastjson.JSONObject;
import com.demo.interceptor.AccessInterceptor;
import com.demo.model.Admin;
import com.demo.model.Student;

@ControllerBind(controllerKey = "/manage/admin")
@Before(AccessInterceptor.class)
public class AdminController extends Controller {
	Admin dao = Admin.dao;


	/**
	 * リスト一覧画面
	 * <p>{@code controllerKey} : /manage/admin/index のデフォルトページ <br>
	 * view ファイル -> /webroot/WEB-INF/template/manage/admin/index.html <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Before(CacheInterceptor.class)
	@CacheName("adminList")
	public void index() {
		List<Admin> adminList = dao.find("select * from admin");
		Page<Admin> list = dao.paginate(this.getParaToInt("p", 1), 10, "select * " , "from admin");
		setAttr("page", list);
		setAttr("adminList", adminList);
		render("index.html");
	}


	/**
	 * 新規追加画面
	 * <p>{@code controllerKey} : /manage/admin/add のデフォルトページ <br>
	 * view ファイル -> /webroot/WEB-INF/template/manage/admin/add.html <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	public void add() {

	}
	

	/**
	 * 編集画面
	 * <p>{@code controllerKey} : /manage/admin/edit のデフォルトページ <br>
	 * view ファイル -> /webroot/WEB-INF/template/manage/admin/edit.html <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	public void edit(){
		Admin admin = dao.findById(getParaToInt("id"));
		if(admin == null){
			redirect("/manage/admin");
		}
		setAttr("admin", admin);
		
	}
	

	/**
	 * 詳細画面
	 * <p>{@code controllerKey} : /manage/admin/view のデフォルトページ <br>
	 * view ファイル -> /webroot/WEB-INF/template/manage/admin/view.html <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	public void view(){
		Admin admin = dao.findById(getParaToInt("id"));
		if(admin == null){
			redirect("/manage/admin");
		}
		setAttr("admin", admin);
		
	}
	

	/**
	 * 削除する
	 * <p>{@code controllerKey} : /manage/admin/delete のデフォルトページ <br>
	 * view ファイル -> なし<br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	public void delete(){
		int id = getParaToInt("id", 0);
		System.out.println("id = " + id);
		if(id == 0){
			renderJson("status", "error");
		}
		if(dao.deleteById(id)){
			CacheKit.removeAll("adminList");
			renderJson("status", "ok");
		}
		else
			renderJson("status", "error");
	}


	/**
	 * 保全する
	 * <p>{@code controllerKey} : /manage/admin/save のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Before(Tx.class)
	public void save() {
		
		List<UploadFile> uf = getFiles();

		StringBuffer image = new StringBuffer();
		
		for (UploadFile up : uf) {
			String name = up.getFileName();
			File file = up.getFile();
			String saveDir = up.getSaveDirectory();
			saveDir += "redir/";
			up.setSaveDirectory(saveDir);
			File newFile = new File(saveDir, name);
			if(!newFile.getParentFile().exists())
				newFile.getParentFile().mkdir();
			file.renameTo(newFile);
			if(up.isAllow()){
				image.append(name + "|");
			}else{
				System.out.println("not allowed file type");
			}
		}
		if(image.length() > 0)
			image.deleteCharAt(image.lastIndexOf("|"));

		Admin admin = this.getModel(Admin.class);
		admin.set("role_id", 1);
		admin.set("create_date", new Date());
		admin.set("modify_date", new Date());
		admin.set("image", image.toString());
		admin.save();

		redirect("/manage/admin");
	}
	

	/**
	 * 更新の保存
	 * <p>{@code controllerKey} : /manage/admin/update のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「Freemarker」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Before(Tx.class)
	public void update() {
		
		List<UploadFile> uf = getFiles();

		StringBuffer image = new StringBuffer();
		
		for (UploadFile up : uf) {
			String name = up.getFileName();
			File file = up.getFile();
			String saveDir = up.getSaveDirectory();
			saveDir += "redir/";
			up.setSaveDirectory(saveDir);
			File newFile = new File(saveDir, name);
			if(!newFile.getParentFile().exists())
				newFile.getParentFile().mkdir();
			file.renameTo(newFile);
			image.append(name + "|");
		}
		if(image.length() > 0)
			image.deleteCharAt(image.lastIndexOf("|"));

		Admin admin = this.getModel(Admin.class);

		admin.set("role_id", 1);
		admin.set("create_date", new Date());
		admin.set("modify_date", new Date());
		if(image.length()>0){
			admin.set("image", image.toString());
		}
		
		admin.update();

		redirect("/manage/admin");
	}


	/**
	 * 返回管理员JSON列表
	 * <p>{@code controllerKey} : /manage/admin/list のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「text/json」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void list() {
		Admin dao = new Admin();

		List<Admin> lists = dao.find("select * from admin");
		this.renderJson(lists);
	}

	
	/**
	 * 测试API POST接收
	 * <p>{@code controllerKey} : /manage/admin/apiGet のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「text/json」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	//@Before(POST.class)
	@Clear(AccessInterceptor.class)
	public void apiGet() {

		//System.out.println("code:" + getPara("data"));
		JSONObject rst = getParaJson();
		System.out.println("admin:" + rst.toString());
		//System.out.println(rst.getJSONArray("marks").getJSONObject(0).getString("horse_number"));
	

		JSONObject obj = new JSONObject();
		obj.put("flag", 1);
		obj.put("message", "成功");

		renderJson(obj);
		//renderHtml(obj.toString());
	}

	/**
	 * 测试API
	 * CSV出す
	 * <p>{@code controllerKey} : /manage/admin/getCsv のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「csv」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void getCsv() {
		List<Student> s = Student.dao
				.find("SELECT id, name, age FROM student LIMIT 300");
		List<String> headers = new ArrayList<String>();
		headers.add("ID");
		headers.add("名前");
		headers.add("種類");
		List<List<Object>> data = new ArrayList<List<Object>>();
		List<Object> d1 = new ArrayList<Object>();
		d1.add(1);
		d1.add("Jeffson");
		data.add(d1);
		List<Object> d2 = new ArrayList<Object>();
		d2.add(2);
		d2.add("enei");
		data.add(d2);
		renderCsv(headers, s);
	}

	/**
	 * 测试API
	 * CSV出す
	 * <p>{@code controllerKey} : /manage/admin/getStudent のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「text/json」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void getStudent() {
		/**
		JSONObject rst = getParaJSON();
		System.out.println(this.getRemoteIp());
		System.out.println(rst.toJSONString());
		List<Student> s = Student.dao.find("SELECT * FROM student LIMIT 300");
		this.renderJson(s);
		*/
	}

	/**
	 * 测试API
	 * PDF出す
	 * <p>{@code controllerKey} : /manage/admin/getPdf のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「application/PDF」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void getPdf() {
		setAttr("age", 30);
		setAttr("title", "中央情報専門学校");
		renderReport("ms", ReportType.PDF);
	}

	/**
	 * 测试API
	 * Excel出す
	 * <p>{@code controllerKey} : /manage/admin/getExcel のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「application/ms-xlsx」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void getExcel() {
		setAttr("age", 30);
		setAttr("title", "中央情報専門学校");
		renderReport("ms", ReportType.EXCEL);
	}

	/**
	 * 测试API
	 * Word出す
	 * <p>{@code controllerKey} : /manage/admin/getWord のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「application/ms-word」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void getWord() {
		setAttr("age", 30);
		setAttr("title", "中央情報専門学校");
		renderReport("ms", ReportType.WORD);
	}

	/**
	 * 测试API
	 * PPT出す
	 * <p>{@code controllerKey} : /manage/admin/getPpt のデフォルトページ <br>
	 * view ファイル -> なし <br>
	 * デフォルトレンダーは「application/ms-powerpoint」です。<br>
	 * メソッドは必ずパブリックで無参無返すである事。
	 */
	@Clear(AccessInterceptor.class)
	public void getPpt() {
		setAttr("age", 30);
		setAttr("title", "中央情報専門学校");
		renderReport("ms", ReportType.POWERPOINT);
	}
}
