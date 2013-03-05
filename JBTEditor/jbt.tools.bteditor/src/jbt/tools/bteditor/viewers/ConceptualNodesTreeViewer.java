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
package jbt.tools.bteditor.viewers;

import java.util.List;
import java.util.Vector;

import jbt.tools.bteditor.ApplicationIcons;
import jbt.tools.bteditor.model.ConceptualNodesTree;
import jbt.tools.bteditor.model.ConceptualNodesTree.CategoryItem;
import jbt.tools.bteditor.model.ConceptualNodesTree.ConceptualBTNodeItem;
import jbt.tools.bteditor.model.ConceptualNodesTree.NodesTreeItem;
import jbt.tools.bteditor.util.IconsPaths;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * ConceptualNodesTreeViewer is a Composite that is able to display, in a
 * tree-like fashion, several {@link ConceptualNodesTree}s.
 * <p>
 * It also defines a drag and drop mechanism for dragging nodes from the view so
 * that they can be dropped anywhere else in the application. This drag and drop
 * mechanism is compatible with the transfer type
 * {@link ConceptualBTNodeTransfer}.
 * <p>
 * Double clicking a category node expands -or collapses- that category.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ConceptualNodesTreeViewer extends Composite {
	/** The TreeViewer that is internally used for displaying the trees. */
	private TreeViewer treeViewer;
	/** The trees thar are being displayed. */
	private List<ConceptualNodesTree> trees;

	/**
	 * Constructor.
	 */
	public ConceptualNodesTreeViewer(Composite parent, int style) {
		super(parent, style);
		this.trees = new Vector<ConceptualNodesTree>();
		this.setLayout(new FillLayout());

		this.treeViewer = new TreeViewer(this, SWT.SINGLE);
		Tree treeWidget = (Tree) this.treeViewer.getControl();
		// treeWidget.setLinesVisible(true);

		this.treeViewer.setContentProvider(new BTContentProvider());
		this.treeViewer.setLabelProvider(new BTLabelProvider());
		this.treeViewer.setInput(this.trees);

		/* Drag and drop support. */
		Transfer[] transfers = new Transfer[] { ConceptualBTNodeTransfer.getInstance() };
		this.treeViewer.addDragSupport(DND.DROP_MOVE, transfers, new NodesTreeViewerDragListener());

		/* Double click listener for expanding and collapsing categories. */
		this.treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object node = ((StructuredSelection) event.getSelection()).getFirstElement();
				if (node instanceof CategoryItem) {
					if (treeViewer.getExpandedState(node)) {
						treeViewer.collapseToLevel(node, 1);
					} else {
						treeViewer.expandToLevel(node, 1);
					}
				}
			}
		});
	}

	/**
	 * Adds a new {@link ConceptualNodesTree} that will be displayed together
	 * with all the trees that were being displayed.
	 */
	public void addTree(ConceptualNodesTree tree) {
		this.trees.add(tree);
		this.treeViewer.refresh();
	}

	/**
	 * Drag source listener of the tree. It is compatible with
	 * {@link ConceptualBTNodeTransfer}.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class NodesTreeViewerDragListener implements DragSourceListener {
		public void dragFinished(DragSourceEvent event) {
		}

		public void dragSetData(DragSourceEvent event) {
			if (ConceptualBTNodeTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = ((ConceptualBTNodeItem) ((IStructuredSelection) treeViewer
						.getSelection()).getFirstElement()).getNodeModel();
			}
		}

		public void dragStart(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

			if (!selection.isEmpty()) {
				if (selection.getFirstElement() instanceof ConceptualBTNodeItem) {
					ConceptualBTNodeItem selectedNode = (ConceptualBTNodeItem) selection
							.getFirstElement();
					event.doit = true;
					return;
				}
			}

			event.doit = false;
		}
	}

	/**
	 * Contente provider of the tree.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class BTContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof CategoryItem) {
				CategoryItem category = (CategoryItem) parentElement;
				return category.getChildren().toArray();
			} else {
				return new Object[] {};
			}
		}

		public Object getParent(Object element) {
			return ((NodesTreeItem) element).getParent();
		}

		public boolean hasChildren(Object element) {
			if (element instanceof CategoryItem) {
				CategoryItem category = (CategoryItem) element;
				return category.getNumChildren() > 0;
			} else {
				return false;
			}
		}

		public Object[] getElements(Object inputElement) {
			List<ConceptualNodesTree> trees = (List<ConceptualNodesTree>) inputElement;
			Object[] elements = new Object[trees.size()];
			for (int i = 0; i < trees.size(); i++) {
				ConceptualNodesTree tree = trees.get(i);
				elements[i] = tree.getRoots().get(0);
			}
			return elements;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Label provider of the tree.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class BTLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {
			if (element instanceof CategoryItem) {
				return ApplicationIcons.getIcon(IconsPaths.CATEGORY);
			} else {
				ConceptualBTNodeItem nodeItem = (ConceptualBTNodeItem) element;
				return ApplicationIcons.getIcon(nodeItem.getNodeModel().getIcon());
			}
		}

		public String getText(Object element) {
			return ((NodesTreeItem) element).getName();
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
}
