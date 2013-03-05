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

import java.util.List;

import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.util.Utilities;
import jbt.tools.bteditor.viewers.NodeInfoViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * ViewPart that shows the information of the selected node of the currently
 * active editor. Internally, this view just stores a NodeInfoViewer.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NodeInfo extends ViewPart {
	public static String ID = "jbt.tools.bteditor.views.NodeInfo";
	private NodeInfoViewer nodeInfoViewer;

	public void createPartControl(Composite parent) {
		this.nodeInfoViewer = new NodeInfoViewer(parent, SWT.NONE);

		/* Initialize view's content with the currently selected node. */
		BTEditor activeBTEditor = Utilities.getActiveBTEditor();

		if (activeBTEditor != null) {
			List<BTNode> selectedElements = activeBTEditor.getSelectedElements();
			if (selectedElements.size() != 0) {
				this.nodeInfoViewer.setNode(selectedElements.get(0));
			}
		}
	}

	public void setFocus() {
	}

	/**
	 * Sets the node whose information is being displayed.
	 */
	public void setNode(BTNode node) {
		this.nodeInfoViewer.setNode(node);
	}
}
