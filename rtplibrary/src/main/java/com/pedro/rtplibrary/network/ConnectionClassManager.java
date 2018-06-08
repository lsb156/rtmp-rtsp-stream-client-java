/*
 *  Copyright (c) 2015, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.pedro.rtplibrary.network;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Class used to calculate the approximate bandwidth of a user's connection.
 * </p>
 * <p>
 * This class notifies all subscribed {@link ConnectionClassStateChangeListener} with the new
 * ConnectionClass when the network's ConnectionClass changes.
 * </p>
 */
public class ConnectionClassManager {

  private static final int BYTES_TO_BITS = 8;

  /**
   * The factor used to calculate the current bandwidth
   * depending upon the previous calculated value for bandwidth.
   *
   * The smaller this value is, the less responsive to new samples the moving average becomes.
   */
  private static final double DEFAULT_DECAY_CONSTANT = 0.05;

  /** Current bandwidth of the user's connection depending upon the response. */
  private ExponentialGeometricAverage mDownloadBandwidth =
      new ExponentialGeometricAverage(DEFAULT_DECAY_CONSTANT);
  private AtomicReference<ConnectionQuality> mCurrentBandwidthConnectionQuality =
      new AtomicReference<>(ConnectionQuality.UNKNOWN);
  private ConnectionClassStateChangeListener listener;
  /**
   * The lower bound for measured bandwidth in bits/ms. Readings
   * lower than this are treated as effectively zero (therefore ignored).
   */
  static final long BANDWIDTH_LOWER_BOUND = 10;

  // Singleton.
  private static class ConnectionClassManagerHolder {
    public static final ConnectionClassManager instance = new ConnectionClassManager();
  }

  /**
   * Retrieval method for the DownloadBandwidthManager singleton.
   *
   * @return The singleton instance of DownloadBandwidthManager.
   */
  public static ConnectionClassManager getInstance() {
    return ConnectionClassManagerHolder.instance;
  }

  // Force constructor to be private.
  private ConnectionClassManager() {
  }

  /**
   * Adds bandwidth to the current filtered latency counter. Sends a broadcast to all
   * {@link ConnectionClassStateChangeListener} if the counter moves from one bucket
   * to another (i.e. poor bandwidth -> moderate bandwidth).
   */
  public synchronized void addBandwidth(long bytes, long timeInMs) {

    //Ignore garbage values.
    if (timeInMs == 0 || (bytes) * 1.0 / (timeInMs) * BYTES_TO_BITS < BANDWIDTH_LOWER_BOUND) {
      return;
    }
    double bandwidth = (bytes) * 1.0 / (timeInMs) * BYTES_TO_BITS;
    if (listener != null) listener.onBandwidthStateChange(bandwidth);
  }

  /**
   * Resets the bandwidth average for this instance of the bandwidth manager.
   */
  public void reset() {
    if (mDownloadBandwidth != null) {
      mDownloadBandwidth.reset();
    }
    mCurrentBandwidthConnectionQuality.set(ConnectionQuality.UNKNOWN);
  }

  /**
   * Interface for listening to when ConnectionClassManager
   * changes state.
   */
  public interface ConnectionClassStateChangeListener {
    /**
     * The method that will be called when ConnectionClassManager
     * changes ConnectionClass.
     */
    void onBandwidthStateChange(double bandwidth);
  }

  /**
   * Method for adding new listeners to this class.
   *
   * @param listener {@link ConnectionClassStateChangeListener} to add as a listener.
   */
  public void register(ConnectionClassStateChangeListener listener) {
    this.listener = listener;
  }

  /**
   * Method for removing listeners from this class.
   */
  public void remove() {
    listener = null;
  }
}