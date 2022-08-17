/*
 * Copyright 2019 GHM Mobile Development GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.free2move.geoscala

/** Marker trait for all types which represent a GeoJSON object.
  * @tparam Properties
  *   type of representing the `properties` of a [[Feature]], `Nothing` in case it's not a feature or feature collection.
  */
sealed trait GeoJson[Properties]

/** A feature collection as of §3.3 of RFC 7946.
  * @param features
  *   the features of this collection, potentially empty
  * @tparam Properties
  *   type of representing the `properties` of a [[Feature]], `Nothing` in case it's not a feature or feature collection.
  */
case class FeatureCollection[Properties](features: List[Feature[Properties]]) extends GeoJson[Properties]

/** A feature object as of §3.2 of RFC 7946.
  * @param properties
  *   the properties object attached to this feature
  * @param geometry
  *   the [[Geometry]] attached to this feature
  * @tparam Properties
  *   type of representing the `properties` of a [[Feature]], `Nothing` in case it's not a feature or feature collection.
  */
case class Feature[Properties](properties: Properties, geometry: Geometry) extends GeoJson[Properties]

object Feature {

  /** Convenience apply to create a feature with `Unit` as properties representation (for cases where properties don't matter).
    * @param geometry
    *   the [[Geometry]] attached to this feature
    * @return
    *   the created feature
    */
  def apply(geometry: Geometry): Feature[Unit] = new Feature((), geometry)
}

/** A representation of a coordinate as a pair of longitude and latitude.
  * @param longitude
  *   the longitude of the coordinate
  * @param latitude
  *   the latitude of the coordinate.
  */
case class Coordinate(longitude: Double, latitude: Double)

/** Marker trait for GeoJSON object which represents a geometry as of §3.1 of RFC 7946.
  */
sealed trait Geometry extends GeoJson[Nothing]

/** A point represents a single coordinate - §3.1.2 of RFC 7946.
  * @param coordinates
  *   a single coordinate to specify the point's location.
  */
case class Point(coordinates: Coordinate) extends Geometry

/** A collection of points - §3.1.3 of RFC 7946.
  * @param coordinates
  *   a `List` of coordinates representing the single points.
  */
case class MultiPoint(coordinates: List[Coordinate]) extends Geometry

/** A line string is connection between an ordered collection of points - §3.1.4 of RFC 7946.
  * @param coordinates
  *   the list of coordinates representing the points this line string goes through.
  */
case class LineString(coordinates: List[Coordinate]) extends Geometry

/** The multi version of a line string - §3.1.5 of RFC 7946.
  * @param coordinates
  *   list of line string data, which itself is a list of coordinates
  */
case class MultiLineString(coordinates: List[List[Coordinate]]) extends Geometry

/** A polygon is a 2d shape which can have holes - §3.1.6 of RFC 7946. The first list of coordinates is the outer ring, the following coordinate list are the
  * holes.
  * @param coordinates
  *   list of outer ring followed by (optional) holes.
  */
case class Polygon(coordinates: List[List[Coordinate]]) extends Geometry

/** The multi version of a polygon - §3.1.7 of RFC 7946.
  * @param coordinates
  *   the list of polygon data
  */
case class MultiPolygon(coordinates: List[List[List[Coordinate]]]) extends Geometry
