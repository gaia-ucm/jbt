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
package jbt.execution.task.leaf.action;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.leaf.ExecutionLeaf;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.action.ModelAction;

/**
 * ExecutionAction is the base class of all of the class that are able to run
 * actions in the game (that is, subclasses of ModelAction).
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ExecutionAction extends ExecutionLeaf {
	/**
	 * Constructs an ExecutionAction that knows how to run a ModelAction.
	 * 
	 * @param modelTask
	 *            the ModelAction to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionAction.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionAction(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelAction)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelAction.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}
}
