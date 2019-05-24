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

import org.scalatest._

import ops._

class AreaTest extends FlatSpec with Matchers with OptionValues {

  "The area" should "be correct for a square" in {
    val poly = Polygon(List(List(Coordinate(-121, 39), Coordinate(-119, 39), Coordinate(-119, 41), Coordinate(-121, 41), Coordinate(-121, 39))))
    poly.area shouldBe 4.0
  }

  it should "be correct for a polygon with a hole" in {
    val poly = Polygon(
      List(
        List(Coordinate(-121, 39), Coordinate(-119, 39), Coordinate(-119, 41), Coordinate(-121, 41), Coordinate(-121, 39)),
        List(Coordinate(-121, 40), Coordinate(-120, 40), Coordinate(-120, 41), Coordinate(-121, 41), Coordinate(-121, 40))
      )
    )
    poly.area shouldBe 3.0
  }

  it should "be correct for a single-element multi polygon" in {
    val poly = MultiPolygon(List(List(List(Coordinate(-121, 39), Coordinate(-119, 39), Coordinate(-119, 41), Coordinate(-121, 41), Coordinate(-121, 39)))))
    poly.area shouldBe 4.0
  }

}
