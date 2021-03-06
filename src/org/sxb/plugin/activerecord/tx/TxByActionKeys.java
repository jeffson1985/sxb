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

package org.sxb.plugin.activerecord.tx;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.sxb.aop.Interceptor;
import org.sxb.aop.Invocation;
import org.sxb.plugin.activerecord.Config;
import org.sxb.plugin.activerecord.DbKit;
import org.sxb.plugin.activerecord.DbPro;
import org.sxb.plugin.activerecord.IAtom;

/**
 * TxByActionKeys
 */
public class TxByActionKeys implements Interceptor {
	
	private Set<String> actionKeySet = new HashSet<String>();
	
	public TxByActionKeys(String... actionKeys) {
		if (actionKeys == null || actionKeys.length == 0)
			throw new IllegalArgumentException("actionKeys can not be blank.");
		
		for (String actionKey : actionKeys)
			actionKeySet.add(actionKey.trim());
	}
	
	public void intercept(final Invocation inv) {
		Config config = Tx.getConfigWithTxConfig(inv);
		if (config == null)
			config = DbKit.getConfig();
		
		if (actionKeySet.contains(inv.getActionKey())) {
			DbPro.use(config.getName()).tx(new IAtom(){
				public boolean run() throws SQLException {
					inv.invoke();
					return true;
				}});
		}
		else {
			inv.invoke();
		}
	}
}







