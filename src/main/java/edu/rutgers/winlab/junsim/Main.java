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

import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.thoughtworks.xstream.XStream;

/**
 * @author Robert Moore
 * 
 */
public class Main {

  static Config config;

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Please provide a configuration file.");
      return;
    }

    XStream configReader = new XStream();
    File configFile = new File(args[0]);
    Main.config = (Config) configReader.fromXML(configFile);

    Transmitter[] transmitters = generateTransmitterLocations(Main.config.numTransmitters);
    Collection<CaptureDisk>disks = new ConcurrentLinkedQueue<CaptureDisk>();
    
    
  }

  /**
   * Randomly generate the locations of {@code numTransmitters} within the
   * bounding square.
   * 
   * @param numTransmitters
   *          the number of transmitters to generate.
   * @return an array of {@code Transmitter} objects randomly positioned.
   */
  static Transmitter[] generateTransmitterLocations(final int numTransmitters) {
    Random rand = new Random(Main.config.randomSeed);
    Transmitter[] txers = new Transmitter[numTransmitters];
    for (int i = 0; i < numTransmitters; ++i) {
      txers[i] = new Transmitter();
      txers[i].x = rand.nextFloat() * Main.config.squareSize;
      txers[i].y = rand.nextFloat() * Main.config.squareSize;
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
   * @param r
   *          the receiver that captures the packet.
   * @return the capture disk of transmitter t1, else {@code null} if non
   *         exists.
   */
  static Ellipse2D generateCaptureDisk(Transmitter t1, Transmitter t2,
      Receiver r) {
    Ellipse2D.Float captureDisk = new Ellipse2D.Float();
    double betaSquared = Math.pow(Main.config.beta, 2);
    double denominator = 1 - betaSquared;

    double centerX = (t1.getX() - (betaSquared * t2.getX())) / denominator;
    double centerY = (t1.getY() - (betaSquared * t2.getY())) / denominator;

    double euclideanDistance = Math.sqrt(Math.pow(t1.getX() - t2.getX(), 2)
        + Math.pow(t1.getY() - t2.getY(), 2));

    double radius = (Main.config.beta * euclideanDistance) / denominator;

    captureDisk.height = (float) (radius * 2);
    captureDisk.width = (float) (radius * 2);
    captureDisk.x = (float) (centerX - radius);
    captureDisk.y = (float) (centerY - radius);

    return captureDisk;
  }
}
