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
package jbt.tools.bteditor.event;

import java.util.EventObject;

import jbt.tools.bteditor.model.BT;

/**
 * Event issued when a {@link BT} is modified.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class TreeModifiedEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private BT tree;

	/**
	 * Constructor. Must specify the tree that has been modified.
	 */
	public TreeModifiedEvent(Object source, BT tree) {
		super(source);
		this.tree = tree;
	}

	/**
	 * Returns the tree that has been modified.
	 */
	public BT getTree() {
		return this.tree;
	}
}
