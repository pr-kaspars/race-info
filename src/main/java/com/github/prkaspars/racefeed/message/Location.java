package com.github.prkaspars.racefeed.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.Math.*;

public class Location {
  private double latitude;
  private double longitude;

  @JsonCreator
  public Location(@JsonProperty("lat") double latitude, @JsonProperty("long") double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @JsonProperty("lat")
  public double getLatitude() {
    return latitude;
  }

  @JsonProperty("long")
  public double getLongitude() {
    return longitude;
  }

  /**
   * Returns distance between two locations.
   *
   * @param location other location
   * @return distance in meters
   */
  public double distance(Location location) {
    double d = PI / 180.0;
    double latA = latitude * d;
    double latB = location.latitude * d;
    double latD = (latitude - location.latitude) * d;
    double lonD = (longitude - location.longitude) * d;

    double a = pow(sin(latD / 2), 2) + cos(latA) * cos(latB) * pow(sin(lonD / 2), 2);
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));

    return 6371.0 * 1000 * c;
  }
}
