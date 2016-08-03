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

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Gatherer} that uses Netty's {@link HashedWheelTimer} to process timeouts.
 *
 * @param <T> type of each request part
 */
@SuppressWarnings("WeakerAccess")
public class HashedWheelGatherer<T> implements Gatherer<T> {

	protected RequestHandler<T> handler;
	private int numParts;
	private long timeoutDurationNs;

	private HashedWheelTimer hashedWheelTimer;
	private ConcurrentHashMap<RequestID, AugmentedRequestBuffer<T>> inflightRequests;

	private static class AugmentedRequestBuffer<S> extends RequestBuffer<S> {
		private Timeout timeout;

		public AugmentedRequestBuffer(int len, Timeout timeout) {
			super(len);

			this.timeout = timeout;
		}

	}

	/**
	 *
	 * @param handler Called with a request when all parts are received or it expires.
	 * @param numParts number of parts for each request
	 * @param timeoutDuration Expiration time for incomplete requests
	 * @param unit time unit for timeoutDuration
	 * @param timeoutMaxError a value in range (0 and 1], where the timeout might happen at time timeoutDuration + timeoutMaxError*timeoutDuration. A larger value means a more efficient data structure.
	 */
	public HashedWheelGatherer(RequestHandler<T> handler, int numParts, long timeoutDuration, TimeUnit unit, double timeoutMaxError) {

		if (timeoutMaxError <= 0 || timeoutMaxError > 1) {
			throw new IllegalArgumentException(String.format("timeoutMaxError must be in range (0, 1] (got %f)", timeoutMaxError));
		}

		timeoutDurationNs = unit.toNanos(timeoutDuration);
		inflightRequests = new ConcurrentHashMap<>();

		// create the wheel timer
		int numSteps = (int)Math.round(1. / timeoutMaxError);
		long tickDurationNs = Math.max(unit.toNanos(timeoutDuration) / numSteps, 1);

		hashedWheelTimer = new HashedWheelTimer(r -> {
			// Use daemon threads
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		}, tickDurationNs, TimeUnit.NANOSECONDS, numSteps);

		hashedWheelTimer.start();

		this.numParts = numParts;
		this.handler = handler;
	}

	@Override
	public void receive(RequestID id, int part, T data) {
		if (part >= numParts) {
			throw new IllegalArgumentException(String.format("Received part %d when max is %d.", part, numParts-1));
		}

		// handle the trivial case to avoid added complexity later
		if (numParts == 1) {
			handler.handle(new RequestBuffer<T>(this.numParts).set(part, data));
			return;
		}

		inflightRequests.compute(id, (k, buffer) -> {
			if (buffer != null) {
				// existing request
				buffer.set(part, data);
				if (buffer.isFull()) {
					buffer.timeout.cancel();
					handler.handle(buffer);
					return null; // request is fulfilled, remove from map
				}
			} else {
				// new request
				Timeout timeout = hashedWheelTimer.newTimeout(timeout1 -> {
					AugmentedRequestBuffer<T> buffer1 = inflightRequests.remove(id);
					if (buffer1 != null) {
						handler.handle(buffer1);
					}
				}, timeoutDurationNs, TimeUnit.NANOSECONDS);
				buffer = new AugmentedRequestBuffer<>(numParts, timeout);
				buffer.set(part, data);
			}
			return buffer;
		});
	}

}
