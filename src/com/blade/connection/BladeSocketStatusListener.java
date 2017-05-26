package com.blade.connection;

/**
 * @author cold_blade
 * @date 2017年5月26日
 * @version 1.0
 */
public interface BladeSocketStatusListener {
  void connectStatusChanged(BladeConnectionStatus status);
}
