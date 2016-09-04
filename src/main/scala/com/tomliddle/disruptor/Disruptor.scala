package com.tomliddle.disruptor

import java.util.concurrent.TimeUnit

object Disruptor {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
  }
}




trait CircularBuffer[T] {

	protected val buffer: IndexedSeq[Option[T]]
	protected val readIdx: Int
	protected val writeIdx: Int
	lazy val length = buffer.length

	def insert(t: T): CircularBuffer[T] = {
		// Add one on to the next write index
		val nWriteIdx = (writeIdx + 1) % length
		// If our readIdx == writeIdx we need to advance the readIdx as the last value will be overwritten
		val nReadIdx = if (buffer(writeIdx).isDefined && readIdx == writeIdx) nWriteIdx else readIdx
		CircularBuffer(buffer.updated(writeIdx, Some(t)), nReadIdx, nWriteIdx)
	}

	def headOption: Option[T] = buffer(readIdx)

	def tail: CircularBuffer[T] = CircularBuffer(buffer, (readIdx + 1) % length, writeIdx)

}

object CircularBuffer {
	def apply[T](length: Int): CircularBuffer[T] = CircularBuffer(IndexedSeq[Option[T]]().padTo(length, None))

	protected def apply[T](circularBuffer: IndexedSeq[Option[T]], start: Int = 0, end: Int = 0): CircularBuffer[T] = {
		require(circularBuffer.nonEmpty)

		new CircularBuffer[T] {
			override val buffer = circularBuffer
			override val readIdx = start
			override val writeIdx = end
		}
	}
}

//https://lmax-exchange.github.io/disruptor/files/Disruptor-1.0.pdf
trait RingBuffer[T] {

	val consumers = IndexedSeq[Consumer[T]]()

	def insert(t: T): Unit

	def nextEntry: T
}

class RingBufferImpl[T](val size: Int) extends RingBuffer[T] {

	val buffer: Array[T] = new Array[T](size)

	//def apply[T](length: Int): RingBuffer[T] = new RingBuffer(1000)

	def insert(t: T): Unit = {

	}

	def nextEntry: T = {
		buffer(0)
	}

	// Claim

	// Write

	// Commit - allows all consumers to read, advance cursor

}

trait ProducerBarrier[T] {
	def entry(sequence: Long): T

	/** Delegate a call to the {@link RingBuffer#getCursor()}
	  *
	  *  @return value of the cursor for entries that have been published.
	  */
	def cursor: Long

	/** Claim the next T in the ring buffer for a Producer
	  */
	def nextEntry: T


	/** Commit to Ring buffer to make it visible
	  */
	def commit(entry: T)
}

trait ConsumerBarrier[T] {

	def getEntry(sequence: Long): T

	/** Wait for the given sequence to be available for consumption.
	  *
	  *  @param sequence to wait for
	  *  @return the sequence up to which is available
	  */
	def waitFor(sequence: Long): Long

	/** Wait for the given sequence to be available for consumption with a time out.
	  *
	  *  @param sequence to wait for
	  *  @param timeout value
	  *  @param units for the timeout value
	  *  @return the sequence up to which is available
	  */
	def waitFor(sequence: Long, timeout: Long, units: TimeUnit): Long

	/** Delegate a call to the {@link RingBuffer#getCursor()}
	  *
	  *  @return value of the cursor for entries that have been published.
	  */
	def getCursor: Long

	/** The current alert status for the barrier.
	  *
	  *  @return true if in alert otherwise false.
	  */
	def isAlerted: Boolean

	/** Alert the consumers of a status change and stay in this status until cleared.
	  */
	def alert()

	/** Clear the current alert status.
	  */
	def clearAlert()
}


trait Producer[T] {

	val ringBuffer: RingBuffer[T]

	def onData(data: T): Unit

}

class ProducerImpl[T](val ringBuffer: RingBuffer[T]) extends Producer[T] {

	def onData(data: T): Unit = {
		// claim

		// notify

	}
}


trait Consumer[T] {

	val ringBuffer: RingBuffer[T]


}

class ConsumerImpl[T](val ringBuffer: RingBuffer[T]) extends Consumer[T] {

	def onData(data: T): Unit = {
		// claim

		// notify

	}
}