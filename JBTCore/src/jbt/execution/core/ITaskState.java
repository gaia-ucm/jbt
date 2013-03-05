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

/**
 * The ITaskState interface represents the persistent state of a task in a
 * behaviour tree. This state is represented as a set of variables with name and
 * value.
 * <p>
 * Some tasks in BTs are persistent in the sense that, after finishing, if they
 * are spawned again, they remember past information. Take for example the
 * "limit" task. A "limit" task allows to run its child node only a certain
 * number of times (for example, 5). After being spawned, it has to remember how
 * many times it has been run so far, so that, once the threshold is exceeded,
 * it fails.
 * <p>
 * This interface represents the common functionality for classes that represent
 * the persistent state of a task. It just defines a method for retrieving the
 * value of a variable of the task's state. They way the task's state is
 * populated is not defined.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public interface ITaskState {
	/**
	 * Returns the value of a variable whose name is <code>name</code>, or null
	 * if it is not found.
	 * 
	 * @param name
	 *            the name of the variable to retrieve.
	 * 
	 * @return the value of a variable whose name is <code>name</code>, or null
	 *         if it does not exist.
	 */
	public Object getStateVariable(String name);
}
