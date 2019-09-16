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
import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PolylineTest extends AnyFlatSpec with Matchers with OptionValues with TryValues with ScalaCheckDrivenPropertyChecks {

  "The polyline encoding" should "be correct for the Google sample" in {
    val example = LineString(List(Coordinate(longitude = -120.2, latitude = 38.5), Coordinate(longitude = -120.95, latitude = 40.7), Coordinate(longitude = -126.453, latitude = 43.252)))
    polyline.encode(example) shouldBe "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
  }

  "The polyline decoding" should "be correct for the Google sample" in {
    val example = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
    val expected = LineString(List(Coordinate(longitude = -120.2, latitude = 38.5), Coordinate(longitude = -120.95, latitude = 40.7), Coordinate(longitude = -126.453, latitude = 43.252)))
    val result = polyline.decode(example)
    result.success.value shouldBe expected
  }

  it should "handle an example trip through Warsaw" in {
    val poly =
      "q_|}Hu|f_C_@O?A@CW~@NxAMv@[r@e@r@e@n@Uh@En@CbAUv@Ut@_@b@]b@a@f@c@j@En@RzA^pAh@fAl@rBThAPz@RbAZ|ALn@Nz@Jf@^pAZlAf@pBTfBLbCFpAFfBDnAF|AF|AFbBHpBFvAHlBFpAHnBJjBDl@RzB"
    val decoded = polyline.decode(poly).success.value
    decoded.coordinates.length shouldBe 50
    polyline.encode(decoded) shouldBe poly
  }

  it should "handle cases where coordinate delta is slightly negative (< 10^-5)" in {
    val ls = LineString(List(Coordinate(13.39336395263672,52.52311483252328), Coordinate(13.39332,52.52311)))
    val encoded = polyline.encode(ls)
    val decoded = polyline.decode(encoded).success.value
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.00001) // 10^-5
    decoded.coordinates.head.latitude should ===(ls.coordinates.head.latitude)
    decoded.coordinates.head.longitude should ===(ls.coordinates.head.longitude)
    decoded.coordinates.tail.head.latitude should ===(ls.coordinates.tail.head.latitude)
    decoded.coordinates.tail.head.longitude should ===(ls.coordinates.tail.head.longitude)
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
