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
package jbt.model.task.leaf.condition;

import jbt.model.core.ModelTask;
import jbt.model.task.leaf.ModelLeaf;

/**
 * Class representing an abstract condition to be tested within the game.
 * Conditions are tasks with no children (that is, they are leaves in the
 * behavior tree) and with no connection to any other task in the tree.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ModelCondition extends ModelLeaf {
	/**
	 * Constructs a ModelCondition.
	 * 
	 * @param guard
	 *            the guard of the ModelCondition, which may be null.
	 */
	public ModelCondition(ModelTask guard) {
		super(guard);
	}
}
