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
package jbt.tools.bteditor.actions;

import java.io.IOException;
import jbt.tools.bteditor.BTCPPManager;
import jbt.tools.bteditor.model.BT;
import org.eclipse.jface.action.Action;

/**
 * Action that saves a behaviour tree in a CPP file. If there is currently an
 * open behaviour tree coming from a file and with the same name, the saving
 * process fails, and an exception is thrown.
 * 
 * @author Ricardo Juan Palma Durán & Fernando Matarrubia
 * 
 */
public class ExportToCppAction extends Action {
	/** The tree to save. */
	private BT btToSave;
	/** The name of the file where the tree must be saved. */
	private String fileName;

	/**
	 * Constructor.
	 * 
	 * @param tree
	 *            the tree to save.
	 * @param fileName
	 *            the name of the file where the tree is going to be saved.
	 */
	public ExportToCppAction(BT tree, String fileName) {
		this.btToSave = tree;
		this.fileName = fileName;
	}

	/**
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run(){
		try {
			BTCPPManager.export(this.btToSave, this.fileName);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
