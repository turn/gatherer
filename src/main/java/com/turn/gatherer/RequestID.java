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

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An identifier for requests. Convertible to/from a long, but kept as a separate class to for clearer semantics.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RequestID implements Serializable {
	private static final long serialVersionUID = 1L;

	private final long id;

	public RequestID() {
		id = System.nanoTime() ^ ThreadLocalRandom.current().nextLong();
	}

	public RequestID(long id) {
		this.id = id;
	}

	public long asLong() {
		return id;
	}

	@Override
	public int hashCode() {
		return (int)id;
	}

	@Override
	public String toString() {
		return Long.toString(id);
	}
}
