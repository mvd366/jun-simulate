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

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.thoughtworks.xstream.XStream;

/**
 * Main class to start the receiver placement simulations.
 * 
 * @author Robert Moore
 */
public class Main {

  /**
   * Configuration file for the application.
   */
  static Config config = new Config();

  /**
   * Configuration for rendering images.
   */
  static RenderConfig gfxConfig = new RenderConfig();

  /**
   * Random number generator.
   */
  static Random rand;

  /**
   * Worker threads for executing parallel tasks.
   */
  static ExecutorService workers = null;

  /**
   * Maximum number of worker threads to use.
   */
  static int maxConcurrentTasks = 1;

  /**
   * Parses the commandline arguments and starts the simulation.
   * 
   * @param args
   *          configuration file
   * @throws IOException
   *           if an exception occurs while reading the configuration file.
   */
  public static void main(String[] args) throws IOException {
    XStream configReader = new XStream();
    if (args.length == 1) {
      System.out.println("Using configuration file " + args[0]);

      File configFile = new File(args[0]);
      Main.config = (Config) configReader.fromXML(configFile);
    } else {
      System.out.println("Using built-in default configuration.");
    }
    rand = new Random(Main.config.randomSeed);

    try {
      RenderConfig rConf = (RenderConfig) configReader.fromXML(new File(
          config.renderConfig));
      gfxConfig = rConf;

    } catch (Exception e) {
      System.err.println("Unable to read rendering configuration file \""
          + config.renderConfig + "\".");
      e.printStackTrace();
    }

    if (Main.config.numThreads < 1) {
      Main.config.numThreads = Runtime.getRuntime().availableProcessors();
      workers = Executors.newFixedThreadPool(Main.config.numThreads);
      maxConcurrentTasks = Main.config.numThreads;
      System.out.println("Using " + Main.config.numThreads
          + " threads based on process availability.");
    } else {
      workers = Executors.newFixedThreadPool(Main.config.numThreads);
      maxConcurrentTasks = Main.config.numThreads;
      System.out.println("Using " + Main.config.numThreads
          + " threads based on configuration file.");
    }

    // Shutdown handler (for signals from OS)
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Main.workers.shutdownNow();
      }
    });

    doSimulation();

  }

  /**
   * Perform an unattended set of simulations.
   * 
   * @throws IOException
   *           if an exception is thrown.
   */
  public static void doSimulation() throws IOException {
    File outputFile = new File(Main.config.outputFileName);
    if (!outputFile.exists()) {
      outputFile.createNewFile();
    }
    if (!outputFile.canWrite()) {
      System.err.println("Unable to write to " + Main.config.outputFileName
          + ". Please check file system permissions.");
      return;
    }

    // Output file (CSV) for stats
    PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFile));
    ExperimentStats[] stats = new ExperimentStats[Main.config.numReceivers];
    for (int i = 0; i < stats.length; ++i) {
      stats[i] = new ExperimentStats();
      stats[i].numberReceivers = i + 1;
      stats[i].numberTransmitters = Main.config.numTransmitters;
    }

    fileWriter
        .println("# Tx, # Rx, Min % Covered, Med. % Covered, Mean % Covered, 95% Coverage, Max % Covered, Min Contention, Med. Contention, Mean Contention, 95% Contention, Max Contention");

    // Iterate through some number of trials
    for (int trialNumber = 0; trialNumber < Main.config.numTrials; ++trialNumber) {

      int numTransmitters = Main.config.numTransmitters;
      // Randomly generate transmitter locations
      Collection<Transmitter> transmitters = Main
          .generateTransmitterLocations(numTransmitters);

      TaskConfig conf = new TaskConfig();
      conf.trialNumber = trialNumber;
      conf.numTransmitters = numTransmitters;
      conf.transmitters = transmitters;
      conf.numReceivers = Main.config.numReceivers;

      Experiment task;
      if ("binned".equalsIgnoreCase(config.experimentType)) {
        task = new BinnedBasicExperiment(conf, stats, workers);
      } else if ("grid".equalsIgnoreCase(config.experimentType)) {
        task = new BinnedGridExperiment(conf, stats, workers);
      } else {
        task = new BasicExperiment(conf, stats, workers);
      }
      task.perform();
    } // End number of trials

    workers.shutdown();
    System.out.println("Waiting up to 60 seconds for threadpool to terminate.");
    try {
      workers.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // # Tx, # Rx, Min % Covered, Med. %
    // Covered, Mean % Covered, Max % Covered, 95% Coverage
    for (ExperimentStats s : stats) {
      fileWriter
          .printf(
              "%d, %d, %.4f, %.4f, %.4f, %.4f, %.4f, %.5f, %.5f, %.5f, %.5f, %.5f\n",
              Integer.valueOf(s.numberTransmitters),
              Integer.valueOf(s.numberReceivers),
              Float.valueOf(s.getMinCoverage()),
              Float.valueOf(s.getMedianCoverage()),
              Float.valueOf(s.getMeanCoverage()),
              Float.valueOf(s.get95PercentileCoverage()),
              Float.valueOf(s.getMaxCoverage()),
              Float.valueOf(s.getMinContention()),
              Float.valueOf(s.getMedianContention()),
              Float.valueOf(s.getMeanContention()),
              Float.valueOf(s.get95PercentileContention()),
              Float.valueOf(s.getMaxContention()));
    }
    fileWriter.flush();
    fileWriter.close();
  }

  /**
   * Randomly generate the locations of {@code numTransmitters} within the
   * bounding square.
   * 
   * @param numTransmitters
   *          the number of transmitters to generate.
   * @return an array of {@code Transmitter} objects randomly positioned.
   */
  static Collection<Transmitter> generateTransmitterLocations(
      final int numTransmitters) {

    LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
    Transmitter txer = null;
    for (int i = 0; i < numTransmitters; ++i) {
      txer = new Transmitter();
      txer.x = (Main.config.universeWidth - Main.config.squareWidth) * .5f
          + Main.rand.nextFloat() * Main.config.squareWidth;
      txer.y = (Main.config.universeHeight - Main.config.squareHeight) * .5f
          + Main.rand.nextFloat() * Main.config.squareHeight;
      txers.add(txer);
    }
    return txers;
  }

  /**
   * Computes the capture disk of transmitter t1. Uses the constant parameter
   * Beta from the global configuration.
   * 
   * @param t1
   *          the captured transmitter.
   * @param t2
   *          the uncaptured (colliding) transmitter.
   * @return the capture disk of transmitter t1, else {@code null} if none
   *         exists.
   */
  static CaptureDisk generateCaptureDisk(final Transmitter t1,
      final Transmitter t2) {
    if (t1 == t2 || t1.equals(t2)) {
      return null;
    }
    CaptureDisk captureDisk = new CaptureDisk();
    captureDisk.disk = new Circle();
    captureDisk.t1 = t1;
    captureDisk.t2 = t2;
    double betaSquared = Math.pow(Main.config.beta, 2);
    double denominator = 1 - betaSquared;

    double centerX = (t1.getX() - (betaSquared * t2.getX())) / denominator;
    double centerY = (t1.getY() - (betaSquared * t2.getY())) / denominator;

    double euclideanDistance = Math.sqrt(Math.pow(t1.getX() - t2.getX(), 2)
        + Math.pow(t1.getY() - t2.getY(), 2));

    /**
     * TODO: Improve the cutting based on transmit distance. This is overly
     * simplistic.
     */
    if (euclideanDistance > (2 * Main.config.maxRangeMeters)) {
      return null;
    }

    double radius = (Main.config.beta * euclideanDistance) / denominator;

    captureDisk.disk.radius = (float) radius;
    captureDisk.disk.center.x = (float) centerX;
    captureDisk.disk.center.y = (float) centerY;

    return captureDisk;
  }

  /**
   * Generates the intersection points of two circles, IF they intersect.
   * 
   * @param cd1
   *          the first circle.
   * @param cd2
   *          the second circle.
   * @return a {@code Collection} containing the intersection points, or
   *         {@code null} if there are no intersections.
   */
  static Collection<Point2D> generateIntersections(final CaptureDisk cd1,
      final CaptureDisk cd2) {
    // If these are the same disks, don't check their intersection
    if (cd1.equals(cd2) || cd1 == cd2) {
      return null;
    }

    double d = Math.sqrt(Math.pow(
        cd1.disk.getCenterX() - cd2.disk.getCenterX(), 2)
        + Math.pow(cd1.disk.getCenterY() - cd2.disk.getCenterY(), 2));

    double r1 = cd1.disk.radius;
    double r2 = cd2.disk.radius;
    double d1 = (Math.pow(r1, 2) - Math.pow(r2, 2) + Math.pow(d, 2)) / (2 * d);

    // Circles are too far apart to overlap.
    if (d > (r1 + r2)) {
      return null;
    }

    double h = Math.sqrt(Math.pow(r1, 2) - Math.pow(d1, 2));

    double x3 = cd1.disk.getCenterX()
        + (d1 * (cd2.disk.getCenterX() - cd1.disk.getCenterX())) / d;

    double y3 = cd1.disk.getCenterY()
        + (d1 * (cd2.disk.getCenterY() - cd1.disk.getCenterY())) / d;

    double x4i = x3 + (h * (cd2.disk.getCenterY() - cd1.disk.getCenterY())) / d;
    double y4i = y3 - (h * (cd2.disk.getCenterX() - cd1.disk.getCenterX())) / d;
    double x4ii = x3 - (h * (cd2.disk.getCenterY() - cd1.disk.getCenterY()))
        / d;
    double y4ii = y3 + (h * (cd2.disk.getCenterX() - cd1.disk.getCenterX()))
        / d;

    if (Double.isNaN(x4i) || Double.isNaN(y4i) || Double.isNaN(x4ii)
        || Double.isNaN(y4ii)) {
      return null;
    }

    LinkedList<Point2D> points = new LinkedList<Point2D>();
    if (x4i >= 0 && x4i <= Main.config.universeWidth && y4i >= 0
        && y4i <= Main.config.universeHeight) {
      points.add(new Point2D.Float((float) x4i, (float) y4i));
    }
    if (x4ii >= 0 && x4ii <= Main.config.universeWidth && y4ii >= 0
        && y4ii <= Main.config.universeHeight) {
      points.add(new Point2D.Float((float) x4ii, (float) y4ii));
    }
    return points;
  }
}
