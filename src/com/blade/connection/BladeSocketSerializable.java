package com.blade.connection;

/**
 * @author: cold_blade
 * @date: 2017年5月24日
 * @version 1.0
 */
public interface BladeSocketSerializable {
  long readObject(BladeSocketInputStream bsis);

  long writeObject(BladeSocketOutputStream bsos);
}
