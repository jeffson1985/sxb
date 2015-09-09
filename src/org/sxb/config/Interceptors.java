/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
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

package org.sxb.config;

import java.util.ArrayList;
import java.util.List;

import org.sxb.aop.Interceptor;
import org.sxb.aop.InterceptorBuilder;

/**
 * The Interceptors is used to config global action interceptors and global service interceptors.
 */
final public class Interceptors {
	
	private final List<Interceptor> globalActionInterceptor = new ArrayList<Interceptor>();
	
	/**
	 * The same as addGlobalActionInterceptor. It is used to compatible with earlier version of sxb
	 */
	public Interceptors add(Interceptor globalActionInterceptor) {
		if (globalActionInterceptor != null)
			this.globalActionInterceptor.add(globalActionInterceptor);
		return this;
	}
	
	/**
	 * Add the global action interceptor to intercept all the actions.
	 */
	public void addGlobalActionInterceptor(Interceptor globalActionInterceptor) {
		if (globalActionInterceptor != null)
			this.globalActionInterceptor.add(globalActionInterceptor);
	}
	
	/**
	 * Add the global service interceptor to intercept all the method enhanced by aop Enhancer.
	 */
	public void addGlobalServiceInterceptor(Interceptor globalServiceInterceptor) {
		if (globalServiceInterceptor != null)
			InterceptorBuilder.addGlobalServiceInterceptor(globalServiceInterceptor);
	}
	
	public Interceptor[] getGlobalActionInterceptor() {
		return globalActionInterceptor.toArray(new Interceptor[globalActionInterceptor.size()]);
	}
}







