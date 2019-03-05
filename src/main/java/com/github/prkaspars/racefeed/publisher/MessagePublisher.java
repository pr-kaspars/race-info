package com.github.prkaspars.racefeed.publisher;

import com.github.prkaspars.racefeed.exception.RuntimeInterruptedException;
import com.github.prkaspars.racefeed.util.InterruptedSupplier;

import java.util.function.Consumer;

import static java.lang.Thread.currentThread;

public class MessagePublisher<T> implements Runnable {
  private Consumer<T> consumer;
  private InterruptedSupplier<T> supplier;

  public MessagePublisher(InterruptedSupplier<T> supplier, Consumer<T> consumer) {
    this.consumer = consumer;
    this.supplier = supplier;
  }

  @Override
  public void run() {
    while (!currentThread().isInterrupted()) {
      try {
        consumer.accept(supplier.get());
      } catch (InterruptedException e) {
        throw new RuntimeInterruptedException("Problem receiving message", e);
      }
    }
  }
}
