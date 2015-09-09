package com.demo.job;

import java.util.Date;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DemoJob2 implements Job{

	 public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		    //get param  from  job
		    Map data = jobExecutionContext.getJobDetail().getJobDataMap();
		    System.out.println("Hello demojob 2,"+data.get("name")+"," + new Date().getTime());
		  }

}
