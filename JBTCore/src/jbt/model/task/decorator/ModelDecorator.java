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

import jbt.model.core.ModelTask;

/**
 * This class represents a decorator of a task. A decorator is a task with only
 * one child, whose behavior it modifies. A decorator is used in situations in
 * which we want to execute a particular task but in a little different way.
 * <p>
 * Typical examples of decorators are:
 * 
 * <ul>
 * <li>Filters: they decide whether the child task can continue running or not.
 * Some examples of filters are:
 * <ul>
 * <li>Limit filter: which limits the number of times a task can execute.
 * <li>Until fail filter: which repeats a task until it fails.
 * </ul>
 * <li>Inverter: inverts the status code of a task.
 * <li>Semaphore guard: they are associated to resources. If the resource is
 * currently being used by another task, the new task cannot start running, so
 * it fails.
 * </ul>
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ModelDecorator extends ModelTask {
	/**
	 * Constructor.
	 * <p>
	 * Constructs a ModelDecorator with one child.
	 * 
	 * @param guard
	 *            the guard of the ModelDecorator. which may be null.
	 * @param child
	 *            the child of the ModelDecorator.
	 */
	public ModelDecorator(ModelTask guard, ModelTask child) {
		super(guard, child);
	}

	/**
	 * Returns the child of this decorator.
	 * 
	 * @return the child of this decorator.
	 */
	public ModelTask getChild() {
		return this.getChildren().get(0);
	}
}
