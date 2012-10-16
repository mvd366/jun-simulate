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

/**
 * @author Robert Moore
 * 
 */
public class ExperimentStats {
  // # Tx, # Rx, Min % Covered, Med. % Covered, Mean % Covered, Max % Covered,
  // 95% Coverage

  int numberTransmitters = 0;
  int numberReceivers = 0;

  List<Float> coverages = new LinkedList<Float>();
  boolean sorted = false;

  public void clear() {

    this.numberReceivers = 0;
    this.numberTransmitters = 0;
    this.coverages.clear();
    this.sorted = false;
  }

  synchronized void addCoverage(float coverage) {
    this.coverages.add(coverage);
    this.sorted = false;
  }

  float getMinCoverage() {
    if (!this.sorted) {
      Collections.sort(this.coverages);
      this.sorted = true;
    }
    return this.coverages.get(0);
  }

  float getMedianCoverage() {
    if (!this.sorted) {
      Collections.sort(this.coverages);
      this.sorted = true;
    }
    return this.coverages.get(this.coverages.size() / 2);
  }

  float getMeanCoverage() {
    float totalCoverage = 0;
    for (Float c : this.coverages) {
      totalCoverage += c;
    }

    return totalCoverage / this.coverages.size();
  }

  float getMaxCoverage() {
    if (!this.sorted) {
      Collections.sort(this.coverages);
      this.sorted = true;
    }
    return this.coverages.get(this.coverages.size() - 1);
  }

  float get95Percentile() {
    if (!this.sorted) {
      Collections.sort(this.coverages);
      this.sorted = true;
    }

    return this.coverages.get((int) (this.coverages.size() * .95));
  }
}
