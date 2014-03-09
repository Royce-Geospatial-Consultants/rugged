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
package orekit.rugged.core.dem;

import org.apache.commons.math3.util.FastMath;
import org.orekit.rugged.api.TileUpdater;
import org.orekit.rugged.api.UpdatableTile;

public class ConstantElevationUpdater implements TileUpdater {

    private double size;
    private int    n;
    private double elevation;

    public ConstantElevationUpdater(double size, int n, double elevation) {
        this.size      = size;
        this.n         = n;
        this.elevation = elevation;
    }

    public void updateTile(double latitude, double longitude, UpdatableTile tile) {
        tile.setGeometry(size * FastMath.floor(latitude / size),
                         size * FastMath.floor(longitude / size),
                         size / n, size / n, n, n);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                tile.setElevation(i, j, elevation);
            }
        }
    }

}