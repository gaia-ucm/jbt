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

import jbt.model.core.ModelTask;

/**
 * A ModelComposite task is a task with several children, whose evaluation
 * depends on the evaluation of its children.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ModelComposite extends ModelTask {
	/**
	 * Constructor.
	 * <p>
	 * Constructs a ModelComposite with some children. A ModelComposite must
	 * have at least one child.
	 * 
	 * @param guard
	 *            the guard of the ModelComposite.
	 * @param children
	 *            the list of children. Must have at least one element.
	 */
	public ModelComposite(ModelTask guard, ModelTask... children) {
		super(guard, children);
		if (children.length == 0) {
			throw new IllegalArgumentException("The list of children cannot be empty");
		}
	}
}
