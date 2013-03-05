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
package jbt.model.task.decorator;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.decorator.ExecutionUntilFail;
import jbt.model.core.ModelTask;

/**
 * The ModelUntilFail class represents a decorator used to run a task as long as
 * it does not fail.
 * <p>
 * ModelUntilFail just keeps executing its child task as long as it does not
 * fail. When the child task fails, ModelUntilFail returns
 * {@link Status#SUCCESS}. Otherwise it returns {@link Status#RUNNING}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelUntilFail extends ModelDecorator {
	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the ModelUntilFail, which may be null.
	 * @param child
	 *            the task that will be run until it fails.
	 */
	public ModelUntilFail(ModelTask guard, ModelTask child) {
		super(guard, child);
	}

	/**
	 * Returns an ExecutionUntilFail that knows how to run this ModelUntilFail.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionUntilFail(this, executor, parent);
	}
}
