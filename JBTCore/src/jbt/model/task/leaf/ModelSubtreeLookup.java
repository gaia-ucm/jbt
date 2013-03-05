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
package jbt.model.task.leaf;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.leaf.ExecutionSubtreeLookup;
import jbt.model.core.ModelTask;

/**
 * A ModelSubtreeLookup is a leaf node that emulates the behaviour of another
 * behaviour tree.
 * <p>
 * One of the key features of behaviour trees is that they can be reused in many
 * places. This reusability is implemented through the ModelSubreeLookup task.
 * When a tree <i>A</i> must be reused within another tree <i>B</i>, this task
 * is used to retrieve <i>A</i> and use it within <i>B</i>. Trees are indexed by
 * names, so this task needs the name of the tree that it will emulate.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelSubtreeLookup extends ModelLeaf {
	/** The name of the tree that this task is going to emulate. */
	private String treeName;

	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the task, which may be null.
	 * @param treeName
	 *            the name of the tree that this task is going to emulate.
	 */
	public ModelSubtreeLookup(ModelTask guard, String treeName) {
		super(guard);
		this.treeName = treeName;
	}

	/**
	 * Returns an ExecutionSubtreeLookup that is able to run this
	 * ModelSubtreeLookup.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionSubtreeLookup(this, executor, parent);
	}

	/**
	 * Returns the name of the tree that this task is going to emulate.
	 * 
	 * @return the name of the tree that this task is going to emulate.
	 */
	public String getTreeName() {
		return this.treeName;
	}
}
