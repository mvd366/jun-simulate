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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert Moore
 * 
 */
public class HittingExperimentTask {
  
  private static final Logger log = LoggerFactory.getLogger(HittingExperimentTask.class);

  final TaskConfig config;
  final ExperimentStats stats[];
  String saveDirectory = null;
  private final ExecutorService workers;

  public HittingExperimentTask(final TaskConfig config,
      final ExperimentStats[] stats, final ExecutorService workers) {
    super();
    this.workers = workers;
    this.config = config;
    this.stats = stats;
    this.saveDirectory = String.format("s%d_t%d_x%d"
        + (Main.config.stripSolutionPoints ? "_S" : ""),
        Main.config.randomSeed, this.config.numTransmitters,
        this.config.trialNumber);
  }

  private static final class SolutionCheckTask implements Callable<Receiver> {

    Collection<Point2D> solutionPoints;
    Collection<CaptureDisk> disks;
    HittingExperimentTask parent;

    public SolutionCheckTask() {
      super();
    }

    @Override
    public Receiver call() {

      Point2D maxPoint = null;
      Collection<CaptureDisk> maxPointDisks = null;
      int maxDisks = 0;

      // For each solution point, map the set of capture disks that contain
      // it

      // System.out.println("[" + this.config.trialNumber
      // + "] Disk " + diskNum + "/" + disks.size() + ".");

      for (Iterator<Point2D> iter = solutionPoints.iterator(); iter.hasNext();) {
        Point2D p = iter.next();
        Collection<CaptureDisk> pDisk = new HashSet<CaptureDisk>();
        for (CaptureDisk d : disks) {
          if (HittingExperimentTask.checkPointInDisk(p, d)) {
            pDisk.add(d);
          }
        }
        if (pDisk.size() > maxDisks) {
          maxDisks = pDisk.size();
          maxPoint = p;
          maxPointDisks = pDisk;
        }
        // No intersections, so remove
        else if (pDisk.size() == 0) {
          iter.remove();
        }
      }
      Receiver maxReceiver = new Receiver();
      // Remove the highest point and its solution disks
      if (maxPoint != null) {

        maxReceiver.setLocation(maxPoint);
        maxReceiver.coveringDisks = maxPointDisks;

      }
      // No solutions found?
      else {
        return null;
      }

      return maxReceiver;
    }
  }

  public Boolean perform() {
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

    Integer rank = Integer.valueOf(1);
    Map<Integer, List<CaptureDiskGroup>> groupsByRank = new HashMap<Integer, List<CaptureDiskGroup>>();
    List<CaptureDiskGroup> firstRank = new ArrayList<CaptureDiskGroup>();
    for (CaptureDisk disk : disks) {
      CaptureDiskGroup grp = new CaptureDiskGroup();
      grp.disks.add(disk);
      firstRank.add(grp);
    }
    
    groupsByRank.put(rank, firstRank);
    do {

      HashSet<CaptureDiskGroup> rankList = new HashSet<CaptureDiskGroup>();
      
      log.debug("Rank {}: {} groups",rank,groupsByRank.get(rank).size());

      for (int i = 0; i < groupsByRank.get(rank).size(); ++i) {
        CaptureDiskGroup iGroup = groupsByRank.get(rank).get(i);
        log.debug(" {} iGrp: {}", i,iGroup);
        for (int j = i+1; j < groupsByRank.get(rank).size(); ++j) {
          CaptureDiskGroup jGroup = groupsByRank.get(rank).get(j);
          log.debug("   {} jGrp: {}",j, jGroup);
          if (iGroup.intersects(jGroup)) {
            CaptureDiskGroup newGroup = new CaptureDiskGroup();
            newGroup.disks.addAll(iGroup.disks);
            newGroup.disks.addAll(jGroup.disks);
            rankList.add(newGroup);
          }
        }
      }
      display.setTransmitters(this.config.transmitters);
      display.setCaptureDiskGroups(rankList);
      this.saveImage(display, String.format("%s%s%04d", saveDirectory,File.separator, rank));
      display.clear();

      rank = Integer.valueOf(rank.intValue() * 2);
      if (rankList.size() > 0) {
        ArrayList<CaptureDiskGroup> list = new ArrayList<CaptureDiskGroup>();
        list.addAll(rankList);
        groupsByRank.put(rank, list);
      }
      

    } while (groupsByRank.get(rank) != null);

    

    Runtime.getRuntime().gc();
    return Boolean.TRUE;
  }

  static boolean checkPointInDisk(Point2D p, CaptureDisk d) {
    double dist1 = Math.sqrt(Math.pow(p.getX() - d.t1.getX(), 2)
        + Math.pow(p.getY() - d.t1.getY(), 2));
    double dist2 = Math.sqrt(Math.pow(p.getX() - d.t2.getX(), 2)
        + Math.pow(p.getY() - d.t2.getY(), 2));
    // This point is too far away from the transmitters for this disk
    if (dist1 > Main.config.maxRangeMeters
        && dist2 > Main.config.maxRangeMeters) {
      return false;
    }
    return d.disk.contains(p);

  }

  private static Collection<Point2D> generateSolutionPoints(
      Collection<CaptureDisk> disks, Collection<Transmitter> transmitters) {
    // Add center points of all capture disks as solutions
    Collection<Point2D> solutionPoints = new HashSet<Point2D>();
    for (CaptureDisk disk : disks) {
      if (disk.disk.getCenterX() < 0
          || disk.disk.getCenterX() >= Main.config.universeWidth
          || disk.disk.getCenterY() < 0
          || disk.disk.getCenterY() > Main.config.universeHeight) {
        continue;
      }
      Point2D.Float center = new Point2D.Float((float) disk.disk.getCenterX(),
          (float) disk.disk.getCenterY());
      if (HittingExperimentTask.checkPointInRange(center, transmitters)) {
        solutionPoints.add(center);
      }
    }

    // Add intersection of all capture disks as solutions
    for (CaptureDisk d1 : disks) {
      for (CaptureDisk d2 : disks) {
        Collection<Point2D> intersections = Main.generateIntersections(d1, d2);
        if (intersections != null && !intersections.isEmpty()) {
          for (Point2D p : intersections) {
            if (HittingExperimentTask.checkPointInRange(p, transmitters)) {
              solutionPoints.add(p);
            }
          }
        }
      }
    }

    return solutionPoints;
  }

  /**
   * Returns true if a point is within the transmit radius of at least one
   * transmitter, else false.
   * 
   * @param p
   *          the point to test.
   * @return {@code true} if the point is within the transmit radius of at
   *         leaset one transmitter.
   */
  private static boolean checkPointInRange(Point2D p,
      Collection<Transmitter> transmitters) {
    for (Transmitter t : transmitters) {
      double d = Math.sqrt(Math.pow(p.getX() - t.getX(), 2)
          + Math.pow(p.getY() - t.getY(), 2));
      if (d < Main.config.maxRangeMeters) {
        return true;
      }
    }
    return false;
  }

  private void saveImage(DisplayPanel display, String fileName) {
    long start = System.currentTimeMillis();
    File imageFile = new File(fileName + ".png");
    System.out.printf("Rendering \"%s\".\n", imageFile);
    BufferedImage img = new BufferedImage(Main.config.renderWidth,
        Main.config.renderHeight, BufferedImage.TYPE_INT_RGB);
    Graphics g = img.createGraphics();

    display.render(g, img.getWidth(), img.getHeight());

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
    long duration = System.currentTimeMillis() - start;
    System.out.printf("Rendering took %,dms.\n", duration);
  }

}
