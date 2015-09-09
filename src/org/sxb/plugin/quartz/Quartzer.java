package org.sxb.plugin.quartz;

import org.sxb.kit.Lister;
import org.sxb.plugin.quartz.job.QuartzJob;
import org.quartz.Job;
import org.quartz.SchedulerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by son on 14-4-21.
 */
public class Quartzer {

	private static SchedulerFactory schedulerFactory;

	private static List<QuartzJob<? extends Job>> quartzJobs = Lister.of();

	private static Map<String, Long> quartzKeys = new ConcurrentHashMap<String, Long>();

	private Quartzer() {
	}

	public static QuartzKey nextKey(String name) {
		return nextKey(QuartzKey.DEFAULT_GROUP, name);
	}

	public static QuartzKey nextKey(String group, String name) {
		Long id = quartzKeys.get(group + "." + name);
		if (id == null) {
			id = 1L;
		} else {
			id++;
		}
		quartzKeys.put(group + "." + name, id);
		return new QuartzKey(id, group, name);
	}

	public static QuartzJob<? extends Job> getJob(QuartzKey quartzKey) {
		for (QuartzJob<?  extends Job> quartzJob : quartzJobs) {
			if (quartzJob.getQuartzKey().equals(quartzKey)) {
				return quartzJob;
			}
		}
		return null;
	}

	public static void stopJob(QuartzKey quartzKey) {
		for (QuartzJob<? extends Job> quartzJob : quartzJobs) {
			if (quartzJob.getQuartzKey().equals(quartzKey)) {
				quartzJob.stop();
			}
		}
	}

	public static void pauseJob(QuartzKey quartzKey) {
		for (QuartzJob<? extends Job> quartzJob : quartzJobs) {
			if (quartzJob.getQuartzKey().equals(quartzKey)) {
				quartzJob.pause();
			}
		}
	}

	public static void resumeJob(QuartzKey quartzKey) {
		for (QuartzJob<? extends Job> quartzJob : quartzJobs) {
			if (quartzJob.getQuartzKey().equals(quartzKey)) {
				quartzJob.resume();
			}
		}
	}

	public static SchedulerFactory getSchedulerFactory() {
		return schedulerFactory;
	}

	public static void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		Quartzer.schedulerFactory = schedulerFactory;
	}

	public static List<QuartzJob<? extends Job>> getQuartzJobs() {
		return quartzJobs;
	}

	public static void setQuartzJobs(List<QuartzJob<? extends Job>> quartzJobs) {
		Quartzer.quartzJobs = quartzJobs;
	}

	public static void addQuartzJob(QuartzJob<? extends Job> startedJob) {
		Quartzer.quartzJobs.add(startedJob);
	}

	public static void removeQuartzJob(QuartzJob<? extends Job> startedJob) {
		Quartzer.quartzJobs.remove(startedJob);
	}
}