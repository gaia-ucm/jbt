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
package jbt.execution.task.decorator;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelDecorator;

/**
 * Base class for all the ExecutionTask subclasses that are able to run
 * decorator tasks (that is, classes that inherit from ModelDecorator).
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ExecutionDecorator extends ExecutionTask {
	/**
	 * Creates an ExecutionDecorator that is able to run a particular
	 * ModelDecorator task.
	 * 
	 * @param modelTask
	 *            the ModelDecorator task to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionDecorator.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionDecorator(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelDecorator)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelDecorator.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}
}
