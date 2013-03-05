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
package jbt.tools.bteditor.util;

/**
 * Class that stores the file extensions that are supported by the application.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 * Modified by Fernando Matarrubia (adding c++ extension)
 */
public class Extensions {
	
	/** Extensions of the files that can contain behaviour trees. */
	private static final String[] BTFileExtensions = new String[] { "xbt",
			"xml" };
	
	/** Extension of the file that can contain an inline c++ version of the behaviour tree */
	private static final String[] BTCppFileExtensions = new String[] {"cpp"};
	
	/** Extension of the file that can contain an inline c++ version of the behaviour tree */
	private static final String[] BTInlFileExtensions = new String[] {"inl"};

	/** Extensions of MMPM domain files. */
	private static final String[] MMPMDomainfileExtensions = new String[] { "xml" };

	/**
	 * Returns an array containing the extensions of the files than can contain
	 * behaviour trees.
	 */
	public static String[] getBTFileExtensions() {
		return BTFileExtensions;
	}
	
	/**
	 * Returns an array containing the extensions of the special cpp files that
	 * can contain behaviour trees
	 */
	public static String[] getCppFileExtensions(){
		
		return BTCppFileExtensions;
	}
	
	/**
	 * Returns an array containing the extensions of the special inline files that
	 * can contain behaviour trees
	 */
	public static String[] getInlFileExtensions(){
		
		return BTInlFileExtensions;
	}

	/**
	 * Returns an array containing the extensions of MMPM domain files.
	 */
	public static String[] getMMPMDomainFileExtensions() {
		return MMPMDomainfileExtensions;
	}

	/**
	 * Given an array with file extensions, this method returns filters for
	 * those extensions to be used in SWT dialogs
	 */
	public static String[] getFiltersFromExtensions(String[] extensions) {
		String[] result = new String[extensions.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = "*." + extensions[i];
		}
		return result;
	}

	/**
	 * Given a set of file extensions, this method returns a single filter for
	 * all of those extensions.
	 */
	public static String getUnifiedFilterFromExtensions(String[] extensions) {
		String result = new String();
		for (int i = 0; i < extensions.length - 1; i++) {
			result += "*." + extensions[i] + ";";
		}
		result += "*." + extensions[extensions.length - 1];
		return result;
	}

	/**
	 * Joins two arrays of String.
	 */
	public static String[] joinArrays(String[] array1, String[] array2) {
		String[] result = new String[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	/**
	 * Joins a file name and an extension. If the file name ends with "."+
	 * <code>extension</code> , then <code>fileName</code> itself is returned.
	 * Otherwise, it returns <code>fileName+"."+extension</code>.
	 */
	public static String joinFileNameAndExtension(String fileName,
			String extension) {
		if (fileName.endsWith("." + extension)) {
			return fileName;
		} else {
			return fileName + "." + extension;
		}
	}

	private Extensions() {
	}
}
