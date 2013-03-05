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
package jbt.tools.btlibrarygenerator.util;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import gatech.mmpm.ActionParameterType;

/**
 * General utilities used in the creation of BT libraries.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
@SuppressWarnings("unchecked")
public class Util {
	/**
	 * The {@link CodeFormatter} that is used to format source code files. It is
	 * statically initialized.
	 */
	static private CodeFormatter codeFormatter;

	/**
	 * Given a MMPM parameter type, this method returns a Class object
	 * representing such a type. MMPM has some built-in types that are not
	 * present in Java, so a conversion must be performed. The conversion is
	 * performed as follows:
	 * 
	 * <pre>
	 * if (type == ActionParameterType.BOOLEAN) {
	 * 	return Boolean.class;
	 * } else if (type == ActionParameterType.ENTITY_ID) {
	 * 	return String.class;
	 * } else if (type == ActionParameterType.ENTITY_TYPE) {
	 * 	return String.class;
	 * } else if (type == ActionParameterType.FLOAT) {
	 * 	return Float.class;
	 * } else if (type == ActionParameterType.INTEGER) {
	 * 	return Integer.class;
	 * } else if (type == ActionParameterType.STRING) {
	 * 	return String.class;
	 * } else if (type == ActionParameterType.PLAYER) {
	 * 	return String.class;
	 * } else if (type == ActionParameterType.COORDINATE) {
	 * 	return float[].class;
	 * } else if (type == ActionParameterType.DIRECTION) {
	 * 	return Integer.class;
	 * } else if (type == ActionParameterType.OBJECT) {
	 * 	return Object.class;
	 * }
	 * </pre>
	 * 
	 * If the input type is not recognized, an exception is thrown.
	 * 
	 * @param type
	 *            the type to be converted.
	 * @return a Class object representing the type <code>type</code>.
	 */
	public static Class fromMMPMParameterType(ActionParameterType type) {
		if (type == ActionParameterType.BOOLEAN) {
			return Boolean.class;
		} else if (type == ActionParameterType.ENTITY_ID) {
			return String.class;
		} else if (type == ActionParameterType.ENTITY_TYPE) {
			return String.class;
		} else if (type == ActionParameterType.FLOAT) {
			return Float.class;
		} else if (type == ActionParameterType.INTEGER) {
			return Integer.class;
		} else if (type == ActionParameterType.STRING) {
			return String.class;
		} else if (type == ActionParameterType.PLAYER) {
			return String.class;
		} else if (type == ActionParameterType.COORDINATE) {
			return float[].class;
		} else if (type == ActionParameterType.DIRECTION) {
			return Integer.class;
		} else if (type == ActionParameterType.OBJECT) {
			return Object.class;
		}

		throw new IllegalArgumentException("Unexpected action parameter type");
	}

	/**
	 * If a directory name does not contain the separator character at the very
	 * end, it is added. Otherwise, the same input directory is returned.
	 */
	public static String addDirectorySeparator(String directory) {
		/* If directory does not end in separator, add it. */

		if (!directory.endsWith(File.separator)) {
			return directory + File.separator;
		}

		return directory;
	}

	/**
	 * Checks if a file name already exists in the file system. If so, returns a
	 * new file name based on <code>fileName</code>, which represents a file
	 * that does not exist in the file system (the name is the same as
	 * <code>fileName</code> except for the fact that is has been appended a
	 * number at its end, before the file extension).
	 */
	public static String overwrites(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			return fileName;
		} else {
			int counter = 1;
			while (true) {
				String alternativeFileName = removeExtension(fileName) + counter++;
				String extension = getExtension(fileName);
				if (extension != null) {
					alternativeFileName += "." + extension;
				}
				file = new File(alternativeFileName);
				if (!file.exists()) {
					return alternativeFileName;
				}
			}
		}
	}

	/**
	 * Removes the extension from a file name. If the file name has no
	 * extension, it is left unchanged.
	 */
	public static String removeExtension(String fileName) {
		String parts[] = fileName.split("\\.");
		if (parts.length == 0) {
			return fileName;
		} else {
			String result = new String();
			for (int i = 0; i < parts.length - 1; i++) {
				result += parts[i] + ".";
			}
			return result.substring(0, result.length() - 1);
		}
	}

	/**
	 * Returns the extension of a file name, or null if it has none.
	 */
	public static String getExtension(String fileName) {
		String parts[] = fileName.split("\\.");
		if (parts.length == 0) {
			return null;
		} else {
			return parts[parts.length - 1];
		}
	}

	/**
	 * If a directory name contains the separator character at the very end, it
	 * is removed. Otherwise, the same input directory is returned.
	 */
	public static String removeDirectorySeparator(String directory) {
		/* If directory ends with separator, remove it. */

		if (directory.endsWith(File.separator)) {
			return directory.substring(0, directory.length() - 1);
		}

		return directory;
	}

	/**
	 * This method formats a source code file (its content is in
	 * <code>sourceCode</code>) according to the Eclipse SDK defaults formatting
	 * settings, and returns the formatted code.
	 * <p>
	 * Note that the input source code must be syntactically correct according
	 * to the Java 1.7 version. Otherwise, an exception is thrown.
	 * 
	 * @param sourceCode
	 *            the source code to format.
	 * @return the formatted source code.
	 */
	public static String format(String sourceCode) {
		try {
			TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT
					| CodeFormatter.F_INCLUDE_COMMENTS, sourceCode, 0, sourceCode.length(), 0,
					System.getProperty("line.separator"));

			IDocument document = new Document(sourceCode);

			edit.apply(document);

			return document.get();
		} catch (Exception e) {
			throw new RuntimeException("The input source code is not sintactically correct:\n\n" + sourceCode);
		}
	}

	/**
	 * Determines if a file exists.
	 */
	public static boolean fileExists(String fileName) {
		File f = new File(fileName);
		return f.exists();
	}

	/* Initialization of the code formatter. */
	static {
		/* Take default Eclipse formatting options. */
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		/* Initialize the compiler settings to be able to format 1.7 code */
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);

		/* Change the option to wrap each enum constant on a new line */
		options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
				DefaultCodeFormatterConstants.createAlignmentValue(true,
						DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));

		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, 78);

		/* Instanciate the default code formatter with the given options */
		codeFormatter = ToolFactory.createCodeFormatter(options);
	}
}
