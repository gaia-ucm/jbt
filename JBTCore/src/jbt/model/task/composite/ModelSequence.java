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
import jbt.execution.task.composite.ExecutionSequence;
import jbt.model.core.ModelTask;

/**
 * A ModeSequence is a task with one or more children which are evaluated
 * sequentially.
 * <p>
 * A ModeSequence has an active child, which is the child task currently being
 * evaluated. If the execution of the current child finishes successfully, the
 * next child of the sequence is spawned and evaluated. However, if the
 * execution of the currently active child ends in failure, the whole
 * ModeSequence also fails.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelSequence extends ModelComposite {
	/**
	 * Constructor.
	 * <p>
	 * Constructs a ModeSequence with some children. A ModeSequence must have at
	 * least one child.
	 * 
	 * @param guard
	 *            the guard of the ModeSequence, which may be null.
	 * @param children
	 *            the list of children. Must have at least one element.
	 */
	public ModelSequence(ModelTask guard, ModelTask... children) {
		super(guard, children);
	}

	/**
	 * Returns an ExecutionSequence that can run this ModelSequence.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionSequence(this, executor, parent);
	}
}
