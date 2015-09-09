/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sxb.core.scheduling.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.enterprise.concurrent.ManagedExecutors;
import javax.enterprise.concurrent.ManagedTask;

import org.sxb.core.task.AsyncListenableTaskExecutor;
import org.sxb.core.task.support.TaskExecutorAdapter;
import org.sxb.core.scheduling.SchedulingAwareRunnable;
import org.sxb.core.scheduling.SchedulingTaskExecutor;
import org.sxb.kit.ClassKits;
import org.sxb.kit.concurrent.ListenableFuture;

/**
 * Adapter that takes a {@code java.util.concurrent.Executor} and exposes
 * a Spring {@link org.sxb.core.task.TaskExecutor} for it.
 * Also detects an extended {@code java.util.concurrent.ExecutorService}, adapting
 * the {@link org.sxb.core.task.AsyncTaskExecutor} interface accordingly.
 *
 * <p>Autodetects a JSR-236 {@link javax.enterprise.concurrent.ManagedExecutorService}
 * in order to expose {@link javax.enterprise.concurrent.ManagedTask} adapters for it,
 * exposing a long-running hint based on {@link SchedulingAwareRunnable} and an identity
 * name based on the given Runnable/Callable's {@code toString()}. For JSR-236 style
 * lookup in a Java EE 7 environment, consider using {@link DefaultManagedTaskExecutor}.
 *
 * <p>Note that there is a pre-built {@link ThreadPoolTaskExecutor} that allows
 * for defining a {@link java.util.concurrent.ThreadPoolExecutor} in bean style,
 * exposing it as a Spring {@link org.sxb.core.task.TaskExecutor} directly.
 * This is a convenient alternative to a raw ThreadPoolExecutor definition with
 * a separate definition of the present adapter class.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see DefaultManagedTaskExecutor
 * @see ThreadPoolTaskExecutor
 */
public class ConcurrentTaskExecutor implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {

	private static Class<?> managedExecutorServiceClass;

	static {
		try {
			managedExecutorServiceClass = ClassKits.forName(
					"javax.enterprise.concurrent.ManagedExecutorService",
					ConcurrentTaskScheduler.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// JSR-236 API not available...
			managedExecutorServiceClass = null;
		}
	}

	private Executor concurrentExecutor;

	private TaskExecutorAdapter adaptedExecutor;


	/**
	 * Create a new ConcurrentTaskExecutor, using a single thread executor as default.
	 * @see java.util.concurrent.Executors#newSingleThreadExecutor()
	 */
	public ConcurrentTaskExecutor() {
		setConcurrentExecutor(null);
	}

	/**
	 * Create a new ConcurrentTaskExecutor, using the given {@link java.util.concurrent.Executor}.
	 * <p>Autodetects a JSR-236 {@link javax.enterprise.concurrent.ManagedExecutorService}
	 * in order to expose {@link javax.enterprise.concurrent.ManagedTask} adapters for it.
	 * @param concurrentExecutor the {@link java.util.concurrent.Executor} to delegate to
	 */
	public ConcurrentTaskExecutor(Executor concurrentExecutor) {
		setConcurrentExecutor(concurrentExecutor);
	}


	/**
	 * Specify the {@link java.util.concurrent.Executor} to delegate to.
	 * <p>Autodetects a JSR-236 {@link javax.enterprise.concurrent.ManagedExecutorService}
	 * in order to expose {@link javax.enterprise.concurrent.ManagedTask} adapters for it.
	 */
	public final void setConcurrentExecutor(Executor concurrentExecutor) {
		if (concurrentExecutor != null) {
			this.concurrentExecutor = concurrentExecutor;
			if (managedExecutorServiceClass != null && managedExecutorServiceClass.isInstance(concurrentExecutor)) {
				this.adaptedExecutor = new ManagedTaskExecutorAdapter(concurrentExecutor);
			}
			else {
				this.adaptedExecutor = new TaskExecutorAdapter(concurrentExecutor);
			}
		}
		else {
			this.concurrentExecutor = Executors.newSingleThreadExecutor();
			this.adaptedExecutor = new TaskExecutorAdapter(this.concurrentExecutor);
		}
	}

	/**
	 * Return the {@link java.util.concurrent.Executor} that this adapter delegates to.
	 */
	public final Executor getConcurrentExecutor() {
		return this.concurrentExecutor;
	}


	@Override
	public void execute(Runnable task) {
		this.adaptedExecutor.execute(task);
	}

	@Override
	public void execute(Runnable task, long startTimeout) {
		this.adaptedExecutor.execute(task, startTimeout);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.adaptedExecutor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.adaptedExecutor.submit(task);
	}

	@Override
	public ListenableFuture<?> submitListenable(Runnable task) {
		return this.adaptedExecutor.submitListenable(task);
	}

	@Override
	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		return this.adaptedExecutor.submitListenable(task);
	}

	/**
	 * This task executor prefers short-lived work units.
	 */
	@Override
	public boolean prefersShortLivedTasks() {
		return true;
	}


	/**
	 * TaskExecutorAdapter subclass that wraps all provided Runnables and Callables
	 * with a JSR-236 ManagedTask, exposing a long-running hint based on
	 * {@link SchedulingAwareRunnable} and an identity name based on the task's
	 * {@code toString()} representation.
	 */
	private static class ManagedTaskExecutorAdapter extends TaskExecutorAdapter {

		public ManagedTaskExecutorAdapter(Executor concurrentExecutor) {
			super(concurrentExecutor);
		}

		@Override
		public void execute(Runnable task) {
			super.execute(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public Future<?> submit(Runnable task) {
			return super.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return super.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public ListenableFuture<?> submitListenable(Runnable task) {
			return super.submitListenable(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
			return super.submitListenable(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}
	}


	/**
	 * Delegate that wraps a given Runnable/Callable  with a JSR-236 ManagedTask,
	 * exposing a long-running hint based on {@link SchedulingAwareRunnable}
	 * and a given identity name.
	 */
	protected static class ManagedTaskBuilder {

		public static Runnable buildManagedTask(Runnable task, String identityName) {
			Map<String, String> properties = new HashMap<String, String>(2);
			if (task instanceof SchedulingAwareRunnable) {
				properties.put(ManagedTask.LONGRUNNING_HINT,
						Boolean.toString(((SchedulingAwareRunnable) task).isLongLived()));
			}
			properties.put(ManagedTask.IDENTITY_NAME, identityName);
			return ManagedExecutors.managedTask(task, properties, null);
		}

		public static <T> Callable<T> buildManagedTask(Callable<T> task, String identityName) {
			Map<String, String> properties = new HashMap<String, String>(1);
			properties.put(ManagedTask.IDENTITY_NAME, identityName);
			return ManagedExecutors.managedTask(task, properties, null);
		}
	}

}
