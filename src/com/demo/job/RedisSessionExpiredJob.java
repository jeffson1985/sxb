package com.demo.job;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RedisSessionExpiredJob implements Job{

	 public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		    //get param  from  job
		    Map<String, Object> data = jobExecutionContext.getJobDetail().getJobDataMap();
		    
		    
		  }

}
