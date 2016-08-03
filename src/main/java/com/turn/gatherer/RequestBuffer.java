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

public class RequestBuffer<T> {
	protected final ArrayList<T> parts;

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

	protected void overwriteNonNullPartsFrom(RequestBuffer<T> source, int startIndex, int endIndex) {
		endIndex = Math.min(endIndex, Math.min(this.size(), source.size()));
		for (int i = startIndex; i < endIndex; i++) {
			T part = source.get(i);
			if (part != null) {
				this.set(i, part);
			}
		}
	}

	/**
	 * Merges the two inputs into one new instance of com.turn.gatherer.RequestBuffer. For each part, if one argument has a non-null value
	 * and the other has a null, the non-null will be used. If both have a non-null, b's version will be used.
	 * @param a
	 * @param b
	 * @param <S>
	 * @return
	 */
	public static <S> RequestBuffer<S> mergeNewOverwriteWithB(RequestBuffer<S> a, RequestBuffer<S> b) {
		RequestBuffer<S> ret = new RequestBuffer<>(Math.max(a.size(), b.size()));

		for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
			S part = b.get(i);
			if (part == null)
				part = a.get(i);

			ret.set(i, part);
		}

		// If the lengths are unequal then copy from the longer one
		if (a.size() != b.size()) {
			if (a.size() > b.size()) {
				ret.overwriteNonNullPartsFrom(a, b.size(), a.size());
			} else {
				ret.overwriteNonNullPartsFrom(b, a.size(), b.size());
			}
		}


		return ret;
	}
}
