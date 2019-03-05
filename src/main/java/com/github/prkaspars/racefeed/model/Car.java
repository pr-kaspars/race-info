package com.github.prkaspars.racefeed.model;

import com.github.prkaspars.racefeed.message.Location;

/**
 * Class represents a moving car.
 */
public class Car {
  private static final double mph = 2.2369363;
  private final int id;
  private Location location;
  private State state = new State(0, 0, 0);

  public Car(int id) {
    this.id = id;
  }

  /**
   * Displaces car and returns new state after displacement.
   *
   * @param time     time of the event
   * @param location new location
   * @return new state
   */
  public State displace(long time, Location location) {
    state = (this.location == null) ? new State(time, 0, 0) : displace(time, this.location.distance(location));
    this.location = location;
    return state;
  }

  /**
   * Returns new state after displacement.
   *
   * @param time         time of the event
   * @param displacement distance in meters
   * @return new state
   */
  private State displace(long time, double displacement) {
    return new State(time, state.distance + displacement, displacement / ((time - state.time) / 1000.0));
  }

  /**
   * Immutable class that represents current state of the car.
   */
  public class State {
    private long time;
    private double distance;
    private double speed;

    public State(long time, double distance, double speed) {
      this.time = time;
      this.distance = distance;
      this.speed = speed;
    }

    /**
     * Returns cars's identified.
     *
     * @return ID
     */
    public int getId() {
      return id;
    }

    /**
     * Returns state timestamp in milliseconds.
     *
     * @return state timestamp
     */
    public long getTime() {
      return time;
    }

    /**
     * Returns total travelled distance in meters.
     *
     * @return total distance
     */
    public double getDistance() {
      return distance;
    }

    /**
     * Returns current speed in m/s.
     *
     * @return current speed
     */
    public double getSpeed() {
      return speed;
    }

    /**
     * Returns current speed in mph.
     *
     * @return current speed
     */
    public int getSpeedMph() {
      return (int) Math.round(speed * mph);
    }

    /**
     * Returns true if both states have the same ID.
     *
     * @param s other state
     * @return true is IDs are equal
     */
    public boolean isIdEqual(State s) {
      return id == s.getId();
    }

    /**
     * Return difference between two distances.
     *
     * @param state other state
     * @return distance difference
     */
    public double delta(State state) {
      return Math.abs(distance - state.distance);
    }
  }
}
