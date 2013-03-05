/*
 * Copyright (C) 2012 Ricardo Juan Palma Durán
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
package jbt.execution.core;

import jbt.model.core.ModelTask;

/**
 * The BTExecutorFactory implements the simple factory pattern, and allows
 * clients of the framework to create instances of {@link IBTExecutor} objects
 * that will run specific behaviour trees.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTExecutorFactory {
	/**
	 * Creates an IBTExecutor that is able to run a specific behaviour tree. The
	 * input context is also specified.
	 * 
	 * @param treeToRun
	 *            the behaviour tree that the returned IBTExecutor will run,
	 * @param context
	 *            the input context to be used by the behaviour tree.
	 * @return an IBTExecutor to run the tree <code>treeToRun</code>.
	 */
	public static IBTExecutor createBTExecutor(ModelTask treeToRun,
			IContext context) {
		return new BTExecutor(treeToRun, context);
	}

	/**
	 * Creates an IBTExecutor that is able to run a specific behaviour tree. A
	 * new empty context is created for the tree.
	 * 
	 * @param treeToRun
	 *            the behaviour tree that the returned IBTExecutor will run,
	 * @return an IBTExecutor to run the tree <code>treeToRun</code>.
	 */
	public static IBTExecutor createBTExecutor(ModelTask treeToRun) {
		return new BTExecutor(treeToRun);
	}
}
