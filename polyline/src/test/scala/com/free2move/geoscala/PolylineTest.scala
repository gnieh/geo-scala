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

import org.scalacheck._
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PolylineTest extends FlatSpec with Matchers with OptionValues with TryValues with ScalaCheckDrivenPropertyChecks {

  "The polyline encoding" should "be correct for the Google sample" in {
    val example = LineString(List(Coordinate(38.5, -120.2), Coordinate(40.7, -120.95), Coordinate(43.252, -126.453)))
    polyline.encode(example) shouldBe "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
  }

  "The polyline decoding" should "be correct for the Google sample" in {
    val example = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
    val expected = LineString(List(Coordinate(38.5, -120.2), Coordinate(40.7, -120.95), Coordinate(43.252, -126.453)))
    val result = polyline.decode(example)
    result.success.value shouldBe expected
  }

  it should "handle an example Lime trip" in {
    val poly =
      "q_|}Hu|f_C_@O?A@CW~@NxAMv@[r@e@r@e@n@Uh@En@CbAUv@Ut@_@b@]b@a@f@c@j@En@RzA^pAh@fAl@rBThAPz@RbAZ|ALn@Nz@Jf@^pAZlAf@pBTfBLbCFpAFfBDnAF|AF|AFbBHpBFvAHlBFpAHnBJjBDl@RzB"
    val decoded = polyline.decode(poly).success.value
    decoded.coordinates.length shouldBe 50
    polyline.encode(decoded) shouldBe poly
  }

  implicit val coordGen: Gen[Coordinate] = for {
    lat <- Gen.chooseNum[Double](-90.0, +90.0)
    lng <- Gen.chooseNum[Double](-180.0, +180.0)
  } yield Coordinate(lng, lat)

  implicit val lsGen: Gen[LineString] = for {
    len <- Gen.chooseNum(0, 100)
    coords <- Gen.listOfN[Coordinate](len, coordGen)
  } yield LineString(coords)

  implicit val lsArb: Arbitrary[LineString] = Arbitrary(lsGen)

  "Polyline encoding & decoding" should "be the identity" in {
    forAll(minSuccessful(500)) { ls: LineString =>
      polyline.decode(polyline.encode(ls)).success.value == ls
    }
  }

}
