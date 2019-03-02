package com.github.prkaspars.racefeed.model;

/**
 * Class represents moving body.
 */
public class Body {
  private static final double mph = 2.2369363;
  private final int id;
  private State state;

  public Body(int id) {
    this.id = id;
  }

  /**
   * Displaces body and returns the object's state after displacement.
   *
   * @param time         time of the event
   * @param displacement distance in meters
   * @return new state
   */
  public State displace(long time, double displacement) {
    if (state == null) {
      return state = new State(time, displacement, 0);
    }

    return state = new State(time, state.distance + displacement, displacement / ((time - state.time) / 1000.0));
  }

  /**
   * Immutable class that represents body's current state.
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
     * Returns body's identified.
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
  }
}
