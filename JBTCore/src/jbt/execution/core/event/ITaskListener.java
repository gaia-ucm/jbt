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
package jbt.execution.core.event;

import jbt.execution.core.ExecutionTask;

/**
 * Interface for an entity that is able to receive events from tasks (
 * {@link ExecutionTask}) whose status has changed in a relevant way.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public interface ITaskListener {
	/**
	 * Method called when an important change in the status of a task has taken
	 * place.
	 * 
	 * @param e
	 *            the TaskEvent with all the information about the change in the
	 *            status of the task.
	 */
	public void statusChanged(TaskEvent e);
}
