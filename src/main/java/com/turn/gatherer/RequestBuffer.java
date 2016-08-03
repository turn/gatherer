/*
 * Copyright 2016 Turn Inc.
 *
 * Turn licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.turn.gatherer;

import java.util.ArrayList;

/**
 * A buffer that represents a request.
 *
 * @param <T> type of each request part
 */
@SuppressWarnings("WeakerAccess")
public class RequestBuffer<T> {
	private final ArrayList<T> parts;

	public RequestBuffer(int len) {
		parts = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			parts.add(null);
		}
	}

	public final int size() {
		return parts.size();
	}

	public T get(int index) {
		return parts.get(index);
	}

	public RequestBuffer<T> set(int index, T part) {
		parts.set(index, part);
		return this;
	}

	/**
	 *
	 * @return true if all parts are set, false otherwise.
	 */
	public boolean isFull() {
		return parts.stream().noneMatch(p -> p == null);
	}
}
