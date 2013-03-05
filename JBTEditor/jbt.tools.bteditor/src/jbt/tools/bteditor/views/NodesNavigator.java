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
package jbt.tools.bteditor.views;

import jbt.tools.bteditor.NodesLoader;
import jbt.tools.bteditor.model.ConceptualNodesTree;
import jbt.tools.bteditor.viewers.ConceptualNodesTreeViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * This is the ViewPart that shows the list of available nodes, that is, the
 * list of nodes that can be used to build behaviour trees.
 * <p>
 * Internally, this view just contains a {@link ConceptualNodesTreeViewer} that shows the
 * list of nodes that are currently loaded into the application (see
 * {@link NodesLoader}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NodesNavigator extends ViewPart {
	public static String ID = "jbt.tools.bteditor.views.NodesNavigator";
	private ConceptualNodesTreeViewer viewer;

	public void createPartControl(Composite parent) {
		/* Load both standard and non-standard nodes. */
		this.viewer = new ConceptualNodesTreeViewer(parent, SWT.NONE);

		this.viewer.addTree(NodesLoader.getStandardNodesTree());

		for (ConceptualNodesTree tree : NodesLoader.getNonStandardNodesTrees()) {
			this.viewer.addTree(tree);
		}
	}

	/**
	 * Adds a {@link ConceptualNodesTree} to the list of trees displayed by this
	 * view. All the nodes of the tree will be displayed.
	 */
	public void addTree(ConceptualNodesTree tree) {
		viewer.addTree(tree);
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
}
