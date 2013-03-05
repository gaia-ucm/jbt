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
import jbt.execution.task.composite.ExecutionStaticPriorityList;
import jbt.model.core.ModelTask;

/**
 * This class represents a task with one or more children, only one being
 * evaluated.
 * <p>
 * A ModelStaticPriorityList has a current active child, which is the task that
 * is being evaluated. The very first time the ModelStaticPriorityList is
 * spawned, the active child is set to the left most task whose guard is
 * evaluated to true. From then on, that child will run as normal, and the
 * ModelStaticPriorityList will finish as soon as its child finishes.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelStaticPriorityList extends ModelComposite {
	/**
	 * Creates a ModelStaticPriorityList task with a guard, and a list of
	 * children to run. A ModelStaticPriorityList must have at least one child.
	 * 
	 * @param guard
	 *            the guard, which may be null.
	 * @param children
	 *            the list of children. Must have at least one element.
	 */
	public ModelStaticPriorityList(ModelTask guard, ModelTask... children) {
		super(guard, children);
	}

	/**
	 * Returns an ExecutionStaticPriorityList that is able to run this
	 * ModelStaticPriorityList.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionStaticPriorityList(this, executor, parent);
	}
}
