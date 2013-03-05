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
import java.util.Vector;

import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.model.BT;
import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.model.BTNode.Identifier;
import jbt.tools.bteditor.util.Utilities;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * The NodesSearcher class is an Eclipse view that lets the user search nodes in
 * a behaviour tree. This view shows a simple text field where the user can
 * input the identifier or the partial identifier of a behaviour tree node (
 * {@link BTNode}), and then perform the search by clicking a button or pressing
 * the "enter". The view shows a list containing the nodes of the BT in the
 * currently active BTEditor that match such an identifier. By clicking a node
 * of the search result, it gets selected, and the BTEditor that contains it
 * gets activated. If the text field is left empty, the search list will contain
 * all the nodes of the tree.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NodesSearcher extends ViewPart {
	public static String ID = "jbt.tools.bteditor.views.NodesSearcher";

	/** Composite that stores all the widget that this ViewPart displays. */
	private Composite global;
	/** TableViewer that displays the search results. */
	private TableViewer resultsTable;
	/** The result of the search. It stores a set of BTNode identifiers. */
	private List<Identifier> searchResult;
	/**
	 * Text field where the user inputs the identifier or the partial identifier
	 * of the node.
	 */
	private Text searchTextField;
	/** BTEditor whose BT was the target of the search. */
	private BTEditor targetEditor;
	/**
	 * Label that displays the name of the BTEditor whose BT was the target of
	 * the search.
	 */
	private Label targetEditorName;

	/**
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		this.searchResult = new Vector<Identifier>();

		/* Initialize the global composite. */
		this.global = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.global.setLayout(layout);

		/*
		 * Create the top part of the composite (the part containing the search
		 * text field and the search button.
		 */
		createTopComposite(this.global);
		/*
		 * Create the bottom part of the composite (the part containing the
		 * search result).
		 */
		createBottomComposite(this.global);
	}

	/**
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/**
	 * Creates the Composite that contains the result of the search. The
	 * Composite contains the {@link #resultsTable} element.
	 * 
	 * @param parent
	 *            the Composite where the created Composite will be placed.
	 */
	private void createBottomComposite(Composite parent) {
		this.resultsTable = new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
		this.resultsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.resultsTable.setLabelProvider(new ResultsTableLabelProvider());
		this.resultsTable.setContentProvider(new ResultsTableContentProvider());
		this.resultsTable.setInput(this.searchResult);

		/* Sort elements by its String representation. */
		this.resultsTable.setSorter(new ViewerSorter());

		/*
		 * Listener that will select the node in the target BTEditor and which
		 * also activates the target BTEditor.
		 */
		this.resultsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();

				if (!selection.isEmpty()) {
					Identifier selectedNode = (Identifier) ((IStructuredSelection) selection)
							.getFirstElement();
					targetEditor.selectNode(selectedNode);
					Utilities.activateEditor(targetEditor);
				}
			}
		});

		this.targetEditorName = new Label(parent, SWT.NONE);
		this.targetEditorName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
	}

	/**
	 * Creates the Composite that shows the search text field and the search
	 * button.
	 * 
	 * @param parent
	 *            the Composite where the created Composite will be placed.
	 */
	private void createTopComposite(Composite parent) {
		Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayout(new GridLayout(3, false));

		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label label = new Label(searchComposite, SWT.NONE);
		label.setText("Node ID:");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		this.searchTextField = createTextField(searchComposite);
		createSearchButton(searchComposite);
	}

	/**
	 * Creates the search Button.
	 * 
	 * @param parent
	 *            the Composite where the Button is placed.
	 */
	private void createSearchButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Search");
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		/* When clicked, the button performs the search. */
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String searchText = searchTextField.getText();
				performSearch(searchText);
			}
		});
	}

	/**
	 * Creates the search text field (Text).
	 * 
	 * @param parent
	 *            the Composite where the text field is placed.
	 * @return the text field.
	 */
	private Text createTextField(Composite parent) {
		final Text textField = new Text(parent, SWT.BORDER);

		textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		/* If "enter" is pressed, perform the search. */
		textField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					performSearch(textField.getText());
				}
			}
		});

		return textField;
	}

	/**
	 * Label provider for the search results table (
	 * {@link NodesSearcher#resultsTable}).
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class ResultsTableLabelProvider implements ITableLabelProvider {
		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return ((Identifier) element).toString();
		}
	}

	/**
	 * Content provider for the search results table (
	 * {@link NodesSearcher#resultsTable}).
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class ResultsTableContentProvider implements IStructuredContentProvider {
		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			return ((List) inputElement).toArray();
		}
	}

	/**
	 * Given the search text, this method searches, in the BT of the currently
	 * active BTEditor, those nodes whose ID contains <code>text</code>,
	 * ignoring case. It stores, in {@link NodesSearcher#searchResult}, the set
	 * of nodes with a matching ID. Also it updates the
	 * {@link NodesSearcher#resultsTable} and
	 * {@link NodesSearcher#targetEditorName} fields.
	 * 
	 * @param text
	 *            the search text.
	 */
	private void performSearch(String text) {
		BTEditor activeEditor = Utilities.getActiveBTEditor();
		this.searchResult.clear();

		if (activeEditor != null) {
			this.targetEditor = activeEditor;
			BT currentBT = activeEditor.getBT();
			searchNode(text, this.searchResult, currentBT.getRoot());
			this.targetEditorName.setText("Searched in: " + this.targetEditor.getTitle());
		}

		this.global.layout();
		this.resultsTable.refresh();
	}

	/**
	 * Method that recursivelly perform the search, starting from
	 * <code>currentNode</code>. If stores in <code>foundNodes</code> the
	 * matching nodes. <code>text</code> is the search text.
	 */
	private void searchNode(String text, List<Identifier> foundNodes, BTNode currentNode) {
		if (currentNode.getID().toString().toLowerCase().contains(text)) {
			foundNodes.add(currentNode.getID());
		}

		for (BTNode child : currentNode.getChildren()) {
			searchNode(text, foundNodes, child);
		}
	}
}
