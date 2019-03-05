package com.github.prkaspars.racefeed.util;

public class Tuple<A, B, C> {
  private A a;
  private B b;
  private C c;

  public Tuple(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  public C getC() {
    return c;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Tuple)) {
      return false;
    }

    Tuple<A, B, C> t = (Tuple<A, B, C>) obj;
    return a.equals(t.a) && b.equals(t.b) && c.equals(t.c);
  }

  @Override
  public String toString() {
    return "{A=" + a + " B= " + b + " C=" + c + "}";
  }
}
