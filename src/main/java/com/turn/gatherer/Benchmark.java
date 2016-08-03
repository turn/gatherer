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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple benchmark class.
 *
 * @author alugowski
 */
public class Benchmark {
	public static void main(String[] argv) {
		int NUM_REQUESTS = 100;
		final long TIME_LIMIT_NANOS = TimeUnit.MILLISECONDS.toNanos(15);

		final AtomicInteger numFails = new AtomicInteger(0);

		Gatherer<Long> gatherer = new GathererBuilder<Long>()
				.handler(buffer -> {
					long nanos = System.nanoTime() - buffer.get(0);
					if (nanos > TIME_LIMIT_NANOS)
						numFails.incrementAndGet();
					System.out.println(nanos / 1_000_000.0);
				})
				.numParts(2)
				.timeoutDuration(10).unit(TimeUnit.MILLISECONDS)
				.createWheelGatherer();

		for (int i = 0; i < NUM_REQUESTS; i++) {
			RequestID id = new RequestID();
			gatherer.receive(id, 0, System.nanoTime());
			try {
				Thread.sleep(0, 1);
			} catch (InterruptedException ignored) {
			}
		}

	}
}
