package com.blade.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 与指定服务器进行连接,{@code reconnCount}表示当连接断开后将去自动尝试重连的最大次数
 * 默认连接超时值{@code DEFAULT_TIMEOUT DEFAULT_TIMEUNIT}为30秒
 * @author cold_blade
 * @date 2017年5月26日
 * @version 1.0
 */
public final class BladeConnection {
  /**连接的默认超时值为30秒**/
  private final static long DEFAULT_TIMEOUT = 30;
  private final static TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;
  private final ReentrantReadWriteLock statusLock = new ReentrantReadWriteLock();
  private Socket socket;
  private BladeSocketAddress sockAddr;
  private long timeOut;
  private TimeUnit timeUnit;
  private int reconnCount;
  private BladeConnectionStatus status = BladeConnectionStatus.DIS_CONNECTED;
  private BladeSocketInputStream bsis;
  private BladeSocketOutputStream bsos;
  private StatusBroadcastThread sbt;

  public BladeConnection(BladeSocketAddress sockAddr, int reconnCount) {
    this(sockAddr, reconnCount, DEFAULT_TIMEOUT);
  }

  public BladeConnection(BladeSocketAddress sockAddr, int reconnCount, long timeOut) {
    this(sockAddr, reconnCount, timeOut, DEFAULT_TIMEUNIT);
  }

  public BladeConnection(BladeSocketAddress sockAddr, int reconnCount, long timeOut,
      TimeUnit timeUnit) {
    if (null == sockAddr) {
      throw new IllegalArgumentException("sockAddr is null");
    }
    this.sockAddr = sockAddr;
    this.reconnCount = reconnCount;
    this.timeOut = timeOut;
    this.timeUnit = timeUnit;
    this.sbt = new StatusBroadcastThread();
  }

  public void addStatusListener(BladeSocketStatusListener listener) {
    sbt.register(listener);
  }
  
  public boolean startConnect() {
    setConnectionStatus(BladeConnectionStatus.START_CONNECT);
    if (doConnect()) {
      //TODO:开启工作线程或心跳线程
      sbt.start();
      return true;
    }
    return false;
  }

  public void stopConnect() {
    if (isConnected()) {
      releaseSystemResource();
      setConnectionStatus(BladeConnectionStatus.DIS_CONNECTED);
    }
    //TODO:停止工作线程或心跳线程
    sbt.release();
  }

  public BladeConnectionStatus getConnectionStatus() {
    BladeConnectionStatus connStatus;
    statusLock.readLock().lock();
    connStatus = status;
    statusLock.readLock().unlock();
    return connStatus;
  }

  private boolean doReconnect() {
    if (isConnected()) {
      return true;
    }
    int count = reconnCount;
    setConnectionStatus(BladeConnectionStatus.RECONNECTING);
    while (count > 0) {
      if (doConnect()) {
        break;
      }
      --count;
    }
    return isConnected();
  }
  
  private boolean doConnect() {
    socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(sockAddr.getAddress(), sockAddr.getPort()),
          (int) timeUnit.toMillis(timeOut));
      bsis = new BladeSocketInputStream(socket.getInputStream());
      bsos = new BladeSocketOutputStream(socket.getOutputStream());
      setConnectionStatus(BladeConnectionStatus.CONNECTED);
    } catch (IOException e) {
      setConnectionStatus(BladeConnectionStatus.DIS_CONNECTED);
      releaseSystemResource();
      return false;
    }
    return true;
  }
  
  private void setConnectionStatus(BladeConnectionStatus status) {
    statusLock.writeLock().lock();
    this.status = status;
    statusLock.writeLock().unlock();
    sbt.broadcast(status);
  }

  private boolean isConnected() {
    boolean connected;
    statusLock.readLock().lock();
    connected = status == BladeConnectionStatus.CONNECTED;
    statusLock.readLock().unlock();
    return connected;
  }

  private void releaseSystemResource() {
    try {
      if (null != bsis) {
        bsis.close();
      }
    } catch (IOException e) {
    } finally {
      bsis = null;
    }
    try {
      if (null != bsos) {
        bsos.close();
      }
    } catch (IOException e) {
    } finally {
      bsos = null;
    }
    try {
      if (null != socket) {
        socket.close();
      }
    } catch (IOException e) {
    } finally {
      socket = null;
    }
  }
}
