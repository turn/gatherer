# Gatherer

A small datastructure that gathers pieces of a request from multiple sources then calls a callback once all pieces arrive or a timeout expires.

## Why?

Java networking codes tend to support only point-to-point communication such as sending a message to another host or performing a request and expecting a reply in some way.

The Gatherer datastructure is intended to help implement more complex topologies.

In particular the target is low-latency systems that cannot use things like Storm or Heron.

**Example:**
System A may make a request to System C that contains data from System B. Traditionally this can be done via two request-reply calls.

A better pattern is A sends request to B, B sends reply to C, and A performs the call to C. This forms a sort of triangle. The upside is that the network traversals happen in parallel and A doesn't see or process B's reply. The downside is that C must join the two requests together.

Gatherer is intended to be the core of the join that C does; it *gathers* the two parts that make up the request.

## How?

A Gatherer message is a [buffer](https://github.com/turn/gatherer/blob/master/src/main/java/com/turn/gatherer/RequestBuffer.java) (essentially an array) where each part has a pre-determined index. A request ID is used to match each part with the right message.

The first received message part with a certain request ID creates a buffer for that message and starts the timer. Parts can arrive in any order (think bittorrent). Once all parts arrive, or the timeout expires, a [handler](https://github.com/turn/gatherer/blob/master/src/main/java/com/turn/gatherer/RequestHandler.java) is called with the message.

Timeouts are kept using Netty's `HashedWheelTimer`. No other part of Netty is used. You're expected to provide your own message system, [grpc-java](https://github.com/grpc/grpc-java) works well.

## Usage

Use the `GathererBuilder` to create a `Gatherer`:

	public static final int A_PART = 0;
	public static final int B_PART = 1;

	Gatherer<Long> gatherer = new GathererBuilder<Long>()
			.handler(buffer -> {
				if (buffer.get(0) != null) {
					System.out.println("Got A! " + buffer.get(A_PART).longValue());
				}
				if (buffer.get(0) != null) {
					System.out.println("Got B! " + buffer.get(B_PART).longValue());
				}
			})
			.numParts(2)
			.timeoutDuration(10).unit(TimeUnit.MILLISECONDS)
			.createWheelGatherer();

Use `RequestID` to identify requests:

	RequestID id = new RequestID();

Call `receive()` when you receive parts using your own asynchronous messaging system:

	void receiveFromA(RequestID id, Long messagePart) {
		gatherer.receive(id, A_PART, messagePart);
	}

	void receiveFromB(RequestID id, Long messagePart) {
        gatherer.receive(id, B_PART, messagePart);
    }

Note that the handler must be very fast so as to not block the timer thread. I.e. if you have to do real work then submit into a thread pool.