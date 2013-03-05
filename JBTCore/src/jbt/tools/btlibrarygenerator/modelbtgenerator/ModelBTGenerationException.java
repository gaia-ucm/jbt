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
package jbt.tools.btlibrarygenerator.modelbtgenerator;

/**
 * Exception that is thrown when there is any error when generating an
 * expression for a model BT.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelBTGenerationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ModelBTGenerationException(String message) {
		super(message);
	}

	public ModelBTGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
