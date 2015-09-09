package com.demo.model;

import org.sxb.plugin.activerecord.Model;
import org.sxb.plugin.activerecord.annotation.TableBind;

@TableBind(name = "student")
public class Student extends Model<Student> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4505523671782577538L;
	public static final Student dao = new Student();

}
