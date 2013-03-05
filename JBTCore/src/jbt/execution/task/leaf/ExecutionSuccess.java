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
import jbt.execution.core.ITaskState;
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.ModelSuccess;

/**
 * An ExecutionSuccess is the ExecutionTask that knows how to run a
 * ModelSuccess.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionSuccess extends ExecutionLeaf {
	/**
	 * Constructs an ExecutionSuccess that knows how to run a ModelSuccess.
	 * 
	 * @param modelTask
	 *            the ModelSuccess to run.
	 * @param executor
	 *            the BTExecutor managing this ExecutionSuccess.
	 * @param parent
	 *            the parent ExecutionTask.
	 */
	public ExecutionSuccess(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelSuccess)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelSuccess.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		this.getExecutor().requestInsertionIntoList(BTExecutorList.TICKABLE, this);
	}

	/**
	 * Returns {@link Status#SUCCESS}.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		return Status.SUCCESS;
	}

	/**
	 * Returns null.
	 * 
	 * @see jbt.execution.core.ExecutionTask#storeState()
	 */
	protected ITaskState storeState() {
		return null;
	}

	/**
	 * Returns null.
	 * 
	 * @see jbt.execution.core.ExecutionTask#storeTerminationState()
	 */
	protected ITaskState storeTerminationState() {
		return null;
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(jbt.execution.core.ITaskState)
	 */
	protected void restoreState(ITaskState state) {
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
	}
}
