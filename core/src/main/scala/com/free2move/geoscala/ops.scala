/*
 * Copyright 2019 GHM Mobile Development GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.free2move.geoscala

/**
  * A collection of implicit classes providing geometrical operations/calculations as syntax extensions.
  */
object ops {

  /**
    * A representation of a bounding box (= rectangle aligned with coordinate axis) by specification of
    * the most south-west and the most north-east points.
    * @param southwest the point with smallest longitude and latitude
    * @param northeast the point with the biggest longitude and latitude
    */
  case class Bounds(southwest: Point, northeast: Point)

  object Bounds extends ((Double, Double, Double, Double) => Bounds) {

    /**
      * Create a [[Bounds]] by specifying the longitude and latitude extremes.
      * @param minLon the minimum longitude
      * @param minLat the minimum latitude
      * @param maxLon the maximum longitude
      * @param maxLat the maximum latitude
      * @return a [[Bounds]] object constructed by combining the minimum and maximum coordinate values.
      */
    def apply(minLon: Double, minLat: Double, maxLon: Double, maxLat: Double): Bounds =
      Bounds(Point(Coordinate(longitude = minLon, latitude = minLat)), Point(Coordinate(longitude = maxLon, latitude = maxLat)))
  }

  implicit class GeometryOps(geometry: Geometry) {

    /**
      * Calculate the area of the given geometry - returns 0.0 for everything which isn't a (multi-)polygon.
      * Holes are subtracted from the area, so invalid polygons can lead to negative results.
      * @return area
      */
    def area: Double = geometry match {
      case Point(_)                  => 0.0
      case MultiPoint(_)             => 0.0
      case LineString(_)             => 0.0
      case MultiLineString(_)        => 0.0
      case Polygon(outer :: holes)   => calcPolygonArea(outer) - holes.foldLeft(0.0)(_ + calcPolygonArea(_))
      case Polygon(Nil)              => 0.0
      case MultiPolygon(coordinates) => coordinates.map(Polygon(_).area).sum
    }

    private def calcPolygonArea(coordinates: List[Coordinate]): Double = {
      0.5 * coordinates
        .zip(coordinates.tail)
        .map {
          case (first, second) =>
            first.longitude * second.latitude - second.longitude * first.latitude
        }
        .sum
    }

    /** Returns the envelope including this geometry if any.
      *  - If the geometry is _empty_, returns `None`
      *  - If the geometry is a point, then the envelope is the point itself.
      *  - Otherwise, the envelope is the minimal bounding box that contains the geometry.
      */
    def envelope: Option[Geometry] = geometry match {
      case p: Point                     => Some(p)
      case MultiPoint(coordinates)      => makeEnvelope(coordinates)
      case LineString(coordinates)      => makeEnvelope(coordinates)
      case MultiLineString(coordinates) => makeEnvelope(coordinates.flatten)
      case Polygon(coordinates)         => coordinates.headOption.flatMap(makeEnvelope)
      case MultiPolygon(coordinates)    => makeEnvelope(coordinates.flatMap(_.headOption).flatten)
    }

    /** Returns the envelope including this geometry, if any, as bounds.
      *  - If the geometry is _empty_, returns `None`
      *  - Otherwise, the envelope is the minimal bounding box that contains the geometry.
      */
    def envelopeBounds: Option[Bounds] = geometry match {
      case p: Point                     => Some(Bounds(p, p))
      case MultiPoint(coordinates)      => makeEnvelopeBounds(coordinates).map(Bounds.tupled)
      case LineString(coordinates)      => makeEnvelopeBounds(coordinates).map(Bounds.tupled)
      case MultiLineString(coordinates) => makeEnvelopeBounds(coordinates.flatten).map(Bounds.tupled)
      case Polygon(coordinates)         => coordinates.headOption.flatMap(makeEnvelopeBounds).map(Bounds.tupled)
      case MultiPolygon(coordinates)    => makeEnvelopeBounds(coordinates.flatMap(_.headOption).flatten).map(Bounds.tupled)
    }

    private def makeEnvelopeBounds(coordinates: List[Coordinate]): Option[(Double, Double, Double, Double)] = coordinates match {
      case Coordinate(lon, lat) :: rest =>
        val bounds = rest.foldLeft((lon, lat, lon, lat)) {
          case ((minLon, minLat, maxLon, maxLat), Coordinate(lon, lat)) =>
            val minLon1 = math.min(minLon, lon)
            val maxLon1 = math.max(maxLon, lon)
            val minLat1 = math.min(minLat, lat)
            val maxLat1 = math.max(maxLat, lat)
            (minLon1, minLat1, maxLon1, maxLat1)
        }
        Some(bounds)
      case Nil =>
        None
    }

    private def makeEnvelope(coordinates: List[Coordinate]): Option[Geometry] =
      makeEnvelopeBounds(coordinates).map {
        case (minLon, minLat, maxLon, maxLat) =>
          val parallelX = minLat == maxLat
          val parallelY = minLon == maxLon
          val env =
            if (parallelX && parallelY)
              Point(Coordinate(minLon, minLat))
            else if (parallelX || parallelY)
              LineString(List(Coordinate(minLon, minLat), Coordinate(maxLon, maxLat)))
            else
              Polygon(
                List(
                  List(Coordinate(minLon, minLat),
                       Coordinate(minLon, maxLat),
                       Coordinate(maxLon, maxLat),
                       Coordinate(maxLon, minLat),
                       Coordinate(minLon, minLat))
                )
              )
          env
      }
  }

  implicit class GeoJsonOps[Properties](geojson: GeoJson[Properties]) {

    /**
      * Helper to extract the first geometry of a GeoJSON - for geometries, it the identity, for features it
      * returns the attached geometry and for feature collections the geometry of the first feature.
      * @return the first geometry
      */
    def firstGeometry: Option[Geometry] = geojson match {
      case FeatureCollection(features) => features.headOption.map(_.geometry)
      case Feature(_, geometry)        => Some(geometry)
      case geometry: Geometry          => Some(geometry)
    }

  }

}
