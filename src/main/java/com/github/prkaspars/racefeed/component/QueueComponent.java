package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import com.github.prkaspars.racefeed.model.Car;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

@Component
public class QueueComponent {
  private TransferQueue<Car.State> states = new LinkedTransferQueue<>();
  private TransferQueue<CarStatus> statuses = new LinkedTransferQueue<>();
  private TransferQueue<Event> events = new LinkedTransferQueue<>();

  public boolean offerState(Car.State state) {
    return states.offer(state);
  }

  public Car.State takeState() throws InterruptedException {
    return states.take();
  }

  public boolean offerStatus(CarStatus status) {
    return statuses.offer(status);
  }

  public CarStatus takeStatus() throws InterruptedException {
    return statuses.take();
  }

  public boolean offerEvent(Event event) {
    return events.offer(event);
  }

  public boolean addAllEvents(Collection<Event> collection) {
    return events.addAll(collection);
  }

  public Event takeEvent() throws InterruptedException {
    return events.take();
  }
}
