/* Copyright 2013-2016 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.cs.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.rugged.api.AlgorithmId;
import org.orekit.rugged.api.BodyRotatingFrameId;
import org.orekit.rugged.api.EllipsoidId;
import org.orekit.rugged.api.InertialFrameId;
import org.orekit.rugged.api.Rugged;
import org.orekit.rugged.api.RuggedBuilder;
import org.orekit.rugged.errors.RuggedException;
import org.orekit.rugged.linesensor.LineSensor;
import org.orekit.rugged.linesensor.LinearLineDatation;
import org.orekit.rugged.linesensor.SensorPixel;
import org.orekit.rugged.los.LOSBuilder;
import org.orekit.rugged.los.FixedRotation;
import org.orekit.rugged.los.TimeDependentLOS;
import org.orekit.rugged.utils.ParameterType;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AngularDerivativesFilter;
import org.orekit.utils.CartesianDerivativesFilter;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.TimeStampedAngularCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

public class InverseLocation {

    public static void main(String[] args) {

        try {

            // Initialize Orekit, assuming an orekit-data folder is in user home directory
            File home       = new File(System.getProperty("user.home"));
            File orekitData = new File(home, "orekit-data");
            DataProvidersManager.getInstance().addProvider(new DirectoryCrawler(orekitData));

            // The raw viewing direction of pixel i with respect to the instrument is defined by the vector:
            List<Vector3D> rawDirs = new ArrayList<Vector3D>();
            for (int i = 0; i < 2000; i++) {
                //20° field of view, 2000 pixels
                rawDirs.add(new Vector3D(0d, i*FastMath.toRadians(20)/2000d, 1d));
            }

            // The instrument is oriented 10° off nadir around the X-axis, we need to rotate the viewing
            // direction to obtain the line of sight in the satellite frame
            LOSBuilder losBuilder = new LOSBuilder(rawDirs);
            losBuilder.addTransform(new FixedRotation(ParameterType.FIXED, Vector3D.PLUS_I, FastMath.toRadians(10)));

            TimeDependentLOS lineOfSight = losBuilder.build();

            // We use Orekit for handling time and dates, and Rugged for defining the datation model:
            TimeScale gps = TimeScalesFactory.getGPS();
            AbsoluteDate absDate = new AbsoluteDate("2009-12-11T16:59:30.0", gps);
            LinearLineDatation lineDatation = new LinearLineDatation(absDate, 1d, 20); 

            // With the LOS and the datation now defined , we can initialize a line sensor object in Rugged:
            String sensorName = "mySensor";
            LineSensor lineSensor = new LineSensor(sensorName, lineDatation, Vector3D.ZERO, lineOfSight);

            
            
            // In our application, we simply need to know the name of the frames we are working with. Positions and
            // velocities are given in the ITRF terrestrial frame, while the quaternions are given in EME2000
            // inertial frame.  
            Frame eme2000 = FramesFactory.getEME2000();
            boolean simpleEOP = true; // we don't want to compute tiny tidal effects at millimeter level
            Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, simpleEOP);

            ArrayList<TimeStampedAngularCoordinates> satelliteQList = new ArrayList<TimeStampedAngularCoordinates>();
            ArrayList<TimeStampedPVCoordinates> satellitePVList = new ArrayList<TimeStampedPVCoordinates>();

            addSatelliteQ(gps, satelliteQList, "2009-12-11T16:58:42.592937", -0.340236d, 0.333952d, -0.844012d, -0.245684d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T16:59:06.592937", -0.354773d, 0.329336d, -0.837871d, -0.252281d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T16:59:30.592937", -0.369237d, 0.324612d, -0.831445d, -0.258824d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T16:59:54.592937", -0.3836d, 0.319792d, -0.824743d, -0.265299d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T17:00:18.592937", -0.397834d, 0.314883d, -0.817777d, -0.271695d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T17:00:42.592937", -0.411912d, 0.309895d, -0.810561d, -0.278001d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T17:01:06.592937", -0.42581d, 0.304838d, -0.803111d, -0.284206d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T17:01:30.592937", -0.439505d, 0.299722d, -0.795442d, -0.290301d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T17:01:54.592937", -0.452976d, 0.294556d, -0.787571d, -0.296279d);
            addSatelliteQ(gps, satelliteQList, "2009-12-11T17:02:18.592937", -0.466207d, 0.28935d, -0.779516d, -0.302131d);

            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T16:58:42.592937", -726361.466d, -5411878.485d, 4637549.599d, -2463.635d, -4447.634d, -5576.736d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T16:59:04.192937", -779538.267d, -5506500.533d, 4515934.894d, -2459.848d, -4312.676d, -5683.906d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T16:59:25.792937", -832615.368d, -5598184.195d, 4392036.13d, -2454.395d, -4175.564d, -5788.201d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T16:59:47.392937", -885556.748d, -5686883.696d, 4265915.971d, -2447.273d, -4036.368d, -5889.568d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:00:08.992937", -938326.32d, -5772554.875d, 4137638.207d, -2438.478d, -3895.166d, -5987.957d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:00:30.592937", -990887.942d, -5855155.21d, 4007267.717d, -2428.011d, -3752.034d, -6083.317d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:00:52.192937", -1043205.448d, -5934643.836d, 3874870.441d, -2415.868d, -3607.05d, -6175.6d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:01:13.792937", -1095242.669d, -6010981.571d, 3740513.34d, -2402.051d, -3460.291d, -6264.76d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:01:35.392937", -1146963.457d, -6084130.93d, 3604264.372d, -2386.561d, -3311.835d, -6350.751d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:01:56.992937", -1198331.706d, -6154056.146d, 3466192.446d, -2369.401d, -3161.764d, -6433.531d);
            addSatellitePV(gps, eme2000, itrf, satellitePVList, "2009-12-11T17:02:18.592937", -1249311.381d, -6220723.191d, 3326367.397d, -2350.574d, -3010.159d, -6513.056d);

            Rugged rugged = new RuggedBuilder().
                    setAlgorithm(AlgorithmId.IGNORE_DEM_USE_ELLIPSOID). 
                    setEllipsoid(EllipsoidId.WGS84, BodyRotatingFrameId.ITRF).
                    setTimeSpan(absDate, absDate.shiftedBy(60.0), 0.01, 5 / lineSensor.getRate(0)). 
                    setTrajectory(InertialFrameId.EME2000,
                                  satellitePVList, 4, CartesianDerivativesFilter.USE_P,
                                  satelliteQList,  4,  AngularDerivativesFilter.USE_R).
                                  addLineSensor(lineSensor).
                                  build();

            // Point defined by its latitude, longitude and altitude
            double latitude = FastMath.toRadians(37.585);
            double longitude = FastMath.toRadians(-96.949);
            double altitude = 0.0d;
            GeodeticPoint gp = new GeodeticPoint(latitude, longitude, altitude);

            // Search the sensor pixel seeing point
            int minLine = 0;
            int maxLine = 100;
            SensorPixel sensorPixel = rugged.inverseLocation(sensorName, gp, minLine, maxLine);
            // we need to test if the sensor pixel is found in the prescribed lines otherwise the sensor pixel is null
            if (sensorPixel != null){
                System.out.format(Locale.US, "Sensor Pixel found : line = %5.3f, pixel = %5.3f %n", sensorPixel.getLineNumber(), sensorPixel.getPixelNumber());
            } else {
                System.out.println("Sensor Pixel is null: point cannot be seen between the prescribed line numbers\n");
            }
            
            // Find the date at which the sensor sees the ground point
            AbsoluteDate dateLine = rugged.dateLocation(sensorName, gp, minLine, maxLine);
            System.out.println("Date at which the sensor sees the ground point : " + dateLine);
            
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
            System.exit(1);
        } catch (RuggedException re) {
            System.err.println(re.getLocalizedMessage());
            System.exit(1);
        }

    }

    private static void addSatellitePV(TimeScale gps, Frame eme2000, Frame itrf,
                                  ArrayList<TimeStampedPVCoordinates> satellitePVList,
                                  String absDate,
                                  double px, double py, double pz, double vx, double vy, double vz)
        throws OrekitException {
        AbsoluteDate ephemerisDate = new AbsoluteDate(absDate, gps);
        Vector3D position = new Vector3D(px, py, pz);
        Vector3D velocity = new Vector3D(vx, vy, vz);
        PVCoordinates pvITRF = new PVCoordinates(position, velocity);
        Transform transform = itrf.getTransformTo(eme2000, ephemerisDate);
        PVCoordinates pvEME2000 = transform.transformPVCoordinates(pvITRF); 
        satellitePVList.add(new TimeStampedPVCoordinates(ephemerisDate, pvEME2000.getPosition(), pvEME2000.getVelocity(), Vector3D.ZERO));
    }

    private static void addSatelliteQ(TimeScale gps, ArrayList<TimeStampedAngularCoordinates> satelliteQList, String absDate,
                                      double q0, double q1, double q2, double q3) {
        AbsoluteDate attitudeDate = new AbsoluteDate(absDate, gps);
        Rotation rotation = new Rotation(q0, q1, q2, q3, true);
        TimeStampedAngularCoordinates pair =
                new TimeStampedAngularCoordinates(attitudeDate, rotation, Vector3D.ZERO, Vector3D.ZERO);
        satelliteQList.add(pair);
    }

}
