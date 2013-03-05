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
import jbt.execution.task.composite.ExecutionParallel;
import jbt.model.core.ModelTask;

/**
 * ModelParallel is a task that runs all its children simultaneously. A
 * ModelParallel is constantly checking the evolution of its children.
 * <p>
 * The parallel task has a policy that defines the way it behaves. There are to
 * policies for parallel:
 * <ul>
 * <li>{@link ParallelPolicy#SEQUENCE_POLICY}: meaning the parallel behaves like
 * a sequence task, that is, it fails as soon as one of its children fail, and
 * it only succeed if all of its children succeed. Otherwise it is running.
 * <li>{@link ParallelPolicy#SELECTOR_POLICY}: meaning the parallel behaves like
 * a selector task, that is, if succeeds as soon as one of its children succeed,
 * and it only fails of all of its children fail. Otherwise it is running.
 * </ul>
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelParallel extends ModelComposite {
	/** Policy of this ModelParallel task. */
	private ParallelPolicy policy;

	/**
	 * Enum defining the different policies for a parallel task (ModelParallel):
	 * <ul>
	 * <li>{@link ParallelPolicy#SEQUENCE_POLICY}: means the parallel behaves
	 * like a sequence task, that is, it fails as soon as one of its children
	 * fail, and it only succeed if all of its children succeed. Otherwise it is
	 * running.
	 * <li>{@link ParallelPolicy#SELECTOR_POLICY}: means the parallel behaves
	 * like a selector task, that is, if succeeds as soon as one of its children
	 * succeed, and it only fails of all of its children fail. Otherwise it is
	 * running.
	 * </ul>
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static enum ParallelPolicy {
		/**
		 * Policy meaning that the parallel behaves like a sequence task, that
		 * is, it fails as soon as one of its children fail, and it only succeed
		 * if all of its children succeed.
		 */
		SEQUENCE_POLICY,
		/**
		 * Policy meaning the parallel behaves like a selector task, that is, if
		 * succeeds as soon as one of its children succeed, and it only fails of
		 * all of its children fail.
		 */
		SELECTOR_POLICY
	}

	/**
	 * Creates a ModelParallel task with a guard, a policy and a list of
	 * children to run. A ModelParallel must have at least one child.
	 * 
	 * @param guard
	 *            the guard, which may be null.
	 * @param policy
	 *            the policy for the ModelParallel.
	 * @param children
	 *            the list of children. Must have at least one element.
	 */
	public ModelParallel(ModelTask guard, ParallelPolicy policy, ModelTask... children) {
		super(guard, children);
		this.policy = policy;
	}

	/**
	 * Returns the policy of this ModelParallel.
	 * 
	 * @return the policy of this ModelParallel.
	 */
	public ParallelPolicy getPolicy() {
		return this.policy;
	}

	/**
	 * Returns an ExecutionParallel that can run this ModelParallel.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionParallel(this, executor, parent);
	}
}
