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

import com.free2move.geoscala.jsoniter_scala._
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JsoniterScalaDecodingTests extends AnyFlatSpec with Matchers with EitherValues {
  "The jsoniter-scala codecs" should "handle simple 2D points" in {
    val json =
      """{
        "type": "Point",
        "coordinates": [
          12.3046875,
          51.8357775
        ]
      }"""
    readFromString[Point](json) shouldBe Point(Coordinate(12.3046875, 51.8357775))
    readFromString[Geometry](json) shouldBe Point(Coordinate(12.3046875, 51.8357775))
  }

  it should "handle points with more dimensions" in {
    val json =
      """{
        "type": "Point",
        "coordinates": [
          12.3046875,
          51.8357775,
          7.000,
          42.12345
        ]
      }"""
    readFromString[Point](json) shouldBe Point(Coordinate(12.3046875, 51.8357775))
    readFromString[Geometry](json) shouldBe Point(Coordinate(12.3046875, 51.8357775))
  }

  it should "handle FeatureCollection without Properties as pure JSON correctly" in {
    type Json = Map[String, Int]

    implicit val jsonCodec: JsonValueCodec[Json] = JsonCodecMaker.make

    val json =
      """{
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "properties": {
                "id": 7
              },
              "geometry": {
                "type": "Point",
                "coordinates": [
                  12.3046875,
                  51.8357775
                ]
              }
            }
          ]
        }"""
    readFromString[FeatureCollection[Json]](json) shouldBe FeatureCollection(
      List(Feature(Map("id" -> 7), Point(Coordinate(12.3046875, 51.8357775))))
    )
    readFromString[GeoJson[Json]](json) shouldBe FeatureCollection(
      List(Feature(Map("id" -> 7), Point(Coordinate(12.3046875, 51.8357775))))
    )
  }

}
