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
package jbt.execution.task.leaf;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.ModelLeaf;

/**
 * Base class for all the ExecutionTask classes that are able to run leaf tasks,
 * that is, classes that inherit from ModelLeaf.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ExecutionLeaf extends ExecutionTask {
	/**
	 * Constructs an ExecutionLeaf to run a specific ModelLeaf.
	 * 
	 * @param modelTask
	 *            the ModelLeaf to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionLeaf.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionLeaf(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelLeaf)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelLeaf.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Does nothing by default, since a leaf task has no children.
	 * 
	 * @see jbt.execution.core.ExecutionTask#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public void statusChanged(TaskEvent e) {}
}
