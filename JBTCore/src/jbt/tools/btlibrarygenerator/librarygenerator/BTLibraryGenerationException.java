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
package jbt.tools.btlibrarygenerator.librarygenerator;

/**
 * Exception thrown when there is an error creating an IBTLibrary.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTLibraryGenerationException extends Exception {
	private static final long serialVersionUID = 1L;

	public BTLibraryGenerationException(String message) {
		super(message);
	}

	public BTLibraryGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
