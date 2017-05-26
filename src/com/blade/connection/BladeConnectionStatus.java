package com.blade.connection;

/**
 * 定义连接所处的状态,{@code CONNECT_STOPPED}具有特殊意义,它表示整个连接将被销毁
 * @author cold_blade
 * @date 2017年5月26日
 * @version 1.0
 */
public enum BladeConnectionStatus {
  START_CONNECT, CONNECTED, DIS_CONNECTED, RECONNECTING, CONNECT_STOPPED
}
