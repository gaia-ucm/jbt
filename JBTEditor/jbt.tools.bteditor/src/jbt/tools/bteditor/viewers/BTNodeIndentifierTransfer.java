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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.model.BTNode.Identifier;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * This transfer class transfers BTNode objects. However, only the BTNode identifier is
 * transfered.
 * 
 * @author Ricardo Juan Palma Durán
 *
 */
public class BTNodeIndentifierTransfer extends ByteArrayTransfer {
	private static final String TYPENAME = "BTNodeIndentifierTransfer";
	private static final int TYPEID = registerType(TYPENAME);
	
	private static BTNodeIndentifierTransfer instance = null;

	private BTNodeIndentifierTransfer(){}

	public static BTNodeIndentifierTransfer getInstance(){
		if(instance == null){
			instance = new BTNodeIndentifierTransfer();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	protected String[] getTypeNames(){
		return new String[]{TYPENAME};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	protected int[] getTypeIds(){
		return new int[]{TYPEID};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#validate(java.lang.Object)
	 */
	protected boolean validate(Object object){
		return(object != null && object instanceof BTNode);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object,
	 * org.eclipse.swt.dnd.TransferData)
	 */
	public void javaToNative(Object object, TransferData transferData){
		if(!validate(object)){
			DND.error(DND.ERROR_INVALID_DATA);
		}
		
		if( !isSupportedType(transferData)){
			DND.error(DND.ERROR_INVALID_DATA);
		}
		
		BTNode node=(BTNode)object;
		
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream writeOut = new ObjectOutputStream(out);
	
			writeOut.writeObject(node.getID());
			
			byte[] buffer = out.toByteArray();
			writeOut.close();
			out.close();
			super.javaToNative(buffer, transferData);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd
	 * .TransferData)
	 */
	public Object nativeToJava(TransferData transferData){
		if(!isSupportedType(transferData)){
			return null;
		}
		byte[] buffer = (byte[])super.nativeToJava(transferData);
		if(buffer == null)
			return null;
		try{
			ByteArrayInputStream in = new ByteArrayInputStream(buffer);
			ObjectInputStream readIn = new ObjectInputStream(in);
			Identifier id=(Identifier)readIn.readObject();
			
			readIn.close();
			in.close();
			return id;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
}
