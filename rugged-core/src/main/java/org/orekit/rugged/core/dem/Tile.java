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
package org.orekit.rugged.core.dem;

import org.orekit.rugged.api.RuggedException;
import org.orekit.rugged.api.UpdatableTile;

/** Interface representing a raster tile.
 * @author Luc Maisonobe
 */
public interface Tile extends UpdatableTile {

    /** Enumerate for point location with respect to tile. */
    enum Location {
        SOUTH_WEST,
        WEST,
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        IN_TILE
    }

    /** Hook called at the end of tile update completion.
     * @exception RuggedException if something wrong occurs
     * (missing data ...)
     */
    void tileUpdateCompleted() throws RuggedException;

    /** Get minimum latitude.
     * @return minimum latitude
     */
    double getMinimumLatitude();

    /** Get maximum latitude.
     * @return maximum latitude
     */
    double getMaximumLatitude();

    /** Get minimum longitude.
     * @return minimum longitude
     */
    double getMinimumLongitude();

    /** Get maximum longitude.
     * @return maximum longitude
     */
    double getMaximumLongitude();

    /** Get step in latitude (size of one raster element).
     * @return step in latitude
     */
    double getLatitudeStep();

    /** Get step in longitude (size of one raster element).
     * @return step in longitude
     */
    double getLongitudeStep();

    /** Get number of latitude rows.
     * @return number of latitude rows
     */
    int getLatitudeRows();

    /** Get number of longitude columns.
     * @return number of longitude columns
     */
    int getLongitudeColumns();

    /** Get the minimum elevation in the tile.
     * @return minimum elevation in the tile
     */
    double getMinElevation();

    /** Get the maximum elevation in the tile.
     * @return maximum elevation in the tile
     */
    double getMaxElevation();

    /** Get the elevation of an exact grid point.
     * @param latitudeIndex
     * @param longitudeIndex
     * @return elevation
     * @exception RuggedException if indices are out of bound
     */
    double getElevationAtIndices(int latitudeIndex, int longitudeIndex)
        throws RuggedException;

    /** Check if a tile covers a ground point.
     * @param latitude ground point latitude
     * @param longitude ground point longitude
     * @return location of the ground point with respect to tile
     */
    Location getLocation(double latitude, double longitude);

}
