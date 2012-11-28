/*
 * Owl Platform Copyright (C) 2012 Robert Moore and the Owl Platform
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

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert Moore
 */
public class Binner {

  private static final Logger log = LoggerFactory.getLogger(Binner.class);

  private final Set<Point2D>[] bins;
  
  private final int[] binMins;

  public Binner(int numBins, int min, int max) {
    super();
    this.binMins = new int[numBins];
    this.binMins[0] = min;
    int step = (int)Math.ceil((max*1f - min)/numBins);
    if(step < 1){
      step = 1;
    }
    
    int binStart = min+step;
    for(int i = 1; i < this.binMins.length; ++i, binStart+=step){
      this.binMins[i] = binStart;
    }
    
    this.bins = new Set[numBins];
    for (int i = 0; i < this.bins.length; ++i) {
      this.bins[i] = Collections
          .newSetFromMap(new ConcurrentHashMap<Point2D, Boolean>());
    }
  }
  
  public void rebin(final int min, final int max){
    int step = (int)Math.ceil((max*1f - min)/this.bins.length);
    if(step < 1){
      step = 1;
    }
    int binStart = min+step;
    for(int i = 1; i < this.binMins.length; ++i, binStart+=step){
      this.binMins[i] = binStart;
    }
  }

  public int put(Point2D point, int score) {
    int bindex = getBindex(score);
    this.bins[bindex].add(point);
    return bindex;
  }

  public void set(Collection<Point2D> points, int score) {
    int bindex = getBindex(score);
    this.bins[bindex].addAll(points);
  }

  public Set<Point2D> getMaxBin() {
    for (int i = this.bins.length - 1; i >= 0; --i) {
      if (!this.bins[i].isEmpty()) {
        return this.bins[i];
      }
    }
    return null;
  }
  
  public int getMaxBindex(){
    int bindex = this.bins.length-1;
    for(; bindex > 0; --bindex){
      if(!this.bins[bindex].isEmpty()){
        break;
      }
    }
    return bindex;
  }

  /**
   * Gets the appropriate bin for some coverage count.
   * 
   * @param origCount
   *          the coverage count.
   * @return the bin index is goes into.
   */
  public int getBindex(int origCount) {
    int bindex =0;
    for(; bindex < this.binMins.length-1; ++bindex){
      if(this.binMins[bindex+1] > origCount){
        break;
      }
    }
    return bindex;
  }

  public void printBins() {

    StringBuilder sb = new StringBuilder();

    int maxSize = 0;
    for (int i = 0; i < this.bins.length; ++i) {
      if (this.bins[i].size() > maxSize) {
        maxSize = this.bins[i].size();
      }
    }

    int tickSize = maxSize / 60;
    if(tickSize == 0){
      tickSize = 1;
    }
    boolean skip = true;

    for (int i = this.bins.length - 1; i >= 0; --i) {
      int binSize = this.bins[i].size();
      if (binSize == 0 && skip) {
        continue;
      }
      skip = false;
      int numTicks = binSize / tickSize;
      boolean hasHalf = (binSize % tickSize) >= (tickSize / 2);

      sb.append(String.format("%3d) |", this.binMins[i]));

      for (int j = 0; j < numTicks; ++j) {
        sb.append("#");
      }
      if (hasHalf) {
        sb.append("=");
      }
      sb.append(String.format(" %,d", binSize));
      sb.append("\n");
    }

    log.info("Bins:\n{}", sb.toString());
  }
  
  public void clear(){
    for(Set<Point2D> bin : this.bins){
      bin.clear();
    }
  }
  
  /**
   * Returns bins in the order of highest-ranked to lowest-ranked.
   * 
   * @return bins in the order of highest-ranked to lowest-ranked.
   */
  public List<Collection<Point2D>> getBins(){
    LinkedList<Collection<Point2D>> returnedList = new LinkedList<Collection<Point2D>>();
    for(Set<Point2D> set : this.bins){
      returnedList.add(set);
    }
    return returnedList;
  }
  
  public List<Integer> getBinMins(){
    LinkedList<Integer> list = new LinkedList<Integer>();
    for(int min : this.binMins){
      list.add(Integer.valueOf(min));
    }
    return list;
  }
}
