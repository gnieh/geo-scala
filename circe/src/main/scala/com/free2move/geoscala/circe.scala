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

import cats.syntax.functor._
import io.circe._
import io.circe.syntax._

trait LowPriorityGeoJsonEncoders {
  implicit val coordinateEncoder: Encoder[Coordinate] = Encoder.instance { coord =>
    Json.arr(Json.fromDoubleOrNull(coord.longitude), Json.fromDoubleOrNull(coord.latitude))
  }

  private def makeGeometryEncoder[C: Encoder, G <: Geometry](`type`: String, coords: G => C): Encoder[G] = Encoder.instance { geometry =>
    Json.obj("type" := `type`, "coordinates" := coords(geometry))
  }

  implicit val pointEncoder: Encoder[Point] = makeGeometryEncoder("Point", _.coordinates)

  implicit val multiPointEncoder: Encoder[MultiPoint] = makeGeometryEncoder("MultiPoint", _.coordinates)

  implicit val lineStringEncoder: Encoder[LineString] = makeGeometryEncoder("LineString", _.coordinates)

  implicit val multiLineStringEncoder: Encoder[MultiLineString] = makeGeometryEncoder("MultiLineString", _.coordinates)

  implicit val polygonEncoder: Encoder[Polygon] = makeGeometryEncoder("Polygon", _.coordinates)

  implicit val multiPolygonEncoder: Encoder[MultiPolygon] = makeGeometryEncoder("MultiPolygon", _.coordinates)

}

trait GeoJsonEncoders extends LowPriorityGeoJsonEncoders {
  implicit val geometryEncoder: Encoder[Geometry] = Encoder.instance {
    case p: Point             => p.asJson
    case mp: MultiPoint       => mp.asJson
    case ls: LineString       => ls.asJson
    case mls: MultiLineString => mls.asJson
    case p: Polygon           => p.asJson
    case mp: MultiPolygon     => mp.asJson
  }

  implicit def extendedFeatureEncoder[Properties: Encoder.AsObject]: Encoder[Feature[Properties]] = Encoder.instance { feature =>
    Json.obj("type" := "Feature", "properties" := feature.properties, "geometry" := feature.geometry)
  }

  implicit def extendedFeatureCollectionEncoder[Properties: Encoder.AsObject]: Encoder[FeatureCollection[Properties]] = Encoder.instance { featureCollection =>
    Json.obj("type" := "FeatureCollection", "features" := featureCollection.features)
  }

  implicit def geojsonEncoder[Properties: Encoder.AsObject]: Encoder[GeoJson[Properties]] = Encoder.instance {
    case fc @ FeatureCollection(_) => fc.asJson
    case f @ Feature(_, _)         => f.asJson
    case geom: Geometry            => (geom: Geometry).asJson
  }
}

trait GeoJsonDecoders {
  implicit val coordinateDecoder: Decoder[Coordinate] = Decoder.instance { cursor =>
    for {
      lng <- cursor.downN(0).as[Double]
      lat <- cursor.downN(1).as[Double]
    } yield Coordinate(lng, lat)
  }

  @inline
  private def makeGeometryDecoder[C: Decoder, G <: Geometry](`type`: String, create: C => G): Decoder[G] = Decoder.instance[G] { cursor =>
    for {
      _ <- ensureType(cursor, `type`)
      coords <- cursor.downField("coordinates").as[C]
    } yield create(coords)
  }

  implicit val pointDecoder: Decoder[Point] = makeGeometryDecoder("Point", Point.apply)

  implicit val multiPointDecoder: Decoder[MultiPoint] = makeGeometryDecoder("MultiPoint", MultiPoint.apply)

  implicit val lineStringDecoder: Decoder[LineString] = makeGeometryDecoder("LineString", LineString.apply)

  implicit val multiLineStringDecoder: Decoder[MultiLineString] = makeGeometryDecoder("MultiLineString", MultiLineString.apply)

  implicit val polygonDecoder: Decoder[Polygon] = makeGeometryDecoder("Polygon", Polygon.apply)

  implicit val multiPolygonDecoder: Decoder[MultiPolygon] = makeGeometryDecoder("MultiPolygon", MultiPolygon.apply)

  implicit val geometryDecoder: Decoder[Geometry] = List[Decoder[Geometry]](
    pointDecoder.widen,
    multiPointDecoder.widen,
    lineStringDecoder.widen,
    multiLineStringDecoder.widen,
    polygonDecoder.widen,
    multiPolygonDecoder.widen
  ).reduce(_ or _)

  implicit def geojsonDecoder[Properties: Decoder]: Decoder[GeoJson[Properties]] =
    List[Decoder[GeoJson[Properties]]](
      extendedFeatureDecoder[Properties].widen,
      extendedFeatureCollectionDecoder[Properties].widen,
      geometryDecoder.widen[GeoJson[Nothing]].asInstanceOf[Decoder[GeoJson[Properties]]]
    ).reduce(_ or _)

  implicit def extendedFeatureDecoder[Properties: Decoder]: Decoder[Feature[Properties]] = Decoder.instance { cursor =>
    for {
      _ <- ensureType(cursor, "Feature")
      properties <- cursor.downField("properties").as[Properties]
      geometry <- cursor.downField("geometry").as[Geometry]
    } yield Feature(properties, geometry)
  }

  implicit def extendedFeatureCollectionDecoder[Properties: Decoder]: Decoder[FeatureCollection[Properties]] = Decoder.instance { cursor =>
    for {
      _ <- ensureType(cursor, "FeatureCollection")
      features <- cursor.downField("features").as[List[Feature[Properties]]]
    } yield FeatureCollection(features)
  }

  @inline
  private def ensureType(cursor: HCursor, tpe: String): Decoder.Result[String] = {
    val typeCursor = cursor.downField("type")
    typeCursor.as[String] match {
      case Right(b) if b != tpe => Left(DecodingFailure(s"GeoJSON's type is not $tpe", typeCursor.history))
      case res                     => res
    }
  }
}

/** Object with implicit circe encoders and decoders. You can alternatively mixin [[GeoJsonEncoders]] and [[GeoJsonDecoders]] instead of importing.
  *
  * Note: If you want to decode feature (collections), you need to parameterize them with a type that has a `Decoder`, one easy option in case you don't care
  * about them is `Json` itself.
  */
object circe extends GeoJsonEncoders with GeoJsonDecoders
