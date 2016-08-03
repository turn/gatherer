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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GathererTest {

	@BeforeMethod
	public void setUp() throws Exception {
	}

	@AfterMethod
	public void tearDown() throws Exception {
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTimeoutMaxErrorRange() throws Exception {
		new GathererBuilder<Long>()
				.handler(null)
				.numParts(1)
				.timeoutDuration(10)
				.timeoutMaxError(0)
				.createWheelGatherer();
	}

	@Test
	public void testTrivialOnePart() throws Exception {
		final AtomicInteger numHandlerCalls = new AtomicInteger(0);

		Gatherer<Long> gatherer = new GathererBuilder<Long>()
				.handler(buffer -> {
					Assert.assertEquals(buffer.get(0).longValue(), 123L);
					numHandlerCalls.incrementAndGet();
				})
				.numParts(1)
				.timeoutDuration(50).unit(TimeUnit.MILLISECONDS)
				.createWheelGatherer();

		Assert.assertEquals(numHandlerCalls.get(), 0);

		gatherer.receive(new RequestID(), 0, 123L);
		Assert.assertEquals(numHandlerCalls.get(), 1);

		gatherer.receive(new RequestID(), 0, 123L);
		Assert.assertEquals(numHandlerCalls.get(), 2);
	}

	@Test
	public void testImmediateTwoPart() throws Exception {
		final AtomicInteger numHandlerCalls = new AtomicInteger(0);

		Gatherer<Long> gatherer = new GathererBuilder<Long>()
				.handler(buffer -> {
					Assert.assertEquals(buffer.get(0).longValue(), 123L);
					Assert.assertEquals(buffer.get(1).longValue(), 456L);
					numHandlerCalls.incrementAndGet();
				})
				.numParts(2)
				.timeoutDuration(50).unit(TimeUnit.MILLISECONDS)
				.createWheelGatherer();

		Assert.assertEquals(numHandlerCalls.get(), 0);

		RequestID id;

		// first request
		id = new RequestID();
		gatherer.receive(id, 0, 123L);
		Assert.assertEquals(numHandlerCalls.get(), 0);
		gatherer.receive(id, 1, 456L);
		Assert.assertEquals(numHandlerCalls.get(), 1);

		// second request
		id = new RequestID();
		gatherer.receive(id, 1, 456L);
		Assert.assertEquals(numHandlerCalls.get(), 1);
		gatherer.receive(id, 0, 123L);
		Assert.assertEquals(numHandlerCalls.get(), 2);
	}

	@Test
	public void testSimpleTwoPartTimeout() throws Exception {
		final AtomicInteger numHandlerCalls = new AtomicInteger(0);

		Gatherer<Long> gatherer = new GathererBuilder<Long>()
				.handler(buffer -> {
					Assert.assertEquals(buffer.get(0).longValue(), 123L);
					Assert.assertNull(buffer.get(1));
					numHandlerCalls.incrementAndGet();
				})
				.numParts(2)
				.timeoutDuration(10).unit(TimeUnit.MILLISECONDS)
				.createWheelGatherer();

		Assert.assertEquals(numHandlerCalls.get(), 0);

		RequestID id = new RequestID();
		gatherer.receive(id, 0, 123L);

		// Sleep to allow the request to time out
		Thread.sleep(15);

		Assert.assertEquals(numHandlerCalls.get(), 1);
	}
}