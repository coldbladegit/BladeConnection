package com.blade.connection;

import java.io.IOException;

/**
 * @author: cold_blade
 * @date: 2017年5月24日
 * @version 1.0
 */
public interface BladeSocketSerializable {
  long readObject(BladeSocketInputStream bsis) throws IOException;

  long writeObject(BladeSocketOutputStream bsos) throws IOException;
}
