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
package jbt.execution.task.leaf.condition;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.leaf.ExecutionLeaf;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.condition.ModelCondition;

/**
 * ExecutionCondition is the base class of all of the class that are able to run
 * conditions in the game (that is, subclasses of ModelCondition).
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ExecutionCondition extends ExecutionLeaf {
	/**
	 * Constructs an ExecutionCondition that knows how to run a ModelCondition.
	 * 
	 * @param modelTask
	 *            the ModelCondition to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionCondition.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionCondition(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelCondition)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelCondition.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}
}
