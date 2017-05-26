package com.blade.connection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * 状态广播线程,用以广播连接当前所处的状态
 * @author cold_blade
 * @date 2017年5月26日
 * @version 1.0
 */
final class StatusBroadcastThread extends Thread {
  private static final int STATUS_QUEUE_CAPACITY = 10;
  private CopyOnWriteArrayList<BladeSocketStatusListener> listeners = new CopyOnWriteArrayList<>();
  private ArrayBlockingQueue<BladeConnectionStatus> statusQueue = new ArrayBlockingQueue<>(
      STATUS_QUEUE_CAPACITY);

  public void register(BladeSocketStatusListener listener) {
    listeners.addIfAbsent(listener);
  }

  /**
   * 如果状态改变太频繁,从而导致状态队列处理不过来,则抛掉后面的状态
   * @param status
   */
  public void broadcast(BladeConnectionStatus status) {
    if (statusQueue.size() == STATUS_QUEUE_CAPACITY) {
      return;
    }
    try {
      statusQueue.put(status);
    } catch (InterruptedException e) {
      interrupted();//清除中断痕迹
    }
  }

  /**
   * 往连接状态队列中添加一个特殊状态,以结束当前线程
   * 队列中之前残留的状态将被忽略
   */
  public void release() {
    statusQueue.clear();
    try {
      statusQueue.put(BladeConnectionStatus.CONNECT_STOPPED);
    } catch (InterruptedException e) {
      statusQueue.clear();
      interrupted();//清除中断痕迹
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        BladeConnectionStatus status = statusQueue.take();
        if (BladeConnectionStatus.CONNECT_STOPPED == status) {
          break;
        }
        for (BladeSocketStatusListener elem : listeners) {
          elem.connectStatusChanged(status);
        }
      } catch (InterruptedException e) {
        interrupted();//清除中断痕迹
        break;
      }
    }
    statusQueue.clear();
    listeners.clear();
  }
}
