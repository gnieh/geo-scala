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

import com.free2move.geoscala.circe._
import io.circe._
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CirceDecodingAndEncodingTests extends AnyFlatSpec with Matchers with EitherValues {
  "The circe decoders" should "parse and serialize simple 2D points" in {
    val json = """{"type":"Point","coordinates":[12.3046875,51.8357775]}"""
    val obj = Point(Coordinate(12.3046875, 51.8357775))
    parser.decode[Point](json) shouldBe Right(obj)
    parser.decode[Geometry](json) shouldBe Right(obj)
    Printer.noSpaces.print(obj.asJson) shouldBe json
  }

  it should "parse points with more dimensions as 2D points" in {
    val json = """{"type":"Point","coordinates":[12.3046875,51.8357775,7.000,42.12345]}"""
    val obj = Point(Coordinate(12.3046875, 51.8357775))
    parser.decode[Point](json) shouldBe Right(obj)
    parser.decode[Geometry](json) shouldBe Right(obj)
  }

  it should "parse and serialize FeatureCollection using Json encoder and decoder" in {
    val json =
      """{"type":"FeatureCollection","features":[{"type":"Feature","properties":{"id":7},"geometry":{"type":"Point","coordinates":[12.3046875,51.8357775]}}]}"""
    val obj = FeatureCollection(List(Feature(Json.obj("id" := 7), Point(Coordinate(12.3046875, 51.8357775)))))
    parser.decode[FeatureCollection[Json]](json) shouldBe Right(obj)
    parser.decode[GeoJson[Json]](json) shouldBe Right(obj)
    Printer.noSpaces.print(obj.asJson) shouldBe json
  }
}
