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

import java.util.concurrent.TimeUnit;

public class GathererBuilder<T> {
	private RequestHandler<T> handler;
	private int numParts;
	private long timeoutDuration;
	private TimeUnit unit = TimeUnit.MILLISECONDS;
	private double timeoutMaxError = 0.2;

	public GathererBuilder<T> handler(RequestHandler<T> handler) {
		this.handler = handler;
		return this;
	}

	public GathererBuilder<T> numParts(int numParts) {
		this.numParts = numParts;
		return this;
	}

	public GathererBuilder<T> timeoutDuration(long timeoutDuration) {
		this.timeoutDuration = timeoutDuration;
		return this;
	}

	public GathererBuilder<T> unit(TimeUnit unit) {
		this.unit = unit;
		return this;
	}

	public GathererBuilder<T> timeoutMaxError(double timeoutMaxError) {
		this.timeoutMaxError = timeoutMaxError;
		return this;
	}

	public Gatherer<T> createWheelGatherer() {
		return new HashedWheelGatherer<>(handler, numParts, timeoutDuration, unit, timeoutMaxError);
	}
}