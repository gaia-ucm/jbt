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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jbt.util.Pair;

/**
 * The TaskStateFactory implements the simple factory pattern, and allows
 * clients of the framework to create instances of {@link ITaskState} objects.
 * The methods provided by this factory allows the client to specify the set of
 * variables that the task state will contain.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class TaskStateFactory {
	/**
	 * Creates an ITaskState that contains the set of variables specified by
	 * <code>variables</code>. Each variable is a Pair whose first element is
	 * the variable's name and the second element is its value.
	 * 
	 * @param variables
	 *            the list of variables that the ITaskState will contain.
	 * @return an ITaskState that contains the set of variables in
	 *         <code>variables</code>.
	 */
	public static ITaskState createTaskState(List<Pair<String, Object>> variables) {
		TaskState taskState = new TaskState();

		for (Pair<String, Object> variable : variables) {
			taskState.setStateVariable(variable.getFirst(), variable.getSecond());
		}

		return taskState;
	}

	/**
	 * Creates an ITaskState that contains the set of variables specified by
	 * <code>variables</code>. Variables are stored in a Map whose keys are
	 * variables' names and whose values are the values of the variables.
	 * 
	 * @param variables
	 *            the list of variables that the ITaskState will contain.
	 * @return an ITaskState that contains the set of variables in
	 *         <code>variables</code>.
	 */
	public static ITaskState createTaskState(Map<String, Object> variables) {
		TaskState taskState = new TaskState();

		for (Entry<String, Object> variable : variables.entrySet()) {
			taskState.setStateVariable(variable.getKey(), variable.getValue());
		}

		return taskState;
	}
}
