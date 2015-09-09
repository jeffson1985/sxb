package com.demo.job;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.demo.model.Admin;

public class DemoJob implements Job{

	 public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		    //get param  from  job
		    Map<String, Object> data = jobExecutionContext.getJobDetail().getJobDataMap();
		    Admin dao = new Admin();
		    
		    List<Admin> admins = dao.find("SELECT * FROM admin");
		    for(Admin ad: admins){
		    	System.out.println("hi demojob,"+ad.getStr("name")+"," + new Date().getTime());
		    }
		    
		  }

}
