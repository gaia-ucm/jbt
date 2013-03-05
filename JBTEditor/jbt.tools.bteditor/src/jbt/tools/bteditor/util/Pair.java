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

import java.io.Serializable;

/**
 * Pair represents a pair of objects
 * 
 * @author Wikipedia
 * @param <T>
 *            type of the first element of the pair.
 * @param <S>
 *            type of the second element of the pair.
 */
public class Pair<T, S> implements Serializable {
	private static final long serialVersionUID = 1L;
	/*
	 * First element of the pair.
	 */
	private T first;
	/*
	 * Second element of the pair.
	 */
	private S second;

	/**
	 * Constructs a Pair.
	 * 
	 * @param f
	 *            first element of the pair.
	 * @param s
	 *            second element of the pair.
	 */
	public Pair(T f, S s) {
		first = f;
		second = s;
	}

	/**
	 * Returns the first element of the pair.
	 * 
	 * @return the first element of the pair.
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * Returns the second element of the pair.
	 * 
	 * @return the second element of the pair.
	 */
	public S getSecond() {
		return second;
	}

	/**
	 * Sets the value of the first element of the pair.
	 * 
	 * @param f
	 *            value for the first element of the pair.
	 */
	public void setFirst(T f) {
		first = f;
	}

	/**
	 * Sets the value of the second element of the pair.
	 * 
	 * @param s
	 *            value for the second element of the pair.
	 */
	public void setSecond(S s) {
		second = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}
	
	public boolean equals(Object o){
		if(this==o)
			return true;
		
		if(o instanceof Pair){
			return first.equals(((Pair)o).first) && second.equals(((Pair)o).second); 
		}
		else{
			return false;
		}
	}
	
	public int hashCode(){
		return first.hashCode()+second.hashCode();
	}
}
