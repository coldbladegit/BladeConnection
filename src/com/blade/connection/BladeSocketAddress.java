package com.blade.connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author cold_blade
 * @date 2017年5月26日
 * @version 1.0
 */
public final class BladeSocketAddress {
  private String ip;
  private int port;
  private InetAddress addr;

  public BladeSocketAddress(String ip, int port) throws UnknownHostException {
    if (null == ip) {
      throw new IllegalArgumentException("ip is null");
    }
    addr = InetAddress.getByName(ip);
    this.ip = ip;
    this.port = port;
  }

  public String getIP() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  public InetAddress getAddress() {
    return addr;
  }
}
