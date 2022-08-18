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

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ops._

class EnvelopeTest extends AnyFlatSpec with Matchers with OptionValues {

  "The envelope" should "be the bounding box for a polygon" in {
    val poly = Polygon(List(List(Coordinate(-121, 39), Coordinate(-119, 39), Coordinate(-119, 41), Coordinate(-121, 41), Coordinate(-121, 39))))
    val envelope = Polygon(List(List(Coordinate(-121, 39), Coordinate(-121, 41), Coordinate(-119, 41), Coordinate(-119, 39), Coordinate(-121, 39))))
    poly.envelope shouldBe defined
    poly.envelope.value shouldBe envelope
  }

  "The envelope" should "be the bounding box for a polygon with holes" in {
    val poly = Polygon(
      List(
        List(Coordinate(-121, 39), Coordinate(-119, 39), Coordinate(-119, 41), Coordinate(-121, 41), Coordinate(-121, 39)),
        List(Coordinate(-121, 41), Coordinate(-119.5, 40.5), Coordinate(-119.6, 40.6), Coordinate(-121, 41))
      )
    )
    val envelope = Polygon(List(List(Coordinate(-121, 39), Coordinate(-121, 41), Coordinate(-119, 41), Coordinate(-119, 39), Coordinate(-121, 39))))
    poly.envelope shouldBe defined
    poly.envelope.value shouldBe envelope
  }

  it should "be the bounding box including all parts of a multi polygon" in {
    val poly = MultiPolygon(List(List(List(Coordinate(-121, 39), Coordinate(-119, 39), Coordinate(-119, 41), Coordinate(-121, 41), Coordinate(-121, 39)))))
    val envelope = Polygon(List(List(Coordinate(-121, 39), Coordinate(-121, 41), Coordinate(-119, 41), Coordinate(-119, 39), Coordinate(-121, 39))))
    poly.envelope shouldBe defined
    poly.envelope.value shouldBe envelope
  }

}
