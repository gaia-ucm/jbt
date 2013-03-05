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

import jbt.model.core.ModelTask;

/**
 * Base class for all the tasks that have no children.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ModelLeaf extends ModelTask {
	/**
	 * Constructs a ModelLeaf with a guard.
	 * 
	 * @param guard
	 *            the guard, which may be null.
	 */
	public ModelLeaf(ModelTask guard) {
		super(guard);
	}
}
