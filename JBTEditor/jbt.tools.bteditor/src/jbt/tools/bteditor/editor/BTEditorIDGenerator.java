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
package jbt.tools.bteditor.editor;

/**
 * A singleton class that generates IDs for BTEditor objects. Every new BTEditor
 * has an unique ID which is represented as a long value.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTEditorIDGenerator {
	/** The only instance of this class. */
	private static BTEditorIDGenerator instance;
	/** Counter for generated IDs. */
	private long counter = 0;

	/**
	 * Returns the only instance of BTEditorIDGenerator.
	 */
	public static BTEditorIDGenerator getInstance() {
		if (instance == null) {
			instance = new BTEditorIDGenerator();
		}
		return instance;
	}

	/**
	 * Returns the next BTEditor ID.
	 */
	public long getNextID() {
		return this.counter++;
	}

	/**
	 * Private constructor to force the singleton pattern.
	 */
	private BTEditorIDGenerator() {
	}
}
