/*
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package edu.rutgers.winlab.junsim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * @author Robert Moore
 * 
 */
public class ExperimentStats {
  // # Tx, # Rx, Min % Covered, Med. % Covered, Mean % Covered, Max % Covered,
  // 95% Coverage

  int numberTransmitters = 0;
  int numberReceivers = 0;

  // Array that holds statistics for coverages, mean collisions, min collisions,
  // and max collisions.
  //List<Float>[] statistics = (List<Float>[]){new LinkedList<Float>(), new LinkedList<Float>(),
  //  new LinkedList<Float>(), new LinkedList<Float>()};
  List<Float>[] statistics = (List<Float>[]) new List[4];
  boolean[] sorted = {false, false, false, false};
  // Indices into the above array.
  private final static int COVERAGE       = 0;
  private final static int COLLISIONS     = 1;
  private final static int MIN_COLLISIONS = 2;
  private final static int MAX_COLLISIONS = 3;

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

  private synchronized void addStatistic(float val, int index) {
    this.statistics[index].add(val);
    this.sorted[index] = false;
  }

  synchronized void addCoverage(float coverage) {
    addStatistic(coverage, COVERAGE);
  }

  synchronized void addCollisions(float collision) {
    addStatistic(collision, COLLISIONS);
  }

  synchronized void addMinCollisions(float collision) {
    addStatistic(collision, MIN_COLLISIONS);
  }

  synchronized void addMaxCollisions(float collision) {
    addStatistic(collision, MAX_COLLISIONS);
  }

  private synchronized float getMinStatistic(int index) {
    if(this.statistics[index].size() == 0){
      return Float.NaN;
    }
    
    if (!this.sorted[index]) {
      Collections.sort(this.statistics[index]);
      this.sorted[index] = true;
    }
    
    return this.statistics[index].get(0);
  }

  float getMinCoverage() {
    return getMinStatistic(COVERAGE);
  }

  float getMinCollisions() {
    return getMinStatistic(COLLISIONS);
  }

  float getMinMinCollisions() {
    return getMinStatistic(MIN_COLLISIONS);
  }

  float getMinMaxCollisions() {
    return getMinStatistic(MAX_COLLISIONS);
  }

  private float getMaxStatistic(int index) {
    if(this.statistics[index].size() == 0){
      return Float.NaN;
    }
    if (!this.sorted[index]) {
      Collections.sort(this.statistics[index]);
      this.sorted[index] = true;
    }
    return this.statistics[index].get(this.statistics[index].size() - 1);
  }

  float getMaxCoverage() {
    return getMaxStatistic(COVERAGE);
  }

  float getMaxCollisions() {
    return getMaxStatistic(COLLISIONS);
  }

  float getMaxMinCollisions() {
    return getMaxStatistic(MIN_COLLISIONS);
  }

  float getMaxMaxCollisions() {
    return getMaxStatistic(MAX_COLLISIONS);
  }

  private float getMedianStatistic(int index) {
    if(this.statistics[index].size() == 0){
      return Float.NaN;
    }
    if (!this.sorted[index]) {
      Collections.sort(this.statistics[index]);
      this.sorted[index] = true;
    }
    return this.statistics[index].get(this.statistics[index].size() / 2);
  }

  float getMedianCoverage() {
    return getMedianStatistic(COVERAGE);
  }

  float getMedianCollisions() {
    return getMedianStatistic(COLLISIONS);
  }

  float getMedianMinCollisions() {
    return getMedianStatistic(MIN_COLLISIONS);
  }

  float getMedianMaxCollisions() {
    return getMedianStatistic(MAX_COLLISIONS);
  }

  private float getMeanStatistic(int index) {
    if(this.statistics[index].size() == 0){
      return Float.NaN;
    }
    float totalCoverage = 0;
    for (Float c : this.statistics[index]) {
      totalCoverage += c;
    }

    return totalCoverage / this.statistics[index].size();
  }

  float getMeanCoverage() {
    return getMeanStatistic(COVERAGE);
  }

  float getMeanCollisions() {
    return getMeanStatistic(COLLISIONS);
  }

  float getMeanMinCollisions() {
    return getMeanStatistic(MIN_COLLISIONS);
  }

  float getMeanMaxCollisions() {
    return getMeanStatistic(MAX_COLLISIONS);
  }

  private float get95PercentileStatistic(int index) {
    if(this.statistics[index].size() == 0){
      return Float.NaN;
    }
    if (!this.sorted[index]) {
      Collections.sort(this.statistics[index]);
      this.sorted[index] = true;
    }

    return this.statistics[index].get((int) (this.statistics[index].size() * .95));
  }

  float get95PercentileCoverage() {
    return get95PercentileStatistic(COVERAGE);
  }

  float get95PercentileCollisions() {
    return get95PercentileStatistic(COLLISIONS);
  }

  float get95PercentileMinCollisions() {
    return get95PercentileStatistic(MIN_COLLISIONS);
  }

  float get95PercentileMaxCollisions() {
    return get95PercentileStatistic(MAX_COLLISIONS);
  }
}
