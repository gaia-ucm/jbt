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

import jbt.tools.bteditor.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jbt.tools.bteditor.model.ConceptualBTNode;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * This transfer class transfers {@link ConceptualBTNode} objects. However, only
 * the pair "type"-"name" is transfered, and recovered as a Pair<String,String>
 * whose first element is "type" and whose second element is "name".
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ConceptualBTNodeTransfer extends ByteArrayTransfer {
	private static final String TYPENAME = "ConceptualBTNodeTransfer";
	private static final int TYPEID = registerType(TYPENAME);

	private static ConceptualBTNodeTransfer instance = null;

	private ConceptualBTNodeTransfer() {
	}

	public static ConceptualBTNodeTransfer getInstance() {
		if (instance == null) {
			instance = new ConceptualBTNodeTransfer();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	protected String[] getTypeNames() {
		return new String[] { TYPENAME };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.Transfer#validate(java.lang.Object)
	 */
	protected boolean validate(Object object) {
		return (object != null && object instanceof ConceptualBTNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object,
	 * org.eclipse.swt.dnd.TransferData)
	 */
	public void javaToNative(Object object, TransferData transferData) {
		if (!validate(object)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}

		if (!isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}

		ConceptualBTNode node = (ConceptualBTNode) object;

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream writeOut = new ObjectOutputStream(out);

			writeOut.writeObject(node.getType());
			writeOut.writeObject(node.getName());

			byte[] buffer = out.toByteArray();
			writeOut.close();
			out.close();
			super.javaToNative(buffer, transferData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd
	 * .TransferData)
	 */
	public Object nativeToJava(TransferData transferData) {
		if (!isSupportedType(transferData)) {
			return null;
		}
		byte[] buffer = (byte[]) super.nativeToJava(transferData);
		if (buffer == null)
			return null;
		try {
			String type;
			String name;
			ByteArrayInputStream in = new ByteArrayInputStream(buffer);
			ObjectInputStream readIn = new ObjectInputStream(in);

			type = (String) readIn.readObject();
			name = (String) readIn.readObject();

			readIn.close();
			in.close();
			return new Pair<String, String>(type, name);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
