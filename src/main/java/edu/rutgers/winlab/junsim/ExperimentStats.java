/*
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package edu.rutgers.winlab.junsim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert Moore
 */
public class ExperimentStats {
  // # Tx, # Rx, Min % Covered, Med. % Covered, Mean % Covered, Max % Covered,
  // 95% Coverage

  int numberTransmitters = 0;
  int numberReceivers = 0;

  // Array that holds statistics for coverages, mean contention, min contention,
  // and max contention.
  List<Float>[] statistics = (List<Float>[]) new List[4];
  boolean[] sorted = { false, false, false, false };
  // Indices into the above array.
  private final static int COVERAGE = 0;
  private final static int CONTENTION = 1;
  private final static int MIN_CONTENTION = 2;
  private final static int MAX_CONTENTION = 3;

  public ExperimentStats() {
    // Seems to be difficult to default initialize an array of generics
    // Initialize stastistics to empty lists here.
    for (int i = 0; i < statistics.length; ++i) {
      statistics[i] = new LinkedList<Float>();
    }
  }

  public void clear() {

    this.numberReceivers = 0;
    this.numberTransmitters = 0;
    for (List<Float> statistic : this.statistics) {
      statistic.clear();
    }
    for (Boolean sort : this.sorted) {
      sort = false;
    }
  }

  private void addStatistic(float val, int index) {
    synchronized (this.statistics[index]) {
      this.statistics[index].add(val);
      this.sorted[index] = false;
    }
  }

  void addCoverage(float coverage) {
    addStatistic(coverage, COVERAGE);
  }

  void addContention(float contention) {
    addStatistic(contention, CONTENTION);
  }

  void addMinContention(float contention) {
    addStatistic(contention, MIN_CONTENTION);
  }

  void addMaxContention(float contention) {
    addStatistic(contention, MAX_CONTENTION);
  }

  private float getMinStatistic(int index) {
    synchronized (this.statistics[index]) {
      if (this.statistics[index].size() == 0) {
        return Float.NaN;
      }

      if (!this.sorted[index]) {
        Collections.sort(this.statistics[index]);
        this.sorted[index] = true;
      }

      return this.statistics[index].get(0);
    }
  }

  float getMinCoverage() {
    return getMinStatistic(COVERAGE);
  }

  float getMinContention() {
    return getMinStatistic(CONTENTION);
  }

  float getMinMinContention() {
    return getMinStatistic(MIN_CONTENTION);
  }

  float getMinMaxContention() {
    return getMinStatistic(MAX_CONTENTION);
  }

  private float getMaxStatistic(int index) {
    synchronized (this.statistics[index]) {
      if (this.statistics[index].size() == 0) {
        return Float.NaN;
      }
      if (!this.sorted[index]) {
        Collections.sort(this.statistics[index]);
        this.sorted[index] = true;
      }
      return this.statistics[index].get(this.statistics[index].size() - 1);
    }
  }

  float getMaxCoverage() {
    return getMaxStatistic(COVERAGE);
  }

  float getMaxContention() {
    return getMaxStatistic(CONTENTION);
  }

  float getMaxMinContentions() {
    return getMaxStatistic(MIN_CONTENTION);
  }

  float getMaxMaxContentions() {
    return getMaxStatistic(MAX_CONTENTION);
  }

  private float getMedianStatistic(int index) {
    synchronized (this.statistics[index]) {
      if (this.statistics[index].size() == 0) {
        return Float.NaN;
      }
      if (!this.sorted[index]) {
        Collections.sort(this.statistics[index]);
        this.sorted[index] = true;
      }
      return this.statistics[index].get(this.statistics[index].size() / 2);
    }
  }

  float getMedianCoverage() {
    return getMedianStatistic(COVERAGE);
  }

  float getMedianContention() {
    return getMedianStatistic(CONTENTION);
  }

  float getMedianMinContention() {
    return getMedianStatistic(MIN_CONTENTION);
  }

  float getMedianMaxContention() {
    return getMedianStatistic(MAX_CONTENTION);
  }

  private float getMeanStatistic(int index) {
    synchronized (this.statistics[index]) {
      if (this.statistics[index].size() == 0) {
        return Float.NaN;
      }
      float totalCoverage = 0;
      for (Float c : this.statistics[index]) {
        totalCoverage += c;
      }

      return totalCoverage / this.statistics[index].size();
    }
  }

  float getMeanCoverage() {
    return getMeanStatistic(COVERAGE);
  }

  float getMeanContention() {
    return getMeanStatistic(CONTENTION);
  }

  float getMeanMinContention() {
    return getMeanStatistic(MIN_CONTENTION);
  }

  float getMeanMaxContention() {
    return getMeanStatistic(MAX_CONTENTION);
  }

  private float get95PercentileStatistic(int index) {
    synchronized (this.statistics[index]) {
      if (this.statistics[index].size() == 0) {
        return Float.NaN;
      }
      if (!this.sorted[index]) {
        Collections.sort(this.statistics[index]);
        this.sorted[index] = true;
      }

      return this.statistics[index]
          .get((int) (this.statistics[index].size() * .95));
    }
  }

  float get95PercentileCoverage() {
    return get95PercentileStatistic(COVERAGE);
  }

  float get95PercentileContention() {
    return get95PercentileStatistic(CONTENTION);
  }

  float get95PercentileMinContention() {
    return get95PercentileStatistic(MIN_CONTENTION);
  }

  float get95PercentileMaxContention() {
    return get95PercentileStatistic(MAX_CONTENTION);
  }
}
