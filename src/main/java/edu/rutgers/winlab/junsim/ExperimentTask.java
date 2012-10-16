/*
 * Owl Platform
 * Copyright (C) 2012 Robert Moore and the Owl Platform
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

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * @author Robert Moore
 *
 */
public class ExperimentTask implements Callable<Boolean> {

  final TaskConfig config;
  final ExperimentStats stats;
  
  public ExperimentTask(final TaskConfig config, final ExperimentStats stats){
    super();
    this.config = config;
    this.stats = stats;
  }
  
  @Override
  public Boolean call() {
    Collection<Point2D> clonePoints = new LinkedList<Point2D>();
    clonePoints.addAll(this.config.solutionPoints);
    Collection<CaptureDisk> cloneDisks = new LinkedList<CaptureDisk>();
    cloneDisks.addAll(this.config.disks);
    
    int totalCaptureDisks = cloneDisks.size();
    int totalSolutionPoints = clonePoints.size();

    int m = 0;
    // Keep going while there are either solution points or capture disks
    while (m < this.config.numReceivers && !clonePoints.isEmpty()
        && !cloneDisks.isEmpty()) {
      HashMap<Point2D, Collection<CaptureDisk>> bipartiteGraph = new HashMap<Point2D, Collection<CaptureDisk>>();

      Point2D maxPoint = null;
      int maxDisks = Integer.MIN_VALUE;

      // For each solution point, map the set of capture disks that contain
      // it
      for (Point2D p : clonePoints) {
        for (CaptureDisk d : cloneDisks) {
          if (d.disk.contains(p)) {
            Collection<CaptureDisk> containingPoints = bipartiteGraph
                .get(p);
            if (containingPoints == null) {
              containingPoints = new HashSet<CaptureDisk>();
              bipartiteGraph.put(p, containingPoints);
            }
            containingPoints.add(d);
            if (containingPoints.size() > maxDisks) {
              maxDisks = containingPoints.size();
              maxPoint = p;
            }
          }
        }
      }
      

      // Remove the highest point and its solution disks
      if (maxDisks > 0) {
        Collection<CaptureDisk> removedDisks = bipartiteGraph.get(maxPoint);
        Receiver r = new Receiver();
        r.setLocation(maxPoint);
        r.coveringDisks = removedDisks;
        clonePoints.remove(maxPoint);
        cloneDisks.removeAll(removedDisks);
      }
      // No solutions found?
      else {
        break;
      }
      ++m;
      
      for(Collection<CaptureDisk> vals :bipartiteGraph.values()){
        vals.clear();
      }
      bipartiteGraph.clear();
    }

    float capturedDisks = totalCaptureDisks - cloneDisks.size();
    float captureRatio = (capturedDisks / totalCaptureDisks);

    this.stats.addCoverage(captureRatio);
    cloneDisks.clear();
    clonePoints.clear();
    Runtime.getRuntime().gc();
    return Boolean.TRUE;
  }

}
