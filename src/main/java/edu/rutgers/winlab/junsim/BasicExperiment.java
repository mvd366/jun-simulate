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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

/**
 * A basic experimental simulation task that generates receiver positions based
 * on intersections of capture disks and tests each one to find the maximal
 * overlap.
 * 
 * @author Robert Moore
 * 
 */
public class BasicExperiment {

  /**
   * Configuration for this task.
   */
  final TaskConfig config;
  /**
   * Statistics to update.
   */
  final ExperimentStats stats[];
  /**
   * Name of the directory in which to save images.
   */
  String saveDirectory = null;
  /**
   * Pool of worker threads to utilize.
   */
  private final ExecutorService workers;

  /**
   * Creates a new experiment task with the specific configuration, global stats
   * to update, and worker pool.
   * 
   * @param config
   *          configuration to use.
   * @param stats
   *          statistics to update at the end
   * @param workers
   *          worker threadpool to utilize.
   */
  public BasicExperiment(final TaskConfig config,
      final ExperimentStats[] stats, final ExecutorService workers) {
    super();
    this.workers = workers;
    this.config = config;
    this.stats = stats;
    this.saveDirectory = String.format("s%d_t%d_x%d"
        + (Main.config.stripSolutionPoints ? "_S" : ""),
        Long.valueOf(Main.config.randomSeed),
        Integer.valueOf(this.config.numTransmitters),
        Integer.valueOf(this.config.trialNumber));
  }

  /**
   * Private class used to parallelize the checking of possible solution points.
   * 
   * @author Robert Moore
   * 
   */
  private static final class SolutionCheckTask implements Callable<Receiver> {

    /**
     * Set of points this task should check.
     */
    Collection<Point2D> solutionPoints;
    /**
     * Set of capture disks to check for intersections
     */
    Collection<CaptureDisk> disks;

    /**
     * Creates a new solution check task.
     */
    public SolutionCheckTask() {
      super();
    }

    @Override
    public Receiver call() {

      Point2D maxPoint = null;
      Collection<CaptureDisk> maxPointDisks = null;
      int maxDisks = 0;
      int totalDisks = this.disks.size();

      /*
       * Determine the number of disks that intersect this point. If the number
       * is the new max, then save it. If there are no intersections, remove it.
       */
      points: for (Iterator<Point2D> iter = solutionPoints.iterator(); iter.hasNext();) {
        Point2D p = iter.next();
        Collection<CaptureDisk> pDisk = new HashSet<CaptureDisk>();
        int numIntersect = 0;
        int diskIndex = 0;
        
        
        for (CaptureDisk d : disks) {
          if (BasicExperiment.checkPointInDisk(p, d)) {
            pDisk.add(d);
            ++numIntersect;
          }
          ++diskIndex;
          if((numIntersect + (totalDisks -diskIndex)) < maxDisks){
            continue points;
          }
        }
        if (pDisk.size() > maxDisks) {
          maxDisks = pDisk.size();
          maxPoint = p;
          maxPointDisks = pDisk;
        }
        // No intersections, so remove
//        else if (pDisk.size() == 0) {
//          iter.remove();
//        }
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

    Collection<Point2D> solutionPoints = BasicExperiment
        .generateSolutionPoints(disks, this.config.transmitters);
    System.out.printf("[%d] Generated %,d solution points.\n",
        this.config.trialNumber, solutionPoints.size());
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
    // Keep track of which collisions are captured so that packet loss
    // probabilities can be quickly calculated
    ConcurrentHashMap<Transmitter, HashSet<Transmitter>> capturedCollisions = new ConcurrentHashMap<Transmitter, HashSet<Transmitter>>();
    // Add an empty set for each transmitter
    for (Transmitter txer : this.config.transmitters) {
      capturedCollisions.put(txer, new HashSet<Transmitter>());
    }

    while (m < this.config.numReceivers && !solutionPoints.isEmpty()
        && !disks.isEmpty()) {
      System.out.println("[" + this.config.trialNumber
          + "] Calculating position for receiver " + (m + 1) + ".");
      // HashMap<Point2D, Collection<CaptureDisk>> bipartiteGraph = new
      // HashMap<Point2D, Collection<CaptureDisk>>();

      int numTasks = Main.config.numThreads;
      int numPoints = solutionPoints.size();
      int pointsPerTask = (numPoints / numTasks) + 1;
      long numComparisons = disks.size() * (long) numPoints;

      Collection<SolutionCheckTask> tasks = new LinkedList<BasicExperiment.SolutionCheckTask>();
      Iterator<Point2D> pointIter = solutionPoints.iterator();
      SolutionCheckTask task = new SolutionCheckTask();
      task.solutionPoints = new LinkedList<Point2D>();
      task.disks = disks;
      // task.parent = this;
      tasks.add(task);
      for (int i = 0; pointIter.hasNext(); ++i) {
        task.solutionPoints.add(pointIter.next());
        if (i == pointsPerTask) {
          i = 0;
          task = new SolutionCheckTask();
          task.solutionPoints = new LinkedList<Point2D>();
          task.disks = disks;
          // task.parent = this;
          tasks.add(task);
        }
      }
      int sumTasks = 0;
      for (SolutionCheckTask t : tasks) {
        System.out.printf("Task (%,d)\n", t.solutionPoints.size());
        sumTasks += t.solutionPoints.size();
      }

      System.out.printf("Divided %,d/%,d points.\n", sumTasks, numPoints);
      long start = System.currentTimeMillis();
      Receiver maxReceiver = null;
      try {
        List<Future<Receiver>> solutions = workers.invokeAll(tasks);

        for (Future<Receiver> future : solutions) {
          if (future.isCancelled() || !future.isDone()) {
            System.err
                .println("One of the tasks was cancelled! Double-check the code!");
            return Boolean.FALSE;
          }
          try {
            Receiver r = future.get();
            if (r == null) {
              continue;
            }
            if (maxReceiver == null
                || r.coveringDisks.size() > maxReceiver.coveringDisks.size()) {
              maxReceiver = r;
            }
          } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      long duration = System.currentTimeMillis() - start;
      System.out.printf("Computed %,d comparisons in %,dms.\n", numComparisons,
          duration);

      if (maxReceiver == null) {
        break;
      }

      solutionPoints.clear();
      for (SolutionCheckTask t : tasks) {
        solutionPoints.addAll(t.solutionPoints);
      }

      // Add the newest receiver and remove newly covered points and disks
      receivers.add(maxReceiver);
      solutionPoints.remove(maxReceiver);
      // Add captures to each transmitter's capture set for collision
      // calculations
      for (CaptureDisk disk : maxReceiver.coveringDisks) {
        capturedCollisions.get(disk.t1).add(disk.t2);
      }
      disks.removeAll(maxReceiver.coveringDisks);

      // Calculate collision rates for each transmitter
      // Store the min, max, and mean
      float mean_contention = 0.0f;
      float min_contention = this.config.numTransmitters;
      float max_contention = 0.0f;
      for (Transmitter txer : this.config.transmitters) {
        // Calculate the number of transmitters in contention
        // Subtract 1 because this transmitter can never be in contention with
        // itself
        int num_in_contention = this.config.numTransmitters - 1
            - capturedCollisions.get(txer).size();
        min_contention = Math.min(num_in_contention, min_contention);
        max_contention = Math.max(num_in_contention, max_contention);
        mean_contention += (float) num_in_contention
            / this.config.numTransmitters;
      }
      this.stats[m].addContention(mean_contention);
      this.stats[m].addMinContention(min_contention);
      this.stats[m].addMaxContention(max_contention);

      float capturedDisks = totalCaptureDisks - disks.size();
      float captureRatio = (capturedDisks / totalCaptureDisks);
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
        solutionPoints = BasicExperiment.generateSolutionPoints(disks,
            this.config.transmitters);
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
      if (BasicExperiment.checkPointInRange(center, transmitters)) {
        solutionPoints.add(center);
      }
    }

    // Add intersection of all capture disks as solutions
    for (CaptureDisk d1 : disks) {
      for (CaptureDisk d2 : disks) {
        Collection<Point2D> intersections = Main.generateIntersections(d1, d2);
        if (intersections != null && !intersections.isEmpty()) {
          for (Point2D p : intersections) {
            if (BasicExperiment.checkPointInRange(p, transmitters)) {
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
