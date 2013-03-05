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

import java.awt.Checkbox;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jbt.tools.bteditor.ApplicationIcons;
import jbt.tools.bteditor.BTXMLManager;
import jbt.tools.bteditor.NodesLoader;
import jbt.tools.bteditor.actions.SaveBTAction;
import jbt.tools.bteditor.actions.SaveBTAsAction;
import jbt.tools.bteditor.event.ITreeModifierListener;
import jbt.tools.bteditor.event.TreeModifiedEvent;
import jbt.tools.bteditor.model.BT;
import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.model.BTNode.Identifier;
import jbt.tools.bteditor.model.BTNode.Parameter;
import jbt.tools.bteditor.model.ConceptualBTNode;
import jbt.tools.bteditor.model.ConceptualBTNode.NodeInternalType;
import jbt.tools.bteditor.model.ConceptualBTNode.ParameterType;
import jbt.tools.bteditor.util.IconsPaths;
import jbt.tools.bteditor.util.OverlayImageIcon;
import jbt.tools.bteditor.util.Pair;
import jbt.tools.bteditor.util.StandardDialogs;
import jbt.tools.bteditor.util.Utilities;
import jbt.tools.bteditor.viewers.BTNodeIndentifierTransfer;
import jbt.tools.bteditor.viewers.ConceptualBTNodeTransfer;
import jbt.tools.bteditor.views.NodeInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

/**
 * Editor that is used for editing behavior trees ({@link BT}). This editor just
 * shows the tree in a tree-like way, letting the user modify a behaviour tree.
 * <p>
 * This editor can be used to insert nodes into the tree, delete them, and edit
 * their parameters. Nodes are inserted via a drag and drop mechanism.
 * <p>
 * For insertion purposes, this class is compatible with the
 * {@link ConceptualBTNodeTransfer} class, so a new empty node is inserted as a
 * child of the target of a drop operation when the underlying data is
 * compatible with {@link ConceptualBTNodeTransfer}.
 * <p>
 * Also, nodes of the tree can be moved around by using the drag and drop
 * mechanism. This class is compatible with the
 * {@link BTNodeIndentifierTransfer} class, so a dragged node is inserted as a
 * sibling of the target of the drop operation.
 * <p>
 * A context menu lets the user delete nodes (nodes can be deleted also by using
 * the delete and back space keys) and edit nodes' guards. Copying and pasting
 * nodes from the BTEditor into another editor (or the same one) is supported
 * through the context menu and global actions (Ctrl+C and Ctrl+V). However, it
 * should be noted that dragging from a BTEditor and dropping onto another
 * editor is not still supported.
 * <p>
 * For those nodes that have parameters, their value can be set by double
 * clicking on them.
 * <p>
 * This editor implements the {@link ITreeModifierListener} in order to change
 * the dirty status of the editor when the underlying tree is modified.
 * <p>
 * An important feature of the BTEditor is that it can be used to also edit the
 * guard of a node of another tree. BTEditors are opened with
 * {@link BTEditorInput} objects. BTEditorInput can specify where the tree that
 * the BTEditor edits comes from, and one of the possible options is to tell the
 * BTEditorInput that the tree comes from a node's guard. In such case, the
 * BTEditor has pretty much the same functionality as a normal BTEditor, with
 * small differences.
 * <p>
 * For instance, the way it responds to the <i>save</i> action is different.
 * When the BTEditor that contains a guard is saved, the guard is not saved into
 * a file, but stored into the node whose guard is being edited. Also, the root
 * node of a guard does not have a name, unlike that of normal tree.
 * <p>
 * A BTEditor's behaviour tree can therefore have guards being edited at any
 * time. In such case, if the editor is closed, all the editors editing guards
 * of the tree will be dissociated from the tree, and as a result, they will not
 * be considered guards any more (just a normal behaviour tree).
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTEditor extends EditorPart implements ITreeModifierListener {
	public static final String ID = "jbt.tools.bteditor.editor.BTEditor";

	/** The viewer that is being used to display the tree. */
	private TreeViewer viewer;
	/** The tree that the BTEditor is managing. */
	private BT tree;
	/** Flag that indicates whether the tree is dirty or not. */
	private boolean dirty;
	/**
	 * Red color for cells containing erroneous items.
	 */
	private Color ERROR_COLOR = Utilities.getDisplay().getSystemColor(SWT.COLOR_RED);
	/**
	 * List of all the editors that are currently editing the guards of this
	 * tree's nodes. BTEditors are indexed by the BTNode whose guard is being
	 * edited.
	 */
	private Map<BTNode, BTEditor> openGuardEditors;
	/**
	 * In case this BTEditor is editing a guard, this variable stores the
	 * behaviour tree that contains the node whose guard is being edited (
	 * {@link #guardNode}).
	 */
	private BT guardTree;
	/**
	 * In case this BTEditor is editing a guard, this variable stores the BTNode
	 * whose guard is being edited.
	 */
	private BTNode guardNode;

	/**
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		/* First, check if the tree has no structural errors. */
		if (!checkTree()) {
			StandardDialogs.errorDialog("Tree not saved",
					"Errors were detected while validating the tree");
			monitor.setCanceled(true);
			return;
		}

		/*
		 * The save the tree.
		 */
		try {
			if (this.isFromFile()) {
				/* If the tree comes from a file, save the tree into a file. */
				new SaveBTAction(this.tree, ((BTEditorInput) getEditorInput()).getTreeName()).run();
				this.dirty = false;
				firePropertyChange(EditorPart.PROP_DIRTY);
			} else if (this.isFromGuard()) {
				/*
				 * If the tree comes from a guard, then set the tree as a guard
				 * of the "this.guardNode". Note that we set a clone of the
				 * guard, not the original one. By doing so, the guard of the
				 * original node will not be modified even if this editor's tree
				 * is modified.
				 */
				BTNode guard = this.tree.getRoot().getChildren().get(0);
				if (guard != null) {
					this.guardNode.setGuard(guard.clone());
					this.guardTree.fireTreeChanged(this);
					BTEditorInput editorInput = (BTEditorInput) this.getEditorInput();
					Utilities.getBTEditor(Long.parseLong(editorInput.getTreeName().split(
							File.pathSeparator)[0])).viewer.refresh();
				}
				this.dirty = false;
				firePropertyChange(EditorPart.PROP_DIRTY);
			} else {
				/* Otherwise, do a save as. */
				doSaveAs();
			}
		} catch (Exception e) {
			StandardDialogs.exceptionDialog("Tree not saved",
					"Errors were detected while saving the tree", e);
			monitor.setCanceled(true);
		}
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		/* First, check if the tree has no structural errors. */
		if (!checkTree()) {
			StandardDialogs.errorDialog("Tree not saved",
					"Errors were detected while validating the tree");
			return;
		}

		SaveBTAsAction action = new SaveBTAsAction(this.tree, this.getEditorInput().getName());

		try {
			action.run();
			if (action.getSelectedFile() != null) {
				BTEditorInput editorInput = (BTEditorInput) getEditorInput();
				editorInput.setTreeName(action.getSelectedFile());
				this.dirty = false;
				setIsFromFile(true);

				/*
				 * If the tree comes from a guard, it must be dissociated from
				 * its original tree. From then on, this BTEditor will be
				 * managed as a normal BTEditor.
				 */
				if (isFromGuard()) {
					dissociateFromParentTree();
				}

				setPartName(editorInput.getName());

				firePropertyChange(EditorPart.PROP_DIRTY);
				firePropertyChange(PROP_TITLE);
			}
		} catch (Exception e) {
			StandardDialogs.exceptionDialog("Error saving the tree",
					"There was an error when saving the tree", e);
		}
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof BTEditorInput)) {
			throw new PartInitException("Illegal IEditorInput. Must be "
					+ BTEditorInput.class.getCanonicalName());
		}

		setSite(site);
		setInputWithNotify(input);
		BTEditorInput editorInput = (BTEditorInput) input;
		setPartName(input.getName());
		this.openGuardEditors = new Hashtable<BTNode, BTEditor>();

		/* Set the IPartListener that will handle close events. */
		IPartService partService = (IPartService) this.getSite().getService(IPartService.class);
		partService.addPartListener(new BTEditorPartListener());

		if (editorInput.isFromFile()) {
			/* If the tree comes from a file, load the file. */
			try {
				this.tree = BTXMLManager.load(editorInput.getTreeName());
				this.tree.addTreeModifiedListener(this);
				this.dirty = false;
			} catch (IOException e) {
				throw new PartInitException("There were errors while loading the tree: "
						+ e.getMessage(), e);
			}
		} else if (editorInput.isFromGuard()) {
			/*
			 * If the tree comes from a guard, we have to construct a new tree
			 * whose root is the guard.
			 */
			this.tree = new BT();
			this.tree.addTreeModifiedListener(this);

			BTEditor activeEditor = Utilities.getActiveBTEditor();
			this.guardTree = activeEditor.getBT();

			String[] pieces = editorInput.getTreeName().split(File.pathSeparator);
			this.guardNode = this.guardTree.findNode(new Identifier(pieces[1]));

			/*
			 * Important: the root node (type ROOT) of the guard's tree is not a
			 * normal ROOT, since it has no name. Therefore, we clone the
			 * original ROOT type and remove its ability to provide a name.
			 */
			ConceptualBTNode conceptualNoNameRoot = NodesLoader.getNode(
					NodeInternalType.ROOT.toString(), null).clone();
			conceptualNoNameRoot.setHasName(false);
			BTNode noNameRoot = this.tree.createNode(conceptualNoNameRoot);

			BTNode guard = this.guardNode.getGuard();

			if (guard != null) {
				/* If the node had a guard, then the editor is not dirty. */
				BTNode clonedGuard = guard.clone();
				clonedGuard.setParent(noNameRoot);
				noNameRoot.addChild(clonedGuard);
				this.dirty = false;
			} else {
				/* Otherwise, the editor is dirty. */
				this.dirty = true;
			}

			this.tree.setRoot(noNameRoot);

			this.setTitleImage(ApplicationIcons.getIcon(IconsPaths.GUARD));
		} else {
			/* Otherwise, create a new empty BT. */
			this.tree = new BT();
			this.tree.addTreeModifiedListener(this);
			tree.setRoot(tree.createNode(NodesLoader.getNode(NodeInternalType.ROOT.toString(), null)));
			this.dirty = true;
		}
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
		return this.dirty;
	}

	/**
	 * Returns true;
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		initializeViewer(parent, SWT.NONE);

		IActionBars bars = this.getEditorSite().getActionBars();

		if (bars.getGlobalActionHandler(ActionFactory.COPY.getId()) == null) {
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(), new BTEditorCopyNode());
			bars.updateActionBars();
		}
		if (bars.getGlobalActionHandler(ActionFactory.PASTE.getId()) == null) {
			bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), new BTEditorPasteNode());
			bars.updateActionBars();
		}

		/* Expands all the nodes of the tree. */
		this.expandTree(true);

		/*
		 * Selection listener that updates the NodeInfo view in order to show
		 * the currently selected node.
		 */
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				NodeInfo nodeInfoView = (NodeInfo) Utilities.getView(NodeInfo.class);

				if (nodeInfoView != null) {
					List<BTNode> selectedElements = getSelectedElements();

					if (selectedElements.size() == 1) {
						nodeInfoView.setNode(selectedElements.get(0));
					}
				}
			}
		});
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		/*
		 * Update the NodeInfo view so that it displays the information of the
		 * currently selected node.
		 */
		NodeInfo nodeInfoView = (NodeInfo) Utilities.getView(NodeInfo.class);

		if (nodeInfoView != null) {
			List<BTNode> selectedElements = getSelectedElements();

			if (selectedElements.size() == 1) {
				nodeInfoView.setNode(selectedElements.get(0));
			} else {
				nodeInfoView.setNode(null);
			}
		}
	}

	private void initializeViewer(Composite parent, int style) {
		/* Initializes the viewer. */
		this.viewer = new TreeViewer(parent, style);
		this.viewer.setContentProvider(new BTContentProvider());
		BTLabelProvider btLabelProvider = new BTLabelProvider();
		DecoratingLabelProvider decoratingLabelProvider = new DecoratingLabelProvider(
				btLabelProvider, btLabelProvider);
		this.viewer.setLabelProvider(decoratingLabelProvider);
		this.viewer.setInput(this.tree);

		final Tree treeWidget = (Tree) this.viewer.getControl();

		/* Key listener for deleting nodes. The root node cannot be deleted. */
		treeWidget.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					List<BTNode> selectedNodes = getSelectedElements();
					if (selectedNodes.size() != 0) {
						new DeleteNode(selectedNodes).run();
					}
				}

			}
		});

		/* Adding drag support. */
		this.viewer.addDragSupport(DND.DROP_MOVE,
				new Transfer[] { BTNodeIndentifierTransfer.getInstance() },
				new BTEditorDragSourceListener());

		/* Adding drop support. */
		this.viewer
				.addDropSupport(DND.DROP_MOVE,
						new Transfer[] { ConceptualBTNodeTransfer.getInstance(),
								BTNodeIndentifierTransfer.getInstance() },
						new BTEditorDropTargetListener());

		/* Menu listener that creates the context menu. */
		treeWidget.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				List<BTNode> selectedNodes = getSelectedElements();
				if (selectedNodes.size() != 0) {
					MenuManager menuManager = new MenuManager();
					menuManager.add(new DeleteNode(selectedNodes));
					menuManager.add(new ExpandNodes(selectedNodes));
					menuManager.add(new CollapseNodes(selectedNodes));

					if (selectedNodes.size() == 1) {
						BTNode selectedNode = selectedNodes.get(0);
						if (!selectedNode.getConceptualNode().getType()
								.equals(NodeInternalType.ROOT.toString())) {
							menuManager.add(new EditGuard(selectedNode));
						}

						menuManager.add(new CopyNode());
						PasteNode pasteAction = new PasteNode();
						menuManager.add(pasteAction);
					}

					treeWidget.setMenu(menuManager.createContextMenu(treeWidget));
				}
			}
		});

		/*
		 * Listener that shows a panel for editing the parameters of the node.
		 */
		this.viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					BTNode selectedNode = (BTNode) selection.getFirstElement();
					if (selectedNode.getConceptualNode().getParameters().size() != 0
							|| (selectedNode.getConceptualNode().getType() == NodeInternalType.ROOT
									.toString() && !isFromGuard())) {
						new NodeParametersDialog(Utilities.getShell(), selectedNode).open();
					}
				}
			}
		});
	}

	/**
	 * Expands or collapses all the nodes of the tree. If <code>expand</code> is
	 * true, the tree is expanded. If false, it is collapsed.
	 */
	public void expandTree(boolean expand) {
		if (expand) {
			this.viewer.expandAll();
		} else {
			this.viewer.collapseAll();
		}
		this.clearErrors();
	}

	/**
	 * Returns a list of the selected elements, or an empty list if none is
	 * selected.
	 */
	public List<BTNode> getSelectedElements() {
		return ((IStructuredSelection) this.viewer.getSelection()).toList();
	}

	/**
	 * 
	 * @see jbt.tools.bteditor.event.ITreeModifierListener#treeModified(jbt.tools.bteditor.event.TreeModifiedEvent)
	 */
	public void treeModified(TreeModifiedEvent event) {
		this.dirty = true;
		firePropertyChange(EditorPart.PROP_DIRTY);
	}

	/**
	 * Checks the validity of the tree. It highlights the nodes that are
	 * incorrect.
	 * 
	 * @return true if the tree is correct, and false otherwise.
	 */
	public boolean checkTree() {
		this.clearErrors();
		List<BTNode> incorrectNodes = this.tree.checkTree();
		if (incorrectNodes.size() != 0) {
			for (BTNode n : incorrectNodes) {
				this.setErrorColor(n);
			}
			return false;
		}
		return true;
	}

	/**
	 * Returns the BT that is being edited by this BTEditor.
	 * 
	 * @return the BT that is being edited by this BTEditor.
	 */
	public BT getBT() {
		return this.tree;
	}

	/**
	 * Selects a node of the BT. If the node does not exist, nothing happens.
	 * 
	 * @param node
	 *            the node to select.
	 */
	public void selectNode(BTNode node) {
		this.viewer.setSelection(new StructuredSelection(node), true);
	}

	/**
	 * Selects a node of the BT. If the node does not exist, nothing happens.
	 * 
	 * @param nodeID
	 *            the identifier of the node to select.
	 */
	public void selectNode(Identifier nodeID) {
		BTNode node = this.tree.findNode(nodeID);

		if (node != null) {
			this.viewer.setSelection(new StructuredSelection(node), true);
		}
	}

	/**
	 * Returns true if the BT that this BTEditor is editing comes from a file,
	 * and false otherwise.
	 * 
	 * @return true if the BT that this BTEditor is editing comes from a file,
	 *         and false otherwise.
	 */
	private boolean isFromFile() {
		return ((BTEditorInput) getEditorInput()).isFromFile();
	}

	/**
	 * Sets if the BT that this BTEditor is editing comes from a file.
	 * 
	 * @param isFromFile
	 *            true if the BT comes from a file, and false otherwise.
	 */
	private void setIsFromFile(boolean isFromFile) {
		((BTEditorInput) getEditorInput()).setIsFromFile(isFromFile);
	}

	/**
	 * Returns true if the BT that this BTEditor is editing comes from a guard,
	 * and false otherwise.
	 * 
	 * @return true if the BT that this BTEditor is editing comes from a guard,
	 *         and false otherwise.
	 */
	private boolean isFromGuard() {
		return ((BTEditorInput) getEditorInput()).isFromGuard();
	}

	/**
	 * Sets if the BT that this BTEditor is editing comes from a guard.
	 * 
	 * @param isFromGuard
	 *            true if the BT comes from a guard, and false otherwise.
	 */
	private void setIsFromGuard(boolean isFromGuard) {
		((BTEditorInput) getEditorInput()).setIsFromGuard(isFromGuard);
	}

	/**
	 * Highlights with an error color the node <code>node</code>.
	 */
	private void setErrorColor(BTNode node) {
		TreeItem item = findNode(this.viewer.getTree().getTopItem(), node);
		if (item != null) {
			item.setBackground(ERROR_COLOR);
		}
	}

	/**
	 * Returns the TreeItem associated to <code>node</code>. The search starts
	 * from the node by <code>item</code>, so all the nodes whose root is
	 * <code>item</code> are searched for. Returns null if no such element is
	 * found.
	 */
	private TreeItem findNode(TreeItem item, BTNode node) {
		if (node == item.getData()) {
			return item;
		} else {
			for (TreeItem child : item.getItems()) {
				TreeItem found = findNode(child, node);
				if (found != null) {
					return found;
				}
			}

			return null;
		}
	}

	/**
	 * Clears the errors that may be being showed in the tree of the editor.
	 */
	public void clearErrors() {
		this.tree.clearErrors();
		TreeItem rootItem = viewer.getTree().getTopItem();
		/*
		 * This check is done because when the TreeViewer is created in the
		 * BTEditor, somehow the rootItem is null despite the fact that there is
		 * an actual root element in the tree. I do not know why this happened.
		 * Actually, after so many changes, I do not know if this still happens
		 * :S
		 */
		if (rootItem != null) {
			internalClearErrorColors(rootItem);
		}
	}

	/**
	 * Clears the error color from all the nodes of the tree whose root element
	 * is <code>item</code>.
	 */
	private void internalClearErrorColors(TreeItem item) {
		item.setBackground(null);
		for (TreeItem child : item.getItems()) {
			internalClearErrorColors(child);
		}
	}

	/**
	 * Label provider of the underlying TreeViewer.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class BTLabelProvider implements ILabelProvider, ILabelDecorator {
		private List<Image> disposableImages = new LinkedList<Image>();

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
			for (Image i : this.disposableImages) {
				i.dispose();
			}
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			BTNode node = (BTNode) element;

			return ApplicationIcons.getIcon(node.getConceptualNode().getIcon());
		}

		public String getText(Object element) {
			BTNode node = (BTNode) element;

			if (node.getConceptualNode().getType()
					.equals(NodeInternalType.SUBTREE_LOOKUP.toString())) {
				for (Parameter p : node.getParameters()) {
					if (p.getName().equals("subtreeName")) {
						return node.getConceptualNode().getReadableType() + " - " + p.getValue();
					}
				}
			}

			return node.getConceptualNode().getReadableType();
		}

		/**
		 * If the BTNode has a guard, it is decorated. Otherwise, it is left
		 * unchanged.
		 * 
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image,
		 *      java.lang.Object)
		 */
		public Image decorateImage(Image image, Object element) {
			/* Overlay decorator image over base image. */
			BTNode node = (BTNode) element;

			if (node.getGuard() != null) {
				Image result;
				OverlayImageIcon overlayIcon = new OverlayImageIcon(image,
						ApplicationIcons.getIcon(IconsPaths.GUARD));
				result = overlayIcon.getImage();
				this.disposableImages.add(result);
				return result;
			} else {
				return null;
			}
		}

		public String decorateText(String text, Object element) {
			return null;
		}
	}

	/**
	 * Content provider for the underlying TreeViewer. Its input is a {@link BT}
	 * .
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class BTContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			BTNode node = (BTNode) parentElement;

			return node.getChildren().toArray();
		}

		public Object getParent(Object element) {
			BTNode node = (BTNode) element;

			return node.getParent();
		}

		public boolean hasChildren(Object element) {
			BTNode node = (BTNode) element;

			return node.getNumChildren() > 0;
		}

		public Object[] getElements(Object inputElement) {
			BTNode root = ((BT) inputElement).getRoot();
			if (root != null) {
				return new Object[] { root };
			} else {
				return new Object[] {};
			}
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Copies the currently selected BTNode of the currently active BTEditor
	 * (<b>not necessarily this one</b>) into the
	 * {@link BTEditorCopyAndPasteManager} for future paste operations. If the
	 * root node is selected, what is copied is its child (if it has one). If no
	 * node is selected, nothing is copied. If several nodes are currently
	 * selected, nothing is copied. If there is no active BTEditor, nothing is
	 * copied either.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class BTEditorCopyNode extends Action implements IAction {
		private BTNode selectedNode;

		public BTEditorCopyNode() {
			this.setText("Copy");
		}

		public void run() {
			BTEditor activeEditor = Utilities.getActiveBTEditor();
			if (activeEditor != null) {
				List<BTNode> selectedElements = activeEditor.getSelectedElements();
				if (selectedElements.size() == 1) {
					this.selectedNode = selectedElements.get(0);

					if (this.selectedNode.getConceptualNode().getType()
							.equals(NodeInternalType.ROOT.toString())) {
						if (this.selectedNode.getChildren().size() != 0) {
							this.selectedNode = this.selectedNode.getChildren().get(0);
						} else {
							this.selectedNode = null;
						}
					}
				}

				if (this.selectedNode != null) {
					BTEditorCopyAndPasteManager.getInstance().copy(this.selectedNode);
				}
			}
		}
	}

	/**
	 * Paste into the currently selected node of the currently active BTEditor
	 * (<b>not necessarily this one</b>) whatever BTNode is currently copied in
	 * the {@link BTEditorCopyAndPasteManager}. The node is pasted as a child of
	 * the currently selected node. If no node is currently copied in the
	 * BTEditorCopyAndPasteManager, nothing is done. If the currently selected
	 * node cannot hold any more children, nothing is done. Also, if there is no
	 * active BTEditor, nothing is done.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class BTEditorPasteNode extends Action implements IAction {
		private BTNode selectedNode;

		public BTEditorPasteNode() {
			this.setText("Paste");
		}

		public void run() {
			if (!BTEditorCopyAndPasteManager.getInstance().hasCopy()) {
				return;
			}
			BTEditor activeEditor = Utilities.getActiveBTEditor();
			if (activeEditor != null) {
				List<BTNode> selectedElements = activeEditor.getSelectedElements();
				if (selectedElements.size() == 1) {
					this.selectedNode = selectedElements.get(0);

					if (this.selectedNode.getConceptualNode().getNumChildren() != -1
							&& this.selectedNode.getConceptualNode().getNumChildren() <= this.selectedNode
									.getNumChildren()) {
						this.selectedNode = null;
					}
				}

				if (this.selectedNode != null) {
					BTNode pastedNode = BTEditorCopyAndPasteManager.getInstance().paste();
					activeEditor.tree.recomputeIDs(pastedNode);

					if (pastedNode.getBT() != activeEditor.tree) {
						activeEditor.tree.reassignUnderlyingBT(pastedNode);
					}

					this.selectedNode.addChild(pastedNode);
					pastedNode.setParent(this.selectedNode);
					activeEditor.tree.updateNodeCounter();
					activeEditor.viewer.refresh(this.selectedNode);
					activeEditor.treeChanged(this);
				}
			}
		}
	}

	/**
	 * Copies the currently selected BTNode into the
	 * {@link BTEditorCopyAndPasteManager} for future paste operations. If no
	 * node is selected, nothing is copied. If the root node is selected, what
	 * is copied is its child (if it has one). If several nodes are currently
	 * selected, nothing is copied either.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class CopyNode extends Action implements IAction {
		private BTNode selectedNode;

		public CopyNode() {
			this.setText("Copy");
		}

		public void run() {
			List<BTNode> selectedElements = getSelectedElements();

			if (selectedElements.size() == 1) {
				this.selectedNode = selectedElements.get(0);

				if (this.selectedNode.getConceptualNode().getType()
						.equals(NodeInternalType.ROOT.toString())) {
					if (this.selectedNode.getChildren().size() != 0) {
						this.selectedNode = this.selectedNode.getChildren().get(0);
					} else {
						this.selectedNode = null;
					}
				}
			}

			if (this.selectedNode != null) {
				BTEditorCopyAndPasteManager.getInstance().copy(this.selectedNode);
			}
		}
	}

	/**
	 * Paste into the currently selected node whatever BTNode is currently
	 * copied in the {@link BTEditorCopyAndPasteManager}. The node is pasted as
	 * a child of the currently selected node. If no node is currently copied in
	 * the BTEditorCopyAndPasteManager, nothing is done. Also, if the currently
	 * selected node cannot hold any more children, nothing is done.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class PasteNode extends Action implements IAction {
		private BTNode selectedNode;

		public PasteNode() {
			this.setText("Paste");
		}

		public void run() {
			if (!BTEditorCopyAndPasteManager.getInstance().hasCopy()) {
				return;
			}

			List<BTNode> selectedElements = getSelectedElements();
			if (selectedElements.size() == 1) {
				this.selectedNode = selectedElements.get(0);

				if (this.selectedNode.getConceptualNode().getNumChildren() != -1
						&& this.selectedNode.getConceptualNode().getNumChildren() <= this.selectedNode
								.getNumChildren()) {
					this.selectedNode = null;
				}
			}

			if (this.selectedNode != null) {
				BTNode pastedNode = BTEditorCopyAndPasteManager.getInstance().paste();

				if (pastedNode.getBT() != tree) {
					tree.reassignUnderlyingBT(pastedNode);
				}

				tree.recomputeIDs(pastedNode);
				this.selectedNode.addChild(pastedNode);
				pastedNode.setParent(this.selectedNode);
				tree.updateNodeCounter();
				viewer.refresh(this.selectedNode);
				treeChanged(this);
			}
		}
	}

	/**
	 * Action for deleting a list of nodes from the tree. The root node is not
	 * deleted.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class DeleteNode extends Action {
		private List<BTNode> selectedNodes;

		public DeleteNode(List<BTNode> selectedNodes) {
			this.setText("Delete node");
			this.selectedNodes = selectedNodes;
		}

		public void run() {
			boolean nodeDeleted = false;

			for (BTNode node : this.selectedNodes) {
				if (!node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
					if (openGuardEditors.containsKey(node)) {
						boolean delete = StandardDialogs
								.confirmationDialog(
										"Node with open guard",
										"Node "
												+ node.getID().toString()
												+ " has an open guard. If this node is removed, the guard will be dissociated from the tree. Are you sure you want to delete the node?");

						if (delete) {
							BTNode parent = node.getParent();
							parent.removeChild(node);
							viewer.refresh(parent);
							nodeDeleted = true;

							BTEditor guardEditor = openGuardEditors.get(node);
							BTEditorInput editorInput = (BTEditorInput) guardEditor
									.getEditorInput();
							editorInput.setTreeName(editorInput.getName());
							guardEditor.dissociateFromParentTree();
							guardEditor.dirty = true;
							guardEditor.firePropertyChange(PROP_TITLE);
							guardEditor.firePropertyChange(PROP_DIRTY);

							openGuardEditors.remove(node);
						}
					} else {
						BTNode parent = node.getParent();
						parent.removeChild(node);
						viewer.refresh(parent);
						nodeDeleted = true;
					}
				}
			}

			if (nodeDeleted) {
				treeChanged(viewer);
			}
		}
	}

	/**
	 * Action that expands nodes of the tree.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class ExpandNodes extends Action {
		private List<BTNode> nodesToExpand;

		public ExpandNodes(List<BTNode> nodesToExpand) {
			this.setText("Expand node");
			this.nodesToExpand = nodesToExpand;
		}

		public void run() {
			for (BTNode node : this.nodesToExpand) {
				viewer.expandToLevel(node, TreeViewer.ALL_LEVELS);
			}
		}
	}

	private class EditGuard extends Action {
		private BTNode node;

		public EditGuard(BTNode node) {
			this.setText("Edit Guard");
			this.node = node;
		}

		public void run() {
			new GuardEditionDialog(Utilities.getShell(), this.node).open();
		}
	}

	/**
	 * Action that expands nodes of the tree.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class CollapseNodes extends Action {
		private List<BTNode> nodesToCollapse;

		public CollapseNodes(List<BTNode> nodesToCollapse) {
			this.setText("Collapse node");
			this.nodesToCollapse = nodesToCollapse;
		}

		public void run() {
			for (BTNode node : this.nodesToCollapse) {
				viewer.collapseToLevel(node, TreeViewer.ALL_LEVELS);
			}
		}
	}

	/**
	 * Dialog that lets the user:
	 * <ul>
	 * <li>Add a guard to a node.
	 * <li>Modify the guard's parameters.
	 * <li>Remove the guard from a node.
	 * </ul>
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class GuardEditionDialog extends Dialog {
		private BTNode node;
		private TreeViewer guardsViewer;
		private Button editGuardButton;
		private Button addSimpleGuardButton;
		private Button removeGuardButton;
		private Button addComplexGuardButton;

		public GuardEditionDialog(Shell parentShell, BTNode node) {
			super(parentShell);
			this.node = node;
			this.setBlockOnOpen(false);
			this.setShellStyle(SWT.RESIZE | SWT.CLOSE | SWT.APPLICATION_MODAL | SWT.MAX);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse
		 * .swt.widgets.Composite)
		 */
		protected Control createContents(Composite parent) {
			Control contents = super.createContents(parent);

			return contents;
		}

		/**
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);// new

			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new GridLayout(2, false));

			/*
			 * We force the default size of the list displaying the guard to be
			 * two rows, and to have at least a minimum width.
			 */
			Composite guardsViewerComposite = new Composite(composite, SWT.NONE) {
				public Point computeSize(int wHint, int hHint, boolean changed) {
					return new Point(wHint < 200 ? 200 : wHint, guardsViewer.getTree()
							.getItemHeight() * 2);
				}
			};

			guardsViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			guardsViewerComposite.setLayout(new FillLayout());

			this.guardsViewer = new TreeViewer(guardsViewerComposite);
			this.guardsViewer.setContentProvider(new BTContentProvider());
			this.guardsViewer.setLabelProvider(new BTLabelProvider());

			/* Double click listener for editing the guards parameters. */
			this.guardsViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					if (node.getGuard() != null) {
						if (node.getGuard().getChildren().size() != 0) {
							try {
								openGuardEditor(node);
								close();
							} catch (PartInitException ex) {
								StandardDialogs.exceptionDialog("Error opening the guard",
										"There was an unexpected error when opening the guard", ex);
							}
						} else if (node.getGuard().getConceptualNode().getParameters().size() != 0) {
							new NodeParametersDialog(getShell(), node.getGuard()).open();
						}
					}
				}
			});

			if (this.node.getGuard() != null) {
				BT input = new BT(this.node.getGuard());
				this.guardsViewer.setInput(input);
			}

			Composite buttonsComposite = new Composite(composite, SWT.NONE);
			buttonsComposite.setLayout(new GridLayout(1, true));
			buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			this.addSimpleGuardButton = createAddSimpleGuardButton(buttonsComposite);
			this.addComplexGuardButton = createAddComplexGuardButton(buttonsComposite);
			this.editGuardButton = createEditGuardButton(buttonsComposite);
			this.removeGuardButton = createRemoveGuardButton(buttonsComposite);

			return composite;
		}

		private Button createAddSimpleGuardButton(Composite parent) {
			Button addSimpleGuardButton = new Button(parent, SWT.PUSH);

			addSimpleGuardButton.setText("Add simple guard");
			addSimpleGuardButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			final GuardEditionDialog guardEditionDialog = this;

			addSimpleGuardButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					new GuardInsertionDialog(Utilities.getShell(), node, guardEditionDialog).open();
				}
			});

			return addSimpleGuardButton;
		}

		private Button createAddComplexGuardButton(Composite parent) {
			Button addGuardButton = new Button(parent, SWT.PUSH);

			addGuardButton.setText("Add complex guard");
			addGuardButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			addGuardButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						openGuardEditor(node);
						close();
					} catch (PartInitException ex) {
						StandardDialogs.exceptionDialog("Error opening the guard",
								"There was an unexpected error when opening the guard", ex);
					}
				}
			});

			return addGuardButton;
		}

		private Button createEditGuardButton(Composite parent) {
			Button editGuardButton = new Button(parent, SWT.PUSH);

			editGuardButton.setText("Edit guard");
			editGuardButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			editGuardButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (node.getGuard() != null) {
						if (node.getGuard().getChildren().size() != 0) {
							try {
								openGuardEditor(node);
								close();
							} catch (PartInitException ex) {
								StandardDialogs.exceptionDialog("Error when creating new tree",
										"There was an unexpected error when creating the tree", ex);
							}
						} else if (node.getGuard().getConceptualNode().getParameters().size() != 0) {
							new NodeParametersDialog(getShell(), node.getGuard()).open();
						}
					}
				}
			});

			return editGuardButton;
		}

		private Button createRemoveGuardButton(Composite parent) {
			final Button removeGuardButton = new Button(parent, SWT.PUSH);

			removeGuardButton.setText("Remove guard");
			removeGuardButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			removeGuardButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (node.getGuard() != null) {
						node.setGuard(null);
						guardsViewer.setInput(null);

						/* Dissociate the editor that is editing the guard. */
						if (openGuardEditors.containsKey(node)) {
							BTEditor guardEditor = openGuardEditors.get(node);
							BTEditorInput editorInput = (BTEditorInput) guardEditor
									.getEditorInput();
							editorInput.setTreeName(editorInput.getName());
							guardEditor.dissociateFromParentTree();
							guardEditor.dirty = true;
							guardEditor.firePropertyChange(PROP_TITLE);
							guardEditor.firePropertyChange(PROP_DIRTY);
						}
						treeChanged(removeGuardButton);
					}
				}
			});

			return removeGuardButton;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.
		 * widgets .Shell)
		 */
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("Guard Editor");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.
		 * eclipse .swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		protected void buttonPressed(int buttonId) {
			this.setReturnCode(buttonId);
			if (buttonId == IDialogConstants.CLOSE_ID) {
				viewer.update(this.node, null);
				close();
			}
		}

		public void setGuard() {
			if (this.node.getGuard() != null) {
				BT input = new BT(this.node.getGuard());
				this.guardsViewer.setInput(input);
			}
		}
	}

	/**
	 * Dialog that shows all the loaded leaf nodes and lets the user select one
	 * to be inserted as the guard of a node.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class GuardInsertionDialog extends Dialog {
		private BTNode node;
		private TableViewer leafNodesViewer;
		private GuardEditionDialog guardEditionDialog;

		public GuardInsertionDialog(Shell parentShell, BTNode node,
				GuardEditionDialog guardEditionDialog) {
			super(parentShell);
			this.node = node;
			this.setBlockOnOpen(false);
			this.guardEditionDialog = guardEditionDialog;
			this.setShellStyle(SWT.RESIZE | SWT.CLOSE | SWT.APPLICATION_MODAL);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse
		 * .swt.widgets.Composite)
		 */
		protected Control createContents(Composite parent) {
			Control contents = super.createContents(parent);

			return contents;
		}

		/**
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			/*
			 * We force the default size of the list displaying the list of leaf
			 * nodes guard to be seven rows.
			 */
			Composite leafNodesViewerComposite = new Composite(composite, SWT.NONE) {
				public Point computeSize(int wHint, int hHint, boolean changed) {
					return new Point(leafNodesViewer.getTable().computeSize(SWT.DEFAULT,
							SWT.DEFAULT).x, leafNodesViewer.getTable().getItemHeight() * 7);
				}
			};

			leafNodesViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			leafNodesViewerComposite.setLayout(new FillLayout());
			this.leafNodesViewer = new TableViewer(leafNodesViewerComposite, SWT.BORDER
					| SWT.SINGLE);
			this.leafNodesViewer.setContentProvider(new ConceptualBTNodeListContentProvider());
			this.leafNodesViewer.setLabelProvider(new ConceptualBTNodeListLabelProvider());

			/*
			 * Build a list with all the nodes that can be used as guards, which
			 * are leaf nodes.
			 */
			List<ConceptualBTNode> standardNodes = NodesLoader.getStandardNodes();
			List<ConceptualBTNode> nonStandardNodes = NodesLoader.getNonStandardNodes();
			List<ConceptualBTNode> leafNodes = new Vector<ConceptualBTNode>();

			for (ConceptualBTNode node : standardNodes) {
				if (node.getNumChildren() == 0) {
					leafNodes.add(node);
				}
			}

			for (ConceptualBTNode node : nonStandardNodes) {
				if (node.getNumChildren() == 0) {
					leafNodes.add(node);
				}
			}

			/* This list is the input to the viewer. */
			this.leafNodesViewer.setInput(leafNodes);
			this.leafNodesViewer.getTable().select(0);

			/*
			 * Set a sorter for the elements. First standard nodes, then
			 * conditions, and finally actions.
			 */
			this.leafNodesViewer.setSorter(new ViewerSorter() {
				public int compare(Viewer viewer, Object e1, Object e2) {
					ConceptualBTNode node1 = (ConceptualBTNode) e1;
					ConceptualBTNode node2 = (ConceptualBTNode) e2;

					String node1Type = node1.getType();
					String node2Type = node2.getType();

					if ((node1Type.equals(NodeInternalType.ACTION.toString()) || node1Type
							.equals(NodeInternalType.CONDITION.toString()))
							&& node2Type.equals(node1Type)) {
						return node1.getReadableType().compareToIgnoreCase(node2.getReadableType());
					}

					if (node1Type.equals(NodeInternalType.ACTION.toString())
							&& node2Type.equals(NodeInternalType.CONDITION.toString())) {
						return 1;
					}

					if (node1Type.equals(NodeInternalType.CONDITION.toString())
							&& node2Type.equals(NodeInternalType.ACTION.toString())) {
						return -1;
					}

					if (!node1Type.equals(NodeInternalType.ACTION.toString())
							&& !node1Type.equals(NodeInternalType.CONDITION.toString())) {
						if (!node2Type.equals(NodeInternalType.ACTION.toString())
								&& !node2Type.equals(NodeInternalType.CONDITION.toString())) {
							return node1.getReadableType().compareToIgnoreCase(
									node2.getReadableType());
						} else {
							return -1;
						}
					}

					return node1.getReadableType().compareToIgnoreCase(node2.getReadableType());
				}
			});

			/* Add double click listener. */
			this.leafNodesViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					setGuardFromSelection();
					treeChanged(this);
					close();
				}
			});

			return composite;
		}

		/**
		 * Content provider for a list of {@link ConceptualBTNode} objects.
		 * 
		 * Its input is a List&lt;ConceptualBTNode&gt;.
		 * 
		 * @author Ricardo Juan Palma Durán
		 * 
		 */
		private class ConceptualBTNodeListContentProvider implements IStructuredContentProvider {
			public Object[] getElements(Object inputElement) {
				return ((List<ConceptualBTNode>) inputElement).toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		}

		/**
		 * Label provider for objects of type ConceptualBTNode.
		 * 
		 * @author Ricardo Juan Palma Durán
		 * 
		 */
		private class ConceptualBTNodeListLabelProvider implements ILabelProvider {

			public Image getImage(Object element) {
				ConceptualBTNode node = (ConceptualBTNode) element;

				return ApplicationIcons.getIcon(node.getIcon());
			}

			public String getText(Object element) {
				ConceptualBTNode node = (ConceptualBTNode) element;

				return node.getReadableType();
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.
		 * widgets .Shell)
		 */
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("Guard Editor");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.
		 * eclipse .swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
			createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		protected void buttonPressed(int buttonId) {
			this.setReturnCode(buttonId);
			if (buttonId == IDialogConstants.CLOSE_ID) {
				close();
			} else {
				setGuardFromSelection();
				treeChanged(this);
				close();
			}
		}

		private void setGuardFromSelection() {
			ConceptualBTNode selectedConceptualBTNode = (ConceptualBTNode) ((IStructuredSelection) this.leafNodesViewer
					.getSelection()).toList().get(0);
			this.node.setGuard(tree.createNode(selectedConceptualBTNode));
			this.guardEditionDialog.setGuard();
		}
	}

	/**
	 * Dialog that is used to enter parameter values.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class NodeParametersDialog extends Dialog {
		private BTNode node;
		private NameEditorComposite nameEditor;
		private ParametersEditorComposite nodeEditor;

		/**
		 * Constructor. <code>node</code> must be a node with parameters.
		 */
		public NodeParametersDialog(Shell shell, BTNode node) {
			super(shell);
			this.setBlockOnOpen(false);
			this.node = node;
			this.setShellStyle(SWT.RESIZE | SWT.CLOSE | SWT.APPLICATION_MODAL);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse
		 * .swt.widgets.Composite)
		 */
		protected Control createContents(Composite parent) {
			Control contents = super.createContents(parent);

			return contents;
		}

		/**
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			if (this.node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
				this.nameEditor = new NameEditorComposite(composite, SWT.NONE, this.node.getName());
				this.nameEditor.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			}

			if (this.node.getConceptualNode().getParameters().size() != 0) {
				Group parametersGroup = new Group(composite, SWT.NONE);
				parametersGroup.setText("Parameters");
				parametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				parametersGroup.setLayout(new GridLayout(1, false));
				this.nodeEditor = new ParametersEditorComposite(parametersGroup, SWT.NONE,
						this.node);
				this.nodeEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}
			
			

			return composite;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.
		 * widgets .Shell)
		 */
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("Node Editor");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.
		 * eclipse .swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);

			createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		protected void buttonPressed(int buttonId) {
			this.setReturnCode(buttonId);
			if (buttonId == IDialogConstants.CLOSE_ID) {
				close();
			} else {
				if (this.nodeEditor != null) {
					try {
						List<Parameter> params = this.nodeEditor.getParameters();
						this.node.clearParameters();
						for (Parameter p : params) {
							this.node.addParameter(p);
						}
					} catch (Exception e) {
						MessageDialog.openError(this.getShell(), "Error validating parameters",
								e.getMessage());
						return;
					}
				}
				if (this.nameEditor != null) {
					try {
						this.node.setName(this.nameEditor.getNodeName());
					} catch (Exception e) {
						MessageDialog.openError(this.getShell(), "Error validating name",
								e.getMessage());
						return;
					}
				}

				treeChanged(viewer);

				if (node.getConceptualNode().getType()
						.equals(NodeInternalType.SUBTREE_LOOKUP.toString())) {
					viewer.update(node, null);
				}

				close();
			}
		}
	}

	/**
	 * Composite for editing the name of the root tree.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class NameEditorComposite extends Composite {
		private Label nameLabel;
		private Text nameValue;

		/**
		 * <code>initialName</code> is the name that is displayed when the
		 * Composite is created. It may be null, in which case no name is
		 * displayed.
		 */
		public NameEditorComposite(Composite parent, int style, String initialName) {
			super(parent, style);
			this.setLayout(new GridLayout(2, false));

			this.nameLabel = new Label(this, SWT.NONE);
			this.nameLabel.setText("Name");
			this.nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			this.nameValue = new Text(this, SWT.BORDER);
			this.nameValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			this.nameValue.setText(initialName == null ? "" : initialName);
		}

		/**
		 * Returns the name, or throws an exception if it is not a valid name.
		 */
		public String getNodeName() throws RuntimeException {
			if (this.nameValue.getText().equals("")) {
				throw new RuntimeException("Invalid name");
			}
			return this.nameValue.getText();
		}
	}

	/**
	 * Composite for editing the parameters of a node. It internally stores many
	 * {@link ParameterComposite}, one for every parameter of the node.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class ParametersEditorComposite extends Composite {
		private BTNode node;
		private List<ParameterComposite> parameterComposites;

		public ParametersEditorComposite(Composite parent, int style, BTNode node) {
			super(parent, style);
			this.node = node;

			this.setLayout(new GridLayout(1, false));
			this.parameterComposites = new Vector<ParameterComposite>();

			List<jbt.tools.bteditor.model.ConceptualBTNode.Parameter> parameters = this.node
					.getConceptualNode().getParameters();

			for (int i = 0; i < parameters.size(); i++) {
				String currentParameterValue = this.node.getParameters().size() > 0 ? this.node
						.getParameters().get(i).getValue() : null;

				boolean currentFromContext = this.node.getParameters().size() > 0 ? this.node
						.getParameters().get(i).getFromContext() : false;

				boolean isConstant = true;
				if (ParameterType.isVariable(this.node.getConceptualNode().getParameters().get(i).getType()))
				{
					String fieldValue = null;
					BTNode.VarParameter varNodeParameter = null;
					if (this.node.getParameters().size() > 0)
					{
						varNodeParameter = (BTNode.VarParameter)this.node.getParameters().get(i);
						fieldValue = varNodeParameter.getVariableName();
						isConstant = varNodeParameter.getIsConstant();
					}
					VarParameterComposite pc = new VarParameterComposite(this, SWT.NONE, parameters.get(i),
							currentParameterValue, currentFromContext, isConstant, fieldValue, varNodeParameter);
					this.parameterComposites.add(pc);
					pc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				}
				else
				{
					ParameterComposite pc = new ParameterComposite(this, SWT.NONE, parameters.get(i),
							currentParameterValue, currentFromContext);
					this.parameterComposites.add(pc);
					pc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				}
			}
		}

		public List<Parameter> getParameters() throws RuntimeException {
			List<Parameter> result = new Vector<Parameter>();

			for (ParameterComposite pc : this.parameterComposites) {
				result.add(pc.getParameter());
			}

			return result;
		}
	}

	/**
	 * Composite for editing an individual parameter. If the parameter is
	 * contextable, the value of the context location where to find the
	 * parameter may be specified.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class ParameterComposite extends Composite {
		private jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameter;
		private Label nameLabel;
		private Text valueText;
		private Checkbox isConstant;
		private Button fromContextButton;
		private static final int DEFAULT_TEXT_FIELD_WIDTH = 100;

		
		public ParameterComposite(Composite parent, int style)
		{
			super(parent, style);
		}
		/**
		 * <code>initialValue</code> may be null.
		 */
		public ParameterComposite(Composite parent, int style,
				jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameter, String initialValue,
				boolean initialFromContext) {
			super(parent, style);

			this.setLayout(new GridLayout(3, false));
			
			this.parameter = parameter;

			String tooltip = getTooltip(parameter);

			this.nameLabel = new Label(this, SWT.NONE);
			this.nameLabel.setText(this.parameter.getName() + " ("
					+ this.parameter.getType().getReadableType() + ")");
			this.nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			this.nameLabel.setToolTipText(tooltip);

			this.valueText = new Text(this, SWT.BORDER);
			this.valueText.setText(initialValue == null ? "" : initialValue);
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.widthHint = DEFAULT_TEXT_FIELD_WIDTH;
			this.valueText.setLayoutData(data);
			this.valueText.setToolTipText(tooltip);

			if (this.parameter.getContextable()) {
				this.fromContextButton = new Button(this, SWT.CHECK);
				this.fromContextButton.setText("From context");
				this.fromContextButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
						false));
				this.fromContextButton.setSelection(initialFromContext);
				this.fromContextButton
						.setToolTipText("If activated, the specified value represents the place, in the context, where the value of the variable will be retrieved from");

				if (this.parameter.getType() == ParameterType.OBJECT) {
					this.fromContextButton.setEnabled(false);
					this.fromContextButton.setSelection(true);
				}
			}
		}

		/**
		 * Returns the parameter shown by the Composite, and throws an exception
		 * in case there is some error in the parameter.
		 */
		public Parameter getParameter() throws RuntimeException {
			/*
			 * If the parameter must come from the context, then any non-empty
			 * string value is allowed.
			 */
			if (this.parameter.getContextable() && this.fromContextButton.getSelection()) {
				if (this.valueText.getText().equals("")) {
					throw new RuntimeException("Invalid parameter value for parameter "
							+ this.parameter.getName() + ". Must be a non-empty string.");
				}
			} else {
				if (!BTNode.checkParameter(parameter, this.valueText.getText(), tree)) {
					throw new RuntimeException("Invalid parameter value ("
							+ this.valueText.getText() + ") for parameter "
							+ this.parameter.getName());
				}
			}
			
			Parameter result = new Parameter();
			result.setName(this.parameter.getName());
			result.setValue(this.valueText.getText());

			if (this.parameter.getContextable()) {
				result.setFromContext(this.fromContextButton.getSelection());
			}

			return result;
		}

		/**
		 * Returns an appropriate descriptive tooltip for a
		 * {@link jbt.tools.bteditor.model.ConceptualBTNode.Parameter}.
		 */
		private String getTooltip(
				jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameterDefinition) {
			ParameterType parameterType = parameterDefinition.getType();

			if (parameterType == ParameterType.BOOLEAN) {
				return "Boolean value. Can be either \"true\" or \"false\"";
			}
			if (parameterType == ParameterType.COORDINATE) {
				return "Coordinate value. Must be a sequence of real numbers separated by blank spaces";
			}
			if (parameterType == ParameterType.DIRECTION) {
				return "Direction value. Can be any integer value";
			}
			if (parameterType == ParameterType.INTEGER) {
				return "Integer value";
			}
			if (parameterType == ParameterType.NODE_ID) {
				String validTypes = "";

				if (parameterDefinition.getNodeClasses().size() != 0) {
					for (String type : parameterDefinition.getNodeClasses()) {
						validTypes += NodesLoader.getNode(type, null).getReadableType() + ", ";
					}

					validTypes = validTypes.substring(0, validTypes.length() - 2);
				}

				return "Node identifier. Must be the identifier of a node of the tree"
						+ (validTypes.equals("") ? "" : ", with one of the following types: "
								+ validTypes);
			}
			if (parameterType == ParameterType.OBJECT) {
				return "Object. Always retrieved from the context";
			}
			if (parameterType == ParameterType.PARALLEL_POLICY) {
				return "Parallel task policy. Can be either \"sequence\" or \"selector\"";
			}
			if (parameterType == ParameterType.REAL) {
				return "Real value";
			}
			if (parameterType == ParameterType.STATUS_CODE) {
				return "Task termination status code. Can be either \"success\" or \"failure\"";
			}
			if (parameterType == ParameterType.STRING) {
				return "String value. Any non-empty string";
			}
			if (parameterType == ParameterType.LIST_OF_VARIABLES) {
				return "List of variables, surrounded each one by \"\", and separated by blank spaces";
			}
			return "";
		}
		
		protected Composite getCompositeParent()
		{
			return this.getParent();
		}
	}
	
	/**
	 * Composite for editing an variable individual parameter. If the parameter is
	 * contextable, the value of the context location where to find the
	 * parameter may be specified. Also check if the parameter is a key-value pair
	 * and shows the dialog regarding that
	 * 
	 * @author Fernando Matarrubia Garc�a
	 * 
	 */
	private class VarParameterComposite extends ParameterComposite {
		private jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameter;
		private Label nameLabel;
		private Text valueText;
		private final Button isConstant;
		private final Text variableNameText;
		private Button fromContextButton;
		private static final int DEFAULT_TEXT_FIELD_WIDTH = 100;
		private BTNode.VarParameter varNodeParameter;

		/**
		 * <code>initialValue</code> may be null.
		 */
		public VarParameterComposite(Composite parent, int style,
				jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameter, String initialValue,
				boolean initialFromContext, boolean isConstant, String defaultVarText,  BTNode.VarParameter varNodeParameter) {
			
			super(parent, style);
			
			this.varNodeParameter = varNodeParameter;

			this.setLayout(new GridLayout(4, true));

			this.parameter = parameter;

			String tooltip = super.getTooltip(parameter);

			this.nameLabel = new Label(this, SWT.NONE);
			this.nameLabel.setText(this.parameter.getName() + " ("
					+ this.parameter.getType().getReadableType() + ")");
			this.nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			this.nameLabel.setToolTipText(tooltip);
			
			this.isConstant = new Button(this, SWT.CHECK);
			this.isConstant.setText("Constant");
			GridData data2 = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			data2.widthHint = DEFAULT_TEXT_FIELD_WIDTH;
			this.isConstant.setLayoutData(data2);
			this.isConstant.setSelection(isConstant);
			this.isConstant.setToolTipText("If activated, the \"Constant\" field is enabled. That means that the parameter is a special key-value type");

			this.variableNameText = new Text(this, SWT.BORDER);
			this.variableNameText.setText(defaultVarText == null ? "" : defaultVarText);
			GridData data3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data2.widthHint = DEFAULT_TEXT_FIELD_WIDTH;
			this.variableNameText.setLayoutData(data3);
			this.variableNameText.setToolTipText(tooltip);
			this.variableNameText.setEnabled(!isConstant);
			
			this.valueText = new Text(this, SWT.BORDER);
			this.valueText.setText(initialValue == null ? "" : initialValue);
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.widthHint = DEFAULT_TEXT_FIELD_WIDTH;
			this.valueText.setLayoutData(data);
			this.valueText.setToolTipText(tooltip);
			
			Listener listener = new Listener() {
				 
			      public void handleEvent(Event event) {
			      
			    	  variableNameText.setEnabled(!VarParameterComposite.this.isConstant.getSelection());
			    	  setIsConstant(VarParameterComposite.this.isConstant.getSelection());
			      }
			};
			
			this.isConstant.addListener(SWT.Selection, listener);

			if (this.parameter.getContextable()) {
				this.fromContextButton = new Button(this, SWT.CHECK);
				this.fromContextButton.setText("From context");
				this.fromContextButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
						false));
				this.fromContextButton.setSelection(initialFromContext);
				this.fromContextButton
						.setToolTipText("If activated, the specified value represents the place, in the context, where the value of the variable will be retrieved from");

				if (this.parameter.getType() == ParameterType.OBJECT) {
					this.fromContextButton.setEnabled(false);
					this.fromContextButton.setSelection(true);
				}
			}
		}

		/**
		 * Returns the parameter shown by the Composite, and throws an exception
		 * in case there is some error in the parameter.
		 */
		public Parameter getParameter() throws RuntimeException {
			/*
			 * If the parameter must come from the context, then any non-empty
			 * string value is allowed.
			 */
			if (this.parameter.getContextable() && this.fromContextButton.getSelection()) {
				if (this.valueText.getText().equals("")) {
					throw new RuntimeException("Invalid parameter value for parameter "
							+ this.parameter.getName() + ". Must be a non-empty string.");
				}
			} else {
				if (!BTNode.checkVarParameter(parameter, this.valueText.getText(), this.variableNameText.getText(), this.isConstant.getSelection(), tree)) {
					throw new RuntimeException("Invalid parameter value ("
							+ this.valueText.getText() + ") for parameter "
							+ this.parameter.getName());
				}
			}
			BTNode.Parameter aux = new BTNode.Parameter();
			

			aux.setName(this.parameter.getName());
			aux.setValue(this.valueText.getText());
			
			BTNode.VarParameter result = new BTNode.VarParameter(aux);
			result.setIsConstant(this.isConstant.getSelection());
			result.setVariableName(this.variableNameText.getText());

			if (this.parameter.getContextable()) {
				result.setFromContext(this.fromContextButton.getSelection());
			}

			return result;
		}
		
		/*
		 * Helper function that sets the Field Value of a Variable Node Parameter
		 * This function is called from the checkbox event listener
		 */
		public void setIsConstant(boolean isConstant)
		{
			if (varNodeParameter != null)
			{
				varNodeParameter.setIsConstant(isConstant);
			}
		}
	}

	/**
	 * Drag listener used by the BTEditor. This drag listener transfers the
	 * selected element {@link BTNode}, and is compatible with the
	 * {@link BTNodeIndentifierTransfer} type.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class BTEditorDragSourceListener implements DragSourceListener {
		public void dragStart(DragSourceEvent event) {
			List<BTNode> selection = ((IStructuredSelection) viewer.getSelection()).toList();

			if (selection.size() == 1) {
				if (!selection.get(0).getConceptualNode().getType()
						.equals(NodeInternalType.ROOT.toString())) {
					event.doit = true;
					return;
				}
			}

			event.doit = false;
		}

		public void dragSetData(DragSourceEvent event) {
			if (BTNodeIndentifierTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			}
		}

		public void dragFinished(DragSourceEvent event) {
		}
	}

	/**
	 * Drop target listener used by the BTEditor. This drop target listener is
	 * compatible with two transfer types, {@link ConceptualBTNodeTransfer} and
	 * {@link BTNodeIndentifierTransfer}.
	 * <p>
	 * When the data being transfered is compatible with
	 * {@link ConceptualBTNodeTransfer}, a new empty BTNode is created as a
	 * child or as a sibling of the target of the drop operation (depending on
	 * the specific position of the cursor at the moment the operation is
	 * performed).
	 * <p>
	 * When the data being transfered is compatible with
	 * {@link BTNodeIndentifierTransfer}, that node is removed from its current
	 * position and inserted as a sibling or as a child of the target of the
	 * drop operation (depending on the specific position of the cursor at the
	 * moment the operation is performed).
	 * <p>
	 * <b>It should be noted that dragging from a BTEditor and dropping onto
	 * another editor is not supported.</b>
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class BTEditorDropTargetListener implements DropTargetListener {
		public void dragEnter(DropTargetEvent event) {
			for (int i = 0; i < event.dataTypes.length; i++) {
				if (BTNodeIndentifierTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
					event.currentDataType = event.dataTypes[i];
					event.detail = DND.DROP_MOVE;
					break;
				}
				if (ConceptualBTNodeTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
					event.currentDataType = event.dataTypes[i];
					event.detail = DND.DROP_MOVE;
					break;
				}
			}
		}

		public void dragLeave(DropTargetEvent event) {
			event.detail = DND.DROP_MOVE;
		}

		public void dragOperationChanged(DropTargetEvent event) {
			event.detail = DND.DROP_MOVE;
		}

		public void dragOver(DropTargetEvent event) {
			if (ConceptualBTNodeTransfer.getInstance().isSupportedType(event.currentDataType)) {
				if (event.item == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				BTNode node = (BTNode) event.item.getData();

				if (node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
					/*
					 * The root node is always allowed as a target for inserting
					 * as a child.
					 */
					event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL
							| DND.FEEDBACK_EXPAND;
				} else {
					/*
					 * If the target is not the root, then the source node can
					 * be inserted as a child or as a sibling of the target.
					 */
					if (!closeToBottom((TreeItem) event.item)) {
						/*
						 * Cannot move if the limit number of children is
						 * exceeded.
						 */
						if (node.getConceptualNode().getNumChildren() != -1) {
							if (node.getConceptualNode().getNumChildren() <= node.getNumChildren()) {
								event.detail = DND.DROP_NONE;
								event.feedback = DND.FEEDBACK_EXPAND;
								return;
							}
						}

						event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL
								| DND.FEEDBACK_EXPAND;
					} else {
						/*
						 * Cannot move if the limit number of children is
						 * exceeded.
						 */
						BTNode parent = node.getParent();

						if (parent.getConceptualNode().getNumChildren() != -1) {
							if (parent.getConceptualNode().getNumChildren() <= parent
									.getNumChildren()) {
								event.detail = DND.DROP_NONE;
								event.feedback = DND.FEEDBACK_EXPAND;
								return;
							}
						}

						event.feedback = DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_SCROLL
								| DND.FEEDBACK_EXPAND;
					}
				}

				event.detail = DND.DROP_MOVE;
			} else if (BTNodeIndentifierTransfer.getInstance().isSupportedType(
					event.currentDataType)) {
				if (event.item == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				BTNode node = (BTNode) event.item.getData();

				/* Cannot move as a sibling of the root. */
				if (node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
					event.detail = DND.DROP_NONE;
					event.feedback = DND.FEEDBACK_EXPAND;
				} else {
					BTNode parent = node.getParent();

					if (!closeToBottom((TreeItem) event.item)) {
						/*
						 * Cannot move if the limit number of children is
						 * exceeded.
						 */
						if (node.getConceptualNode().getNumChildren() != -1) {
							if (node.getConceptualNode().getNumChildren() <= node.getNumChildren()) {
								event.detail = DND.DROP_NONE;
								event.feedback = DND.FEEDBACK_EXPAND;
								return;
							}
						}

						event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL
								| DND.FEEDBACK_EXPAND;
					} else {
						/*
						 * Cannot move if the limit number of children is
						 * exceeded.
						 */
						if (parent.getConceptualNode().getNumChildren() != -1) {
							if (parent.getConceptualNode().getNumChildren() <= parent
									.getNumChildren()) {
								event.detail = DND.DROP_NONE;
								event.feedback = DND.FEEDBACK_EXPAND;
								return;
							}
						}

						event.feedback = DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_SCROLL
								| DND.FEEDBACK_EXPAND;
					}

					event.detail = DND.DROP_MOVE;
				}
			}
		}

		public void drop(DropTargetEvent event) {
			if (ConceptualBTNodeTransfer.getInstance().isSupportedType(event.currentDataType)) {
				Pair<String, String> nodeIdentifier = (Pair<String, String>) event.data;
				BTNode selectedNode = (BTNode) event.item.getData();

				ConceptualBTNode newConceptualNode = NodesLoader.getNode(nodeIdentifier.getFirst(),
						nodeIdentifier.getSecond());

				/*
				 * This is a bit messy.
				 * 
				 * If the target node ("selectedNode") is the root:
				 * 
				 * 1) If the root has no child, then the dropped node is
				 * inserted as the child of the root.
				 * 
				 * 2) If it has one child (the root can only have one child),
				 * the dropped node is inserted as its new only child, and the
				 * old one is inserted as a child of the new one. If the dropped
				 * node does not support children, then the drop operation is
				 * canceled.
				 * 
				 * If the target node is not the root, then proceed as usual: the
				 * dropped node is inserted as a child or as a sibling of the
				 * target node.
				 */
				if (selectedNode.getConceptualNode().getType()
						.equals(NodeInternalType.ROOT.toString())) {
					if (selectedNode.getNumChildren() != 0) {
						if (newConceptualNode.getNumChildren() == 0) {
							return;
						} else {
							BTNode newNode = tree.createNode(NodesLoader.getNode(
									nodeIdentifier.getFirst(), nodeIdentifier.getSecond()));

							BTNode oldRoot = selectedNode.getChildren().get(0);
							selectedNode.removeChild(oldRoot);
							newNode.setParent(selectedNode);
							selectedNode.addChild(newNode);
							oldRoot.setParent(newNode);
							newNode.addChild(oldRoot);

							viewer.refresh(selectedNode);
							viewer.expandToLevel(selectedNode, 1);
							treeChanged(viewer);
							clearErrors();
							return;
						}
					}
				}

				BTNode newNode = tree.createNode(NodesLoader.getNode(nodeIdentifier.getFirst(),
						nodeIdentifier.getSecond()));

				/*
				 * Insert the source node as a child or as sibling of the target
				 * depending on the current relative position of the cursor to
				 * the target.
				 */
				if (!closeToBottom((TreeItem) event.item)) {
					newNode.setParent(selectedNode);
					selectedNode.addChild(newNode);
					viewer.expandToLevel(selectedNode, 1);
					viewer.refresh(selectedNode);

				} else {
					selectedNode.addAsSibling(newNode);
					viewer.refresh(selectedNode.getParent());
				}

				treeChanged(viewer);
				clearErrors();
			}
			if (BTNodeIndentifierTransfer.getInstance().isSupportedType(event.currentDataType)) {
				BTNode target = (BTNode) event.item.getData();
				Identifier id = (Identifier) event.data;
				BTNode source = tree.findNode(id);

				if (source.hasAsDescendant(target)) {
					event.detail = DND.DROP_NONE;
					return;
				}

				/*
				 * Insert the source node as a child or as sibling of the target
				 * depending on the current relative position of the cursor to
				 * the target.
				 */
				if (!closeToBottom((TreeItem) event.item)) {
					target.addChild(source);
					source.getParent().removeChild(source);
					source.setParent(target);
				} else {
					target.addAsSibling(source);
				}

				viewer.refresh();
				viewer.expandToLevel(target, 1);
				treeChanged(viewer);
				clearErrors();
			}
		}

		public void dropAccept(DropTargetEvent event) {
		}

		/**
		 * Given a TreeItem of the tree, this function analyses whether the
		 * current cursor position is close to its bottom or not. This is used
		 * to tell whether the dropped element is inserted as a child or as a
		 * sibling of the target node.
		 * 
		 * @return true if the cursor position is close to the bottom of
		 *         <code>treeItem</code>.
		 */
		private boolean closeToBottom(TreeItem treeItem) {
			Display display = treeItem.getDisplay();
			Point cursorLocation = treeItem.getDisplay().getCursorLocation();
			Rectangle itemLocation = treeItem.getBounds();
			int height = viewer.getTree().getItemHeight();
			int margin = ((int) height * 0.20) <= 1 ? 1 : (int) (height * 0.20);

			/*
			 * If the margin is null, return true, since insertion as sibling
			 * prevails over insertion as child.
			 */
			if (margin == 0) {
				return true;
			}

			int limitHeight = itemLocation.y + height - 1 - margin;

			Point mappedCoords = display.map(null, viewer.getTree(), cursorLocation);

			if (mappedCoords.y >= limitHeight) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Method that is called when the tree has experimented a change. This will
	 * notify all the listeners of the underlying tree by firing a
	 * {@link TreeModifiedEvent}.
	 */
	private void treeChanged(Object source) {
		this.tree.fireTreeChanged(source);
	}

	/**
	 * Opens a BTEditor for editing the guard of a BTNode.
	 */
	private void openGuardEditor(final BTNode node) throws PartInitException {
		IWorkbenchPage activePage = Utilities.getMainWindowActivePage();

		BTEditorInput editorInput = new BTEditorInput(
				((BTEditorInput) getEditorInput()).getEditorID() + File.pathSeparator
						+ node.getID().toString(), false, true);

		BTEditor openEditor = (BTEditor) activePage.openEditor(editorInput, BTEditor.ID);

		if (!openGuardEditors.containsKey(node)) {
			openGuardEditors.put(node, openEditor);
		}
	}

	/**
	 * The IPartListener that BTEditors use to manage close events. When a
	 * BTEditor is closed, two things happen:
	 * 
	 * <ul>
	 * <li>If this is an BTEditor that contains nodes whose guards are being
	 * edited in other editors, then those editors are dissociated from its
	 * corresponding tree, so that they are no longer editing a guard, but a
	 * normal behaviour tree.
	 * <li>If this is a BTEditor that is editing a guard, then it removes itself
	 * from the list of BTEditors {@link BTEditor#openGuardEditors}.
	 * </ul>
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class BTEditorPartListener implements IPartListener {
		public void partActivated(IWorkbenchPart part) {
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
			/*
			 * Must be kept in mind that this IPartListener is shared among all
			 * the editors, so trying to acces "this" will just not work. The
			 * BTEditor that is closed is the input argument "part".
			 */
			if (part instanceof BTEditor) {
				/*
				 * First dissociate all the editors that are editing nodes'
				 * guards of this tree.
				 */
				BTEditor editorToClose = (BTEditor) part;
				Collection<BTEditor> guardEditors = editorToClose.openGuardEditors.values();

				for (BTEditor editor : guardEditors) {
					BTEditorInput editorInput = (BTEditorInput) editor.getEditorInput();
					editorInput.setTreeName(editorInput.getName());
					editor.dissociateFromParentTree();
					editor.dirty = true;
					editor.firePropertyChange(PROP_TITLE);
					editor.firePropertyChange(PROP_DIRTY);
				}

				// if (guardEditors.size() != 0) {
				// StandardDialogs
				// .informationDialog(
				// "Guards still open",
				// "The closed behaviour tree contained some open guards. They have been dissociated from the tree, but they are kept open.");
				// }

				guardEditors.clear();

				/*
				 * Then, if this editor is editing a guard, remove itself from
				 * the "openGuardEditors" list of the editor that contains the
				 * tree that has the node whose guard is being edited by this
				 * editor.
				 */
				if (editorToClose.isFromGuard()) {
					BTEditorInput editorInput = (BTEditorInput) editorToClose.getEditorInput();
					String[] pieces = editorInput.getTreeName().split(File.pathSeparator);
					long parentEditorID = Long.parseLong(pieces[0]);
					BTEditor parentEditor = Utilities.getBTEditor(parentEditorID);
					if (parentEditor != null) {
						parentEditor.openGuardEditors.remove(editorToClose.guardNode);
					}
				}
			}
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	}

	/**
	 * This is used for BTEditors that are editing guards. It dissociates the
	 * BTEditor from the tree that contains the node whose guard is being
	 * edited. By doing so, this editor will be considered not to come from a guard
	 * (<code>setIsFromGuard(false)</code>), so it will change its title image to
	 * that of a normal BT. Also, {@link BTEditor#guardNode} and
	 * {@link BTEditor#guardTree} are set to null. Finally, the root node of the
	 * tree (that of type ROOT) is modified so that it is a normal ROOT node
	 * (that is, it can have a name).
	 */
	private void dissociateFromParentTree() {
		if (isFromGuard()) {
			this.setIsFromGuard(false);
			this.setTitleImage(ApplicationIcons.getIcon(IconsPaths.BT));

			BTNode newRoot = this.tree.createNode(NodesLoader.getNode(
					NodeInternalType.ROOT.toString(), null));

			if (this.tree.getRoot().getChildren().size() != 0) {
				newRoot.addChild(this.tree.getRoot().getChildren().get(0));
			}

			this.tree.setRoot(newRoot);
			this.guardNode = null;
			this.guardTree = null;
			this.viewer.refresh();
			this.viewer.expandAll();
		}
	}
}
