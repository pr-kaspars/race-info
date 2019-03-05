package com.github.prkaspars.racefeed.util;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {

  T get() throws E;
}
