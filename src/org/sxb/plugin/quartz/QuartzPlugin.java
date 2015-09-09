package org.sxb.plugin.quartz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.sxb.kit.PropKit;
import org.sxb.kit.FileKit;
import org.sxb.log.Logger;
import org.sxb.plugin.IPlugin;
import org.sxb.plugin.quartz.exception.QuartzException;
import org.sxb.plugin.quartz.job.QuartzCronJob;
import org.sxb.plugin.quartz.job.QuartzOnceJob;

/**
 * Created by son on 14-4-21.
 */
public class QuartzPlugin implements IPlugin {

	private static final Logger logger = Logger.getLogger(QuartzPlugin.class);
	public static String dsName = "main";
	private static boolean dsAlone;
	/**
	 * 默认配置文件*
	 */
	private String config = "quartz/quartz.properties";
	private String jobs = "quartz/jobs.properties";

	public QuartzPlugin() {
		this(false);
	}

	public QuartzPlugin(boolean dsAlone) {
		this(null, null, dsAlone);
	}

	public QuartzPlugin(String config, String jobs) {
		this(config, jobs, false);
	}

	public QuartzPlugin(String config, String jobs, boolean dsAlone) {
		if (config != null) {
			this.config = config;
		}
		if (jobs != null) {
			this.jobs = jobs;
		}
		QuartzPlugin.dsAlone = dsAlone;
	}

	public static boolean isDsAlone() {
		return dsAlone;
	}

	public boolean start() {
		try {
			// 加载配置文件
			Properties configProp = PropKit.use(config).getProperties();
			// 实例化
			Quartzer.setSchedulerFactory(new StdSchedulerFactory(configProp));
			// 获取Scheduler
			Scheduler sched = Quartzer.getSchedulerFactory().getScheduler();
			// 内存,数据库的任务
			sched.start();
			// 属性文件中的任务
			startPropertiesJobs();
			return true;
		} catch (Exception e) {
			logger.error("Can't start quartz plugin.");
			throw new QuartzException("Can't start quartz plugin.", e);
		}
	}

	public boolean stop() {
		try {
			Quartzer.getSchedulerFactory().getScheduler().shutdown();
			Quartzer.setSchedulerFactory(null);
			return true;
		} catch (Exception e) {
			throw new QuartzException("Can't stop quartz plugin.", e);
		}
	}

	/**
	 * 启动配置文件中的任务
	 */
	@SuppressWarnings("unchecked")
	public void startPropertiesJobs() {
		if (FileKit.exist(jobs)) {
			Properties jobsProp = PropKit.use(jobs).getProperties();
			Enumeration<Object> enums = jobsProp.keys();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<String> startedJobs = new ArrayList<String>();
			String[] keyArr;
			String key, jobName, jobClassKey, groupKey, cronKey, onceKey, enable, group, jobCron, jobOnce, jobClassName;
			Class<Job> clazz;
			Date onceTime;
			while (enums.hasMoreElements()) {
				key = enums.nextElement() + "";
				if (!key.startsWith("job")) {
					continue;
				}

				keyArr = key.split("\\.");
				jobName = keyArr[1];
				// 已经启动过的任务
				if (startedJobs.contains(jobName))
					continue;
				startedJobs.add(jobName);

				jobClassKey = key.replace(keyArr[2], "class");
				groupKey = key.replace(keyArr[2], "group");
				cronKey = key.replace(keyArr[2], "cron");
				onceKey = key.replace(keyArr[2], "once");
				enable = key.replace(keyArr[2], "enable");

				// 判断任务是否启用
				if (!Boolean.valueOf(jobsProp.getProperty(enable))) {
					continue;
				}

				group = jobsProp.getProperty(groupKey);
				jobCron = jobsProp.getProperty(cronKey);
				jobOnce = jobsProp.getProperty(onceKey);
				jobClassName = jobsProp.getProperty(jobClassKey);

				try {
					clazz = (Class<Job>) Class.forName(jobClassName);

				} catch (ClassNotFoundException e) {
					throw new QuartzException(e.getMessage(), e);
				}
				// 启动任务
				if (jobCron != null) {
					if (group != null) {
						new QuartzCronJob<Job>(group, keyArr[1], jobCron, clazz)
								.start();
					} else {
						new QuartzCronJob<Job>(keyArr[1], jobCron, clazz)
								.start();
					}
				} else if (jobOnce != null) {
					try {
						onceTime = sdf.parse(jobOnce);
					} catch (ParseException e) {
						throw new QuartzException(e.getMessage(), e);
					}
					if (System.currentTimeMillis() <= onceTime.getTime()) {
						if (group != null) {
							new QuartzOnceJob<Job>(group, keyArr[1], onceTime, clazz)
									.start();
						} else {
							new QuartzOnceJob<Job>(keyArr[1], onceTime, clazz)
									.start();
						}
					}
				} else {
					if (group != null) {
						new QuartzOnceJob<Job>(group, keyArr[1], new Date(), clazz)
								.start();
					} else {
						new QuartzOnceJob<Job>(keyArr[1], new Date(), clazz).start();
					}
				}
			}
		}
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getJobs() {
		return jobs;
	}

	public void setJobs(String jobs) {
		this.jobs = jobs;
	}

}
