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
package jbt.model.task.composite;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.composite.ExecutionDynamicPriorityList;
import jbt.model.core.ModelTask;

/**
 * This class represents a task with one or more children, only one being
 * evaluated.
 * <p>
 * A ModelDynamicPriorityList has a current active child, which is the task that
 * is being evaluated. The very first time the ModelDynamicPriorityList is
 * spawned, the active child is set to the left most task whose guard is
 * evaluated to true. However, the current active task may change when the task
 * is ticked, according to the guards of the other tasks: if there is a task to
 * the left of the current active task whose guard is true, the latter is
 * terminated, and the new current active task is set to the former. In case
 * there are several tasks to the left of the current active task whose guards
 * are evaluated to true, the current active task will be the left most one.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelDynamicPriorityList extends ModelComposite {
	/**
	 * Creates a ModelDynamicPriorityList task with a guard, and a list of
	 * children to run. A ModelDynamicPriorityList must have at least one child.
	 * 
	 * @param guard
	 *            the guard, which may be null.
	 * @param children
	 *            the list of children. Must have at least one element.
	 */
	public ModelDynamicPriorityList(ModelTask guard, ModelTask... children) {
		super(guard, children);
	}

	/**
	 * Returns an ExecutionDynamicPriorityList that is able to run this
	 * ModelDynamicPriorityList.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionDynamicPriorityList(this, executor, parent);
	}
}
