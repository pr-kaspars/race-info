package com.github.prkaspars.racefeed.publisher;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static java.lang.Thread.currentThread;

public class MessagePublisher<T> implements Runnable {
  private Consumer<T> consumer;
  private BlockingQueue<T> queue;

  public MessagePublisher(BlockingQueue<T> queue, Consumer<T> consumer) {
    this.consumer = consumer;
    this.queue = queue;
  }

  @Override
  public void run() {
    while (!currentThread().isInterrupted()) {
      try {
        consumer.accept(queue.take());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
