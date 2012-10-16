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

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * @author Robert Moore
 * 
 */
public class ExperimentTask implements Callable<Boolean> {

  final TaskConfig config;
  final ExperimentStats stats[];

  public ExperimentTask(final TaskConfig config, final ExperimentStats[] stats) {
    super();
    this.config = config;
    this.stats = stats;
  }

  @Override
  public Boolean call() {

    Collection<CaptureDisk> disks = new HashSet<CaptureDisk>();
    // Compute all possible capture disks
    for (Transmitter t1 : this.config.transmitters) {
      for (Transmitter t2 : this.config.transmitters) {
        CaptureDisk someDisk = Main.generateCaptureDisk(t1, t2);
        if (someDisk != null) {
          disks.add(someDisk);
        }
      }
    }
    System.out.println("[" + this.config.trialNumber
        + "] Generated " + disks.size() + " disks.");
    
    
    // Add center points of all capture disks as solutions
    Collection<Point2D> solutionPoints = new HashSet<Point2D>();
    for (CaptureDisk disk : disks) {
      solutionPoints.add(new Point2D.Float((float) disk.disk.getCenterX(),
          (float) disk.disk.getCenterY()));
    }

    System.out.println("[" + this.config.trialNumber
        + "] Generating intersection solutions.");
    // Add intersection of all capture disks as solutions
    for (CaptureDisk d1 : disks) {
      for (CaptureDisk d2 : disks) {
        Collection<Point2D> intersections = Main.generateIntersections(d1, d2);
        if (intersections != null && !intersections.isEmpty()) {
          solutionPoints.addAll(intersections);
        }
      }
    }
    
    System.out.println("[" + this.config.trialNumber
        + "] Generated " + solutionPoints.size() + " solution points.");

    DisplayPanel display = new DisplayPanel();

    int totalCaptureDisks = disks.size();
    int totalSolutionPoints = solutionPoints.size();
    int m = 0;
    // Keep going while there are either solution points or capture disks
    Collection<Receiver> receivers = new LinkedList<Receiver>();
    while (m < this.config.numReceivers && !solutionPoints.isEmpty()
        && !disks.isEmpty()) {
      System.out.println("[" + this.config.trialNumber
          + "] Calculating position for receiver " + (m + 1) + ".");
      HashMap<Point2D, Collection<CaptureDisk>> bipartiteGraph = new HashMap<Point2D, Collection<CaptureDisk>>();

      Point2D maxPoint = null;
      int maxDisks = 0;

      // For each solution point, map the set of capture disks that contain
      // it
      int diskNum = 0;
      for (CaptureDisk d : disks) {
//        System.out.println("[" + this.config.trialNumber
//            + "] Disk " + diskNum + "/" + disks.size() + ".");
        ++diskNum;
        for (Point2D p : solutionPoints) {
          if (d.disk.contains(p)) {
            Collection<CaptureDisk> containingPoints = bipartiteGraph.get(p);
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
      if (maxPoint != null) {
        Collection<CaptureDisk> removedDisks = bipartiteGraph.remove(maxPoint);
        Receiver r = new Receiver();
        r.setLocation(maxPoint);
        r.coveringDisks = removedDisks;
        receivers.add(r);
        solutionPoints.remove(maxPoint);
        disks.removeAll(removedDisks);
      }
      // No solutions found?
      else {
        break;
      }

      float capturedDisks = totalCaptureDisks - disks.size();
      float captureRatio = (capturedDisks / totalCaptureDisks);
      float receiverRatio = (1f * m + 1) / this.config.numReceivers;
      // Debugging stuff
      if (Main.config.generateImages && captureRatio < .999f
          && receiverRatio >= 0.5) {
        BufferedImage img = new BufferedImage(1920, 1080,
            BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();

        display.setTransmitters(this.config.transmitters);
        display.setSolutionPoints(solutionPoints);
        display.setCaptureDisks(disks);
        display.setReceiverPoints(receivers);

        display.render(g, img.getWidth(), img.getHeight());

        File imageFile = new File("t" + this.config.numTransmitters + "_r"
            + (m + 1) + "_x" + this.config.trialNumber + ".png");
        try {
          ImageIO.write(img, "png", imageFile);
          System.out.println("Wrote " + imageFile.getName());
        } catch (Exception e) {
          e.printStackTrace();
        }
        g.dispose();

      }

      this.stats[m].addCoverage(captureRatio);
      ++m;

      for (Collection<CaptureDisk> vals : bipartiteGraph.values()) {
        vals.clear();
      }
      bipartiteGraph.clear();

    } // End for each receiver

    // }
    disks.clear();
    solutionPoints.clear();
    this.config.transmitters.clear();
    Runtime.getRuntime().gc();
    return Boolean.TRUE;
  }

}
