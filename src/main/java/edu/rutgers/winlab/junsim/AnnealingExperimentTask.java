/*
 * Copyright (C) 2012 Bernhard Firner and Rutgers University
 * Based on the AnnealingExperimentTask class by Robert Moore
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * @author Bernhard Firner
 * 
 */
public class AnnealingExperimentTask {

  final TaskConfig config;
  final ExperimentStats stats[];
  String saveDirectory = null;
  private final ExecutorService workers;

  public AnnealingExperimentTask(final TaskConfig config, final ExperimentStats[] stats,
      final ExecutorService workers) {
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
    AnnealingExperimentTask parent;

    
    
    public SolutionCheckTask() {
      super();
    }

    @Override
    public Receiver call() {
      System.out.println("Call is called!\n");

      Point2D maxPoint = null;
      Collection<CaptureDisk> maxPointDisks = null;
      int maxDisks = 0;

      // For each solution point, map the set of capture disks that contain it

      for (Iterator<Point2D> iter = solutionPoints.iterator(); iter.hasNext();) {
        Point2D p = iter.next();
        Collection<CaptureDisk> pDisk = new HashSet<CaptureDisk>();
        for (CaptureDisk d : disks) {
          if (AnnealingExperimentTask.checkPointInDisk(p,d)) {
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
    HashMap<Point2D, HashSet<Point2D>> adjacencies =
      new HashMap<Point2D, HashSet<Point2D>>();
    HashMap<Point2D, HashSet<CaptureDisk>> pToD =
      new HashMap<Point2D, HashSet<CaptureDisk>>();

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

    Collection<Point2D> solutionPoints = AnnealingExperimentTask.generateSolutionPoints(
        disks, this.config.transmitters, adjacencies);
    // Adjacency list is now populated from possible points
    // This occurs in the genreateSolutionPoints function

    //Populate solution point to disk record
    for (Point2D p : solutionPoints) {
      pToD.put(p, new HashSet<CaptureDisk>());
      for (CaptureDisk d : disks) {
        double x = d.disk.getCenterX();
        double y = d.disk.getCenterY();
        float radius = d.disk.radius;
        //Point in disk? Add to map
        if (p.getX() >= radius-x &&
            p.getX() < radius+x &&
            p.getY() >= radius-y &&
            p.getY() < radius+y) {
          pToD.get(p).add(d);
        }
      }
    }

    System.out.printf("[%d] Generated %,d solution points.\n",this.config.trialNumber, solutionPoints.size());
    if (Main.config.generateImages) {
      display.setTransmitters(this.config.transmitters);
      display.setSolutionPoints(solutionPoints);
      display.setCaptureDisks(disks);
      saveImage(display, this.saveDirectory + File.separator + "0020");
      display.clear();
    }

    //TODO FIXME HERE Use adjacency list to drive simulated annealing
    // First pick K random starting points
    // Follow simulated annealing algorithm. From wikipedia:
    /*
       s <- s0; e <- E(s)                            // Initial state, energy.
       sbest <- s; ebest <- e                        // Initial "best" solution
       k <- 0                                        // Energy evaluation count.
       while k < kmax and e > emax                   // While time left & not good enough:
         T <- temperature(k/kmax)                    // Temperature calculation.
         snew <- neighbour(s)                        // Pick some neighbour.
         enew <- E(snew)                             // Compute its energy.
         if P(e, enew, T) > random() then            // Should we move to it?
           s <- snew; e <- enew                      // Yes, change state.
         if enew < ebest then                        // Is this a new best?
           sbest <- snew; ebest <- enew              // Save 'new neighbour' to 'best found'.
         k <- k + 1                                  // One more evaluation done
       return sbest                                  // Return the best solution found.
     */
    //
    // Select random set of receivers as the starting set for annealing
    // TODO Vector instead of linked list?
    Collection<Point2D> state = new LinkedList<Point2D>();
    //TODO FIXME Use random points, this doesn't work (solutionPoints is a hash)
    //state.add(solutionPoints.get(i*solutionPoints.size()/this.config.numReceivers));
    int cur = 0;
    int end = solutionPoints.size() / this.config.numReceivers;
    for (Point2D p: solutionPoints) {
      cur += 1;
      if (cur % end == 0) {
        state.add(p);
      }
    }
    // Initialize energy and best state
    float energy = AnnealingExperimentTask.getMeanContention(
        AnnealingExperimentTask.getCoverage(state, pToD, this.config.transmitters),
        this.config.transmitters, this.config.numTransmitters);
    Collection<Point2D> bestState = state;
    float bestEnergy = energy;
    float temperature = 1.0f;
    float delta = 0.01f; // 1000 iterations
    while (temperature <= 11.0) {
      for (Point2D p : state) {
        Point2D possiblePoint = AnnealingExperimentTask.getRandomNeighbor(p, adjacencies);
        Collection<Point2D> possibleState = new LinkedList<Point2D>();
        //TODO FIXME Copy from state into possible state but insert possible point for p
        //TODO FIXME Replace this code with something sensible when the internet is available
        boolean replaced = false;
        for (Point2D cur_p : state) {
          if (replaced || p != cur_p) {
            possibleState.add(cur_p);
          }
          else {
            replaced = true;
            possibleState.add(possiblePoint);
          }
        }
        float possibleEnergy = AnnealingExperimentTask.getMeanContention(
            AnnealingExperimentTask.getCoverage(possibleState, pToD, this.config.transmitters),
            this.config.transmitters, this.config.numTransmitters);
        // transitionProb may be > 1, but the result is the same
        //float transitionProb = (float)Math.pow(0.5f + energy / (energy+possibleEnergy), temperature);
        float transitionProb = (float)Math.pow(energy/possibleEnergy, 11.0 - temperature);
        if (possibleEnergy < energy ||
            Math.random() < transitionProb) {
          state = possibleState;
          energy = possibleEnergy;
          if (bestEnergy > energy) {
            System.out.println("Moving mean contention "+bestEnergy+" to "+possibleEnergy);
            bestState = state;
            bestEnergy = energy;
          }
        }
      }
      temperature += delta;
    }
    // Log statistics for best state
    // TODO FIXME Log other statistics besides just contention
    this.stats[this.config.numReceivers-1].addContention(AnnealingExperimentTask.getMeanContention(
          AnnealingExperimentTask.getCoverage(bestState, pToD, this.config.transmitters),
          this.config.transmitters, this.config.numTransmitters));

    // Keep going while there are either solution points or capture disks
    Collection<Receiver> receivers = new LinkedList<Receiver>();
    // Keep track of which collisions are captured so that packet loss
    // probabilities can be quickly calculated
    ConcurrentHashMap<Transmitter, HashSet<Transmitter>> capturedCollisions = new ConcurrentHashMap<Transmitter, HashSet<Transmitter>>();
    // Add an empty set for each transmitter
    for (Transmitter txer : this.config.transmitters) {
      capturedCollisions.put(txer, new HashSet<Transmitter>());
    }

    disks.clear();
    solutionPoints.clear();
    this.config.transmitters.clear();
    Runtime.getRuntime().gc();
    return Boolean.TRUE;
  }

  //Get energy of the coverage map
  private static float getMeanContention(HashMap<Transmitter, HashSet<Transmitter>> coverage,
      Collection<Transmitter> transmitters, int numTransmitters) {
    float mean_contention = 0.0f;
    for (Transmitter txer : transmitters) {
      // Calculate the number of transmitters in contention
      // Subtract 1 because this transmitter can never be in contention with itself
      int num_in_contention = numTransmitters - 1 - coverage.get(txer).size();
      mean_contention += (float)num_in_contention / numTransmitters;
    }
    //System.out.println("Mean contention is "+mean_contention);
    return mean_contention;
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

  private static Point2D getRandomNeighbor(Point2D p,
      HashMap<Point2D, HashSet<Point2D>> adjacencies) {
    double r = Math.random();
    int cur = 0;
    double end = r * adjacencies.get(p).size();
    for (Point2D neighbor : adjacencies.get(p)) {
      cur += 1;
      if (cur >= end) {
        return neighbor;
      }
    }
    System.out.println("getRandomNeighbor borked.");
    return null;
    //throw new Exception("getRandomNeighbor borked");
  }

  /**
   * Generate solution points and populate the adjacency list.
   */
  private static Collection<Point2D> generateSolutionPoints(
      Collection<CaptureDisk> disks, Collection<Transmitter> transmitters,
      HashMap<Point2D, HashSet<Point2D>> adjacencies) {
    // Add center points of all capture disks as solutions
    Collection<Point2D> solutionPoints = new HashSet<Point2D>();
    for (CaptureDisk d1 : disks) {
      // Build an adjacency list disk by disk
      Collection<Point2D> pointsInDisk = new LinkedList<Point2D>();
      // Check if the center point can be added
      if (d1.disk.getCenterX() >= 0
          && d1.disk.getCenterX() < Main.config.universeWidth
          && d1.disk.getCenterY() >= 0
          && d1.disk.getCenterY() < Main.config.universeHeight) {
        Point2D.Float center = new Point2D.Float((float) d1.disk.getCenterX(),
            (float) d1.disk.getCenterY());
        if (AnnealingExperimentTask.checkPointInRange(center, transmitters)) {
          solutionPoints.add(center);
          // Add this new solution point to the adjacency list
          adjacencies.put(center, new HashSet<Point2D>());

          // Mark this point adjacent to all points in the disk, all points
          // in the disk adjacent to this point, and add this point to the
          // disk's list
          for (Point2D adjPoint : pointsInDisk) {
            adjacencies.get(center).add(adjPoint);
            adjacencies.get(adjPoint).add(center);
          }
          pointsInDisk.add(center);
        }
      }
      // Add intersection of all capture disks as solutions
      for (CaptureDisk d2 : disks) {
        Collection<Point2D> intersections = Main.generateIntersections(d1, d2);
        if (intersections != null && !intersections.isEmpty()) {
          for (Point2D p : intersections) {
            if (AnnealingExperimentTask.checkPointInRange(p, transmitters)) {
              //Add this as a solution point
              solutionPoints.add(p);
              // Add this new solution point to the adjacency list
              adjacencies.put(p, new HashSet<Point2D>());

              // Mark this point adjacent to all points in the disk, all points
              // in the disk adjacent to this point, and add this point to the
              // disk's list
              for (Point2D adjPoint : pointsInDisk) {
                adjacencies.get(p).add(adjPoint);
                adjacencies.get(adjPoint).add(p);
              }
              pointsInDisk.add(p);
            }
          }
        }
      }
    }


    return solutionPoints;
  }

  /**
   * Get the collisions covered for each transmitter
   */
  private static HashMap<Transmitter, HashSet<Transmitter>> getCoverage(
      Collection<Point2D> state, HashMap<Point2D, HashSet<CaptureDisk>> pToD,
      Collection<Transmitter> transmitters) {
    HashMap<Transmitter, HashSet<Transmitter>> coverage = new HashMap<Transmitter, HashSet<Transmitter>>();
    // Initialize each set to empty
    for (Transmitter txer : transmitters) {
      coverage.put(txer, new HashSet<Transmitter>());
    }

    //Fill in coverage information
    for (Point2D p : state) {
      for (CaptureDisk disk : pToD.get(p)) {
        coverage.get(disk.t1).add(disk.t2);
      }
    }
    return coverage;
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
    System.out.printf("Rendering took %,dms.\n",duration);
  }

}
