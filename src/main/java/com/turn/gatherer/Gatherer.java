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

/**
 * Gatherer collects pieces of a request. If all pieces are received (or a timeout expires) then the request is
 * processed.
 *
 * Construct using {@link GathererBuilder}.
 *
 * @param <T> type of each request part
 */
@SuppressWarnings("WeakerAccess")
public interface Gatherer<T> {
	void receive(RequestID id, int part, T data);
}
