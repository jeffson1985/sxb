package com.demo.controller.site;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.sxb.core.Controller;
import org.sxb.ext.route.ControllerBind;
import org.sxb.plugin.mail.MailSender;
import org.sxb.upload.ext.multipart.MultipartFile;
@ControllerBind(controllerKey = "/")
public class IndexController extends Controller{
	private static Map<String, Object> site = new HashMap<String, Object>();

	static{
		site.put("companyName","ホープ株式会社");
		site.put("telphone", "080-3582-5888");
		site.put("description", "楽旅はホープ株式会社が内部フレムワーク（SXB）をテストするため、作成した、ダミーのサイトです。");
		site.put("keywords", "this is for site SEO, jeffson,hope123, rakutabi,楽旅");
	}
	public void index(){
		
		setAttr("site",site);
		render("site/index.html");
	}
	
	public void about(){
		setAttr("site",site);
		render("site/about.html");
	}
	
	public void contact(){
		
		setAttr("site",site);
		render("site/contact.html");
	}
	
	public void destinations(){
		setAttr("site",site);
		render("site/destinations.html");
	}
	
	public void sendmail(){
		String name = getPara("name");
		String email = getPara("email");
		String phone = getPara("phone");
		
		try {
			SimpleEmail sm = MailSender.getSimpleEmail("Test", name +  "\n" + phone, email);
			
			sm.send();
			redirect("/");
		} catch (EmailException e) {
			 
			e.printStackTrace();
		}
		
	}
	
	public void upload(){
		render("site/upload.html");
	}
	
	public void saveFile(){
		int count = 0;
		for(MultipartFile file: this.getMultipartFiles()){
			
				//file.upload();
				//file.upload("sxb" + count + "." + file.getSuffix());
				try {
					file.upload("test", "sxb" + count++ + "." + file.getSuffix());
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}
		}
		System.out.println("上传form 中的普通数据测试:" + getPara("filename"));
		redirect("/");
	}
	
	// test JSON
	
	public void testJson(){
		render("site/testJson.html");
	}
	
	public void testXml(){
		render("site/testXml.html");
	}
	
}
