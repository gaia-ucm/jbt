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

import jbt.tools.bteditor.model.BTNode;

/**
 * This class manages the copy and paste mechanism implemented by the
 * {@link BTEditor} class.
 * <p>
 * This class offers two main methods, {@link #copy(BTNode)} and
 * {@link #paste()}. When <code>copy()</code> gets called, the
 * BTEditorCopyAndPasteManager creates a copy (by cloning) of the BTNode to
 * copy. From then on, whenever <code>paste()</code> is called, a new copy (by
 * cloning) of it is returned.
 * <p>
 * This is a singleton class, so there is only one instance of
 * BTEditorCopyAndPasteManager.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTEditorCopyAndPasteManager {
	/** The only instance of BTEditorCopyAndPasteManager. */
	private static BTEditorCopyAndPasteManager instance;
	/** The BTNode that has been copied. */
	private BTNode copiedBranch;

	/**
	 * Copies the node <code>branchToCopy</code>. This makes the
	 * BTEditorCopyAndPasteManager store a copy (by calling
	 * {@link BTNode#clone()}) of <code>branchToCopy</code>. From then on,
	 * {@link #paste()} will return a copy of the copy created by this method.
	 */
	public void copy(BTNode branchToCopy) {
		this.copiedBranch = branchToCopy.clone();
	}

	/**
	 * This method can be called only after {@link #copy(BTNode)} has been
	 * called. This method returns a copy (by invoking {@link BTNode#clone()})
	 * of the node that was copied by {@link #copy(BTNode)}.
	 */
	public BTNode paste() {
		return this.copiedBranch.clone();
	}

	/**
	 * Returns the only instance of BTEditorCopyAndPasteManager.
	 */
	public static BTEditorCopyAndPasteManager getInstance() {
		if (instance == null) {
			instance = new BTEditorCopyAndPasteManager();
		}

		return instance;
	}

	/**
	 * Returns true if a {@link #copy(BTNode)} operation has been made, or false
	 * if not.
	 */
	public boolean hasCopy() {
		return this.copiedBranch != null;
	}

	/**
	 * Private constructor to force the singleton pattern.
	 */
	private BTEditorCopyAndPasteManager() {
	}
}
