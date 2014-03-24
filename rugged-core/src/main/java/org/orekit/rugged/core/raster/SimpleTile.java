/* Copyright 2013-2014 CS Systèmes d'Information
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
package org.orekit.rugged.core.raster;

import org.apache.commons.math3.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.rugged.api.RuggedException;
import org.orekit.rugged.api.RuggedMessages;


/** Simple implementation of a {@link Tile}.
 * @see SimpleTileFactory
 * @author Luc Maisonobe
 */
public class SimpleTile implements Tile {

    /** Tolerance used to interpolate points slightly out of tile (in pixels). */
    private static final double TOLERANCE = 1.0 / 8.0;

    /** Minimum latitude. */
    private double minLatitude;

    /** Minimum longitude. */
    private double minLongitude;

    /** Step in latitude (size of one raster element). */
    private double latitudeStep;

    /** Step in longitude (size of one raster element). */
    private double longitudeStep;

    /** Number of latitude rows. */
    private int latitudeRows;

    /** Number of longitude columns. */
    private int longitudeColumns;

    /** Minimum elevation. */
    private double minElevation;

    /** Maximum elevation. */
    private double maxElevation;

    /** Elevation array. */
    private double[] elevations;

    /** Simple constructor.
     * <p>
     * Creates an empty tile.
     * </p>
     */
    protected SimpleTile() {
    }

    /** {@inheritDoc} */
    @Override
    public void setGeometry(final double minLatitude, final double minLongitude,
                            final double latitudeStep, final double longitudeStep,
                            final int latitudeRows, final int longitudeColumns)
        throws RuggedException {
        this.minLatitude      = minLatitude;
        this.minLongitude     = minLongitude;
        this.latitudeStep     = latitudeStep;
        this.longitudeStep    = longitudeStep;
        this.latitudeRows     = latitudeRows;
        this.longitudeColumns = longitudeColumns;
        this.minElevation     = Double.POSITIVE_INFINITY;
        this.maxElevation     = Double.NEGATIVE_INFINITY;

        if (latitudeRows < 1 || longitudeColumns < 1) {
            throw new RuggedException(RuggedMessages.EMPTY_TILE, latitudeRows, longitudeColumns);
        }
        this.elevations = new double[latitudeRows * longitudeColumns];

    }

    /** {@inheritDoc} */
    @Override
    public void tileUpdateCompleted() throws RuggedException {
        processUpdatedElevation(elevations);
    }

    /** Process elevation array at completion.
     * </p>
     * This method is called at tile update completion, it is
     * expected to be overridden by subclasses. The default
     * implementation does nothing.
     * </p>
     * @param elevations elevations array
     */
    protected void processUpdatedElevation(final double[] elevations) {
        // do nothing by default
    }

    /** {@inheritDoc} */
    @Override
    public double getMinimumLatitude() {
        return getLatitudeAtIndex(0);
    }

    /** {@inheritDoc} */
    @Override
    public double getLatitudeAtIndex(final int latitudeIndex) {
        return minLatitude + latitudeStep * latitudeIndex;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaximumLatitude() {
        return getLatitudeAtIndex(latitudeRows - 1);
    }

    /** {@inheritDoc} */
    @Override
    public double getMinimumLongitude() {
        return getLongitudeAtIndex(0);
    }

    /** {@inheritDoc} */
    @Override
    public double getLongitudeAtIndex(final int longitudeIndex) {
        return minLongitude + longitudeStep * longitudeIndex;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaximumLongitude() {
        return getLongitudeAtIndex(longitudeColumns - 1);
    }

    /** {@inheritDoc} */
    @Override
    public double getLatitudeStep() {
        return latitudeStep;
    }

    /** {@inheritDoc} */
    @Override
    public double getLongitudeStep() {
        return longitudeStep;
    }

    /** {@inheritDoc} */
    @Override
    public int getLatitudeRows() {
        return latitudeRows;
    }

    /** {@inheritDoc} */
    @Override
    public int getLongitudeColumns() {
        return longitudeColumns;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinElevation() {
        return minElevation;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxElevation() {
        return maxElevation;
    }

    /** {@inheritDoc} */
    @Override
    public void setElevation(final int latitudeIndex, final int longitudeIndex,
                             final double elevation) throws RuggedException {
        if (latitudeIndex  < 0 || latitudeIndex  > (latitudeRows - 1) ||
            longitudeIndex < 0 || longitudeIndex > (longitudeColumns - 1)) {
            throw new RuggedException(RuggedMessages.OUT_OF_TILE_INDICES,
                                      latitudeIndex, longitudeIndex,
                                      latitudeRows - 1, longitudeColumns - 1);
        }        
        minElevation = FastMath.min(minElevation, elevation);
        maxElevation = FastMath.max(maxElevation, elevation);
        elevations[latitudeIndex * getLongitudeColumns() + longitudeIndex] = elevation;
    }

    /** {@inheritDoc} */
    @Override
    public double getElevationAtIndices(int latitudeIndex, int longitudeIndex) {
        return elevations[latitudeIndex * getLongitudeColumns() + longitudeIndex];
    }

    /** {@inheritDoc}
     * <p>
     * This classes uses an arbitrary 1/8 pixel tolerance for interpolating
     * slightly out of tile points.
     * </p>
     */
    @Override
    public double interpolateElevation(final double latitude, final double longitude)
        throws RuggedException {

        final double doubleLatitudeIndex  = getDoubleLatitudeIndex(latitude);
        final double doubleLongitudeIndex = getDoubleLontitudeIndex(longitude);
        if (doubleLatitudeIndex  < -TOLERANCE || doubleLatitudeIndex  >= (latitudeRows - 1 + TOLERANCE) ||
            doubleLongitudeIndex < -TOLERANCE || doubleLongitudeIndex >= (longitudeColumns - 1 + TOLERANCE)) {
            throw new RuggedException(RuggedMessages.OUT_OF_TILE_ANGLES,
                                      FastMath.toDegrees(latitude),
                                      FastMath.toDegrees(longitude),
                                      FastMath.toDegrees(getMinimumLatitude()),
                                      FastMath.toDegrees(getMaximumLatitude()),
                                      FastMath.toDegrees(getMinimumLongitude()),
                                      FastMath.toDegrees(getMaximumLongitude()));
        }

        final int latitudeIndex  = FastMath.max(0,
                                                FastMath.min(latitudeRows - 2,
                                                             (int) FastMath.floor(doubleLatitudeIndex)));
        final int longitudeIndex = FastMath.max(0,
                                                FastMath.min(longitudeColumns - 2,
                                                             (int) FastMath.floor(doubleLongitudeIndex)));

        // bi-linear interpolation
        final double dLat = doubleLatitudeIndex  - latitudeIndex;
        final double dLon = doubleLongitudeIndex - longitudeIndex;
        final double e00  = getElevationAtIndices(latitudeIndex,     longitudeIndex);
        final double e10  = getElevationAtIndices(latitudeIndex,     longitudeIndex + 1);
        final double e01  = getElevationAtIndices(latitudeIndex + 1, longitudeIndex);
        final double e11  = getElevationAtIndices(latitudeIndex + 1, longitudeIndex + 1);
        return (e00 * (1.0 - dLon) + dLon * e10) * (1.0 - dLat) +
               (e01 * (1.0 - dLon) + dLon * e11) * dLat;

    }

    /** {@inheritDoc} */
    @Override
    public GeodeticPoint pixelIntersection(final GeodeticPoint pA, final GeodeticPoint pB,
                                           final int latitudeIndex, final int longitudeIndex)
        throws RuggedException {

        // Digital Elevation Mode coordinates at pixel vertices
        final double x00 = getLongitudeAtIndex(longitudeIndex);
        final double y00 = getLatitudeAtIndex(latitudeIndex);
        final double z00 = getElevationAtIndices(latitudeIndex,     longitudeIndex);
        final double z01 = getElevationAtIndices(latitudeIndex + 1, longitudeIndex);
        final double z10 = getElevationAtIndices(latitudeIndex,     longitudeIndex + 1);
        final double z11 = getElevationAtIndices(latitudeIndex + 1, longitudeIndex + 1);

        // line-of-sight coordinates at close points
        final double dxA = (pA.getLongitude() - x00) / longitudeStep;
        final double dyA = (pA.getLatitude()  - y00) / latitudeStep;
        final double dzA = pA.getAltitude();
        final double dxB = (pB.getLongitude() - x00) / longitudeStep;
        final double dyB = (pB.getLatitude()  - y00) / latitudeStep;
        final double dzB = pB.getAltitude();
        
        // points along line-of-sight can be defined as a linear combination
        // between pA and pB depending on free variable t: p(t) = pA * (1 - t) + pB * t.
        // As the point latitude and longitude are linear with respect to t,
        // and as Digital Elevation Model is quadratic with respect to latitude
        // and longitude, the altitude of DEM at same horizontal position as
        // point is quadratic in t:
        // z_DEM(t) = u t² + v t + w
        final double u = (dxA - dxB) * (dyA - dyB) * (z00 - z10 - z01 + z11);
        final double v = ((dxA - dxB) * (1 - dyA) + (dyA - dyB) * (1 - dxA)) * z00 +
                         (dxA * (dyA - dyB) - (dxA - dxB) * (1 - dyA)) * z10 +
                         (dyA * (dxA - dxB) - (dyA - dyB) * (1 - dxA)) * z01 +
                         ((dxB - dxA) * dyA + (dyB - dyA) * dxA) * z11;
        final double w = (1 - dxA) * ((1 - dyA) * z00 + dyA * z01) +
                         dxA       * ((1 - dyA) * z10 + dyA * z11);

        // subtract linear z from line-of-sight
        // z_DEM(t) - z_LOS(t) = a t² + b t + c
        final double a = u;
        final double b = v + dzA - dzB;
        final double c = w - dzA;

        // solve the quadratic equation
        final double b2  = b * b;
        final double fac = 4 * a * c;
        if (b2 < fac) {
            // no intersection at all
            return null;
        }
        final double s  = FastMath.sqrt(b2 - fac);
        final double t1 = (b < 0) ? (s - b) / (2 * a) : -2 * c / (b + s);
        final double t2 = c / (a * t1);

        final double dx1 = dxA  * (1 - t1) + dxB  * t1;
        final double dy1 = dyA  * (1 - t1) + dyB  * t1;
        final GeodeticPoint p1;
        if (dx1 >= 0 && dx1 <= 1 && dy1 >= 0 && dy1 <= 1) {
            p1 = new GeodeticPoint(pA.getLatitude()  * (1 - t1) + pB.getLatitude()  * t1,
                                   pA.getLongitude() * (1 - t1) + pB.getLongitude() * t1,
                                   pA.getAltitude()  * (1 - t1) + pB.getAltitude()  * t1);
        } else {
            p1    = null;
        }

        final double dx2 = dxA  * (1 - t2) + dxB  * t2;
        final double dy2 = dyA  * (1 - t2) + dyB  * t2;
        final GeodeticPoint p2;
        if (dx2 >= 0 && dx2 <= 1 && dy2 >= 0 && dy2 <= 1) {
            p2 = new GeodeticPoint(pA.getLatitude()  * (1 - t2) + pB.getLatitude()  * t2,
                                   pA.getLongitude() * (1 - t2) + pB.getLongitude() * t2,
                                   pA.getAltitude()  * (1 - t2) + pB.getAltitude()  * t2);
        } else {
            p2    = null;
        }

        // select the first point along line-of-sight
        if (p1 == null) {
            return p2;
        } else if (p2 == null) {
            return p1;
        } else {
            return t1 <= t2 ? p1 : p2;
        }

    }

    /** {@inheritDoc} */
    @Override
    public int getLatitudeIndex(double latitude) {
        return (int) FastMath.floor(getDoubleLatitudeIndex(latitude));
    }

    /** {@inheritDoc} */
    @Override
    public int getLongitudeIndex(double longitude) {
        return (int) FastMath.floor(getDoubleLontitudeIndex(longitude));
    }

    /** Get the latitude index of a point.
     * @param latitude geodetic latitude
     * @return latitute index (it may lie outside of the tile!)
     */
    private double getDoubleLatitudeIndex(double latitude) {
        return (latitude  - minLatitude)  / latitudeStep;
    }

    /** Get the longitude index of a point.
     * @param longitude geodetic latitude
     * @return longitude index (it may lie outside of the tile!)
     */
    private double getDoubleLontitudeIndex(double longitude) {
        return (longitude - minLongitude) / longitudeStep;
    }

    /** {@inheritDoc} */
    @Override
    public Location getLocation(final double latitude, final double longitude) {
        final int latitudeIndex  = getLatitudeIndex(latitude);
        final int longitudeIndex = getLongitudeIndex(longitude);
        if (longitudeIndex < 0) {
            if (latitudeIndex < 0) {
                return Location.SOUTH_WEST;
            } else if (latitudeIndex <= (latitudeRows - 2)) {
                return Location.WEST;
            } else {
                return Location.NORTH_WEST;
            }
        } else if (longitudeIndex <= (longitudeColumns - 2)) {
            if (latitudeIndex < 0) {
                return Location.SOUTH;
            } else if (latitudeIndex <= (latitudeRows - 2)) {
                return Location.IN_TILE;
            } else {
                return Location.NORTH;
            }
        } else {
            if (latitudeIndex < 0) {
                return Location.SOUTH_EAST;
            } else if (latitudeIndex <= (latitudeRows - 2)) {
                return Location.EAST;
            } else {
                return Location.NORTH_EAST;
            }
        }
    }

}