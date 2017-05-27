package com.blade.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 与指定服务器进行连接,默认连接超时值{@code DEFAULT_TIMEOUT DEFAULT_TIMEUNIT}为30秒
 * 该连接拥有一个对外广播当前的状态的线程,如果对连接当前所处的状态非常关心,需要调用{@code addStatusListener}
 * 接口进行注册
 * @author cold_blade
 * @date 2017年5月26日
 * @version 1.0
 */
public final class BladeConnection {
  /**连接的默认超时值为30秒**/
  private final static long DEFAULT_TIMEOUT = 30;
  private final static TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;
  private final ReentrantReadWriteLock statusLock = new ReentrantReadWriteLock();
  private Socket socket = new Socket();
  private BladeSocketAddress sockAddr;
  private long timeOut;
  private TimeUnit timeUnit;
  private BladeConnectionStatus status = BladeConnectionStatus.DIS_CONNECTED;
  private BladeSocketInputStream bsis;
  private BladeSocketOutputStream bsos;
  private StatusBroadcastThread sbt;

  public BladeConnection(BladeSocketAddress sockAddr) {
    this(sockAddr, DEFAULT_TIMEOUT);
  }

  public BladeConnection(BladeSocketAddress sockAddr, long timeOut) {
    this(sockAddr, timeOut, DEFAULT_TIMEUNIT);
  }

  public BladeConnection(BladeSocketAddress sockAddr, long timeOut, TimeUnit timeUnit) {
    if (null == sockAddr) {
      throw new IllegalArgumentException("sockAddr is null");
    }
    this.sockAddr = sockAddr;
    this.timeOut = timeOut;
    this.timeUnit = timeUnit;
    this.sbt = new StatusBroadcastThread();
    this.sbt.start();
  }

  /**
   * 添加连接状态改变的事件监听
   * @param listener
   */
  public void addStatusListener(BladeSocketStatusListener listener) {
    sbt.register(listener);
  }

  /**
   * 建立与服务的连接
   * @return true 成功建立连接,否则false
   */
  public boolean doConnect() {
    setConnectionStatus(BladeConnectionStatus.START_CONNECT);
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

  /**
   * 从socket流的缓冲区中读取一个{@code BladeSocketSerializable}对象
   * @param obj
   * @return 返回具体读取的长度
   * @throws IOException
   * @throws IllegalStateException 未建立连接
   */
  public long readObject(BladeSocketSerializable obj) throws IOException, IllegalStateException {
    if (!isConnected()) {
      throw new IllegalStateException("还未建立连接");
    }
    return obj.readObject(bsis);
  }

  /**
   * 将一个{@code BladeSocketSerializable}对象写入socket流的缓冲区中
   * @param obj
   * @return 返回具体写入的长度
   * @throws IOException
   * @throws IllegalStateException 未建立连接
   */
  public long writeObject(BladeSocketSerializable obj) throws IOException, IllegalStateException {
    if (!isConnected()) {
      throw new IllegalStateException("还未建立连接");
    }
    return obj.writeObject(bsos);
  }

  /**
   * 断开与服务的连接,并释放系统的IO资源
   */
  public void closeConnect() {
    if (isConnected()) {
      releaseSystemResource();
      setConnectionStatus(BladeConnectionStatus.DIS_CONNECTED);
    }
  }

  /**
   * 销毁该连接,释放系统的IO资源的同时结束状态广播的线程
   */
  public void destroyConnect() {
    closeConnect();
    sbt.release();
  }

  /**
   * 获取当前连接所处的状态
   * @return {@code BladeConnectionStatus}
   */
  public BladeConnectionStatus getConnectionStatus() {
    BladeConnectionStatus connStatus;
    statusLock.readLock().lock();
    connStatus = status;
    statusLock.readLock().unlock();
    return connStatus;
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
    }
  }
}
