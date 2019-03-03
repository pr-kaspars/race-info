package com.github.prkaspars.racefeed.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class MessagePublisherTest {
  private BlockingQueue<Object> queue;
  private Consumer<Object> consumer;

  @BeforeEach
  public void setUp() {
    queue = mock(BlockingQueue.class);
    consumer = mock(Consumer.class);
  }

  @Test
  @DisplayName("run should take messages from queue and apply to consumer")
  public void run() throws InterruptedException {
    Object o = new Object();
    when(queue.take())
      .thenReturn(o)
      .thenThrow(new InterruptedException("break"));

    MessagePublisher<Object> publisher = new MessagePublisher<>(queue, consumer);
    assertThrows(RuntimeException.class, publisher::run);

    verify(queue, times(2)).take();
    verify(consumer, times(1)).accept(eq(o));
  }

  @Test
  @DisplayName("run should exit when thread is interrupted")
  public void runInterrupted() throws InterruptedException {
    Thread.currentThread().interrupt();
    new MessagePublisher<>(queue, consumer).run();

    verify(queue, times(0)).take();
    verify(consumer, times(0)).accept(any());
  }

}
