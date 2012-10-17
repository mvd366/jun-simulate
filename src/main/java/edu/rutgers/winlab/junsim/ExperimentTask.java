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

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * @author Robert Moore
 * 
 */
public class ExperimentTask implements Callable<Boolean> {

  final TaskConfig config;
  final ExperimentStats stats[];
  String saveDirectory = null;

  public ExperimentTask(final TaskConfig config, final ExperimentStats[] stats) {
    super();
    this.config = config;
    this.stats = stats;
    this.saveDirectory = String.format("s%d_t%d_x%d"
        + (Main.config.stripSolutionPoints ? "_S" : ""),
        Main.config.randomSeed, this.config.numTransmitters,
        this.config.trialNumber);
  }

  @Override
  public Boolean call() {
    DisplayPanel display = new DisplayPanel();

    if (Main.config.generateImages) {

      display.setTransmitters(this.config.transmitters);
      saveImage(display, this.saveDirectory + File.separator + "0000");
      display.clear();
    }

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
    System.out.println("[" + this.config.trialNumber + "] Generated "
        + disks.size() + " disks.");
    if (Main.config.generateImages) {
      display.setTransmitters(this.config.transmitters);
      display.setCaptureDisks(disks);
      saveImage(display, this.saveDirectory + File.separator + "0010");
      display.clear();
    }

    Collection<Point2D> solutionPoints = ExperimentTask
        .generateSolutionPoints(disks);
    System.out.println("[" + this.config.trialNumber + "] Generated "
        + solutionPoints.size() + " solution points.");
    if (Main.config.generateImages) {
      display.setTransmitters(this.config.transmitters);
      display.setSolutionPoints(solutionPoints);
      display.setCaptureDisks(disks);
      saveImage(display, this.saveDirectory + File.separator + "0020");
      display.clear();
    }

    int totalCaptureDisks = disks.size();
    int totalSolutionPoints = solutionPoints.size();
    int m = 0;
    // Keep going while there are either solution points or capture disks
    Collection<Receiver> receivers = new LinkedList<Receiver>();
    while (m < this.config.numReceivers && !solutionPoints.isEmpty()
        && !disks.isEmpty()) {
      System.out.println("[" + this.config.trialNumber
          + "] Calculating position for receiver " + (m + 1) + ".");
      // HashMap<Point2D, Collection<CaptureDisk>> bipartiteGraph = new
      // HashMap<Point2D, Collection<CaptureDisk>>();

      Point2D maxPoint = null;
      Collection<CaptureDisk> maxPointDisks = null;
      int maxDisks = 0;

      // For each solution point, map the set of capture disks that contain
      // it

      // System.out.println("[" + this.config.trialNumber
      // + "] Disk " + diskNum + "/" + disks.size() + ".");

      for (Point2D p : solutionPoints) {
        Collection<CaptureDisk> pDisk = new HashSet<CaptureDisk>();
        for (CaptureDisk d : disks) {
          if (d.disk.contains(p)) {
            pDisk.add(d);

          }
          if (pDisk.size() > maxDisks) {
            maxDisks = pDisk.size();
            maxPoint = p;
            maxPointDisks = pDisk;
          }
        }
      }

      // Remove the highest point and its solution disks
      if (maxPoint != null) {
        Receiver r = new Receiver();
        r.setLocation(maxPoint);
        r.coveringDisks = maxPointDisks;
        receivers.add(r);
        solutionPoints.remove(maxPoint);
        disks.removeAll(maxPointDisks);
      }
      // No solutions found?
      else {
        break;
      }

      float capturedDisks = totalCaptureDisks - disks.size();
      float captureRatio = (capturedDisks / totalCaptureDisks);
      float receiverRatio = (1f * m + 1) / this.config.numReceivers;
      // Debugging stuff
      if (Main.config.generateImages) {
        display.setTransmitters(this.config.transmitters);
        display.setSolutionPoints(solutionPoints);
        display.setCaptureDisks(disks);
        display.setReceiverPoints(receivers);

        String saveName = String.format(this.saveDirectory + File.separator
            + "1%03d", (m + 1));
        saveImage(display, saveName);
        display.clear();

      }

      this.stats[m].addCoverage(captureRatio);
      ++m;
      // Recompute solution points based on remaining disks
      if (Main.config.stripSolutionPoints) {
        solutionPoints.clear();
        solutionPoints = ExperimentTask.generateSolutionPoints(disks);
        System.out.println("[" + this.config.trialNumber + "] Regenerated "
            + solutionPoints.size() + " solution points.");
      }

    } // End for each receiver

    // }
    disks.clear();
    solutionPoints.clear();
    this.config.transmitters.clear();
    Runtime.getRuntime().gc();
    return Boolean.TRUE;
  }

  private static Collection<Point2D> generateSolutionPoints(
      Collection<CaptureDisk> disks) {
    // Add center points of all capture disks as solutions
    Collection<Point2D> solutionPoints = new HashSet<Point2D>();
    for (CaptureDisk disk : disks) {
      if (disk.disk.getCenterX() < 0
          || disk.disk.getCenterX() >= Main.config.universeWidth
          || disk.disk.getCenterY() < 0
          || disk.disk.getCenterY() > Main.config.universeHeight) {
        continue;
      }
      solutionPoints.add(new Point2D.Float((float) disk.disk.getCenterX(),
          (float) disk.disk.getCenterY()));
    }

    // Add intersection of all capture disks as solutions
    for (CaptureDisk d1 : disks) {
      for (CaptureDisk d2 : disks) {
        Collection<Point2D> intersections = Main.generateIntersections(d1, d2);
        if (intersections != null && !intersections.isEmpty()) {
          solutionPoints.addAll(intersections);
        }
      }
    }

    return solutionPoints;
  }

  private void saveImage(DisplayPanel display, String fileName) {
    BufferedImage img = new BufferedImage(1920, 1080,
        BufferedImage.TYPE_INT_RGB);
    Graphics g = img.createGraphics();

    display.render(g, img.getWidth(), img.getHeight());

    File imageFile = new File(fileName + ".png");
    imageFile.mkdirs();
    if (!imageFile.exists()) {
      try {
        imageFile.createNewFile();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    try {
      ImageIO.write(img, "png", imageFile);
      System.out.println("Saved " + imageFile.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    g.dispose();
  }

}
