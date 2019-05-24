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

import scala.util.{Failure, Success, Try}

/**
  * Pair of functions to encode and decode [[LineString]] and their polyline representation, see
  * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
  */
object polyline {

  private val chunkMasks = List(0x1F, 0x3E0, 0x7C00, 0xF8000, 0x1F00000, 0x3E000000).zipWithIndex

  /**
    * Encode a [[LineString]] into its polyline representation.
    * @param line the linestring to encode
    * @return the polyline string
    */
  def encode(line: LineString): String = {
    line.coordinates
      .scanLeft(Coordinate(0, 0) -> Coordinate(0, 0)) {
        case ((old, _), curr) =>
          curr -> Coordinate(curr.longitude - old.longitude, curr.latitude - old.latitude)
      }
      .tail
      .map(_._2)
      .map(coord2Poly)
      .mkString
  }

  private def coord2Poly(coord: Coordinate): String = {
    num2Poly(coord.longitude) + num2Poly(coord.latitude)
  }

  private def num2Poly(num: Double): String = {
    val scaled = Math.round(num * 1e5).toInt
    val bits = if (num < 0.0) ~(scaled << 1) else scaled << 1
    chunkMasks
      .map {
        case (mask, idx) =>
          ((bits & mask) >>> (idx * 5), (bits >>> ((idx + 1) * 5)) > 0, idx == 0 && bits == 0)
      }
      .flatMap {
        case (n, more, force) =>
          val nx = if (more) n | 0x20 else n // signal that more chunks come
          Some((nx + 63).toChar).filter(_ => nx != 0 || force)
      }
      .mkString
  }

  /**
    * Attempt to decode a polyline string - fails if the input is not a valid polyline
    * @param in the polyline string
    * @return the decoded [[LineString]] or a describing `Failure`
    */
  def decode(in: String): Try[LineString] = {
    val chunks = chunk(in.map(_.toInt - 63).toList).tail
    if (chunks.length % 2 != 0) {
      Failure[LineString](new IllegalArgumentException(s"Polyline chunks have an uneven number: ${chunks.length}"))
    } else {
      Success(
        LineString(
          chunks
            .map(poly2Num)
            .grouped(2)
            .map {
              // it is safe to only match this case as it was checked above that the length is even
              // then every group has exactly 2 elements
              case List(lng, lat) => Coordinate(lat, lng)
            }
            .toList
            .scanRight(Coordinate(0.0, 0.0)) {
              case (old, delta) =>
                Coordinate(old.longitude + delta.longitude, old.latitude + delta.latitude)
            }
            .reverse
            .tail
        )
      )
    }
  }

  private def chunk(ints: List[Int]): List[List[Int]] = {
    ints.foldLeft(List.empty[Int] :: Nil) {
      case (acc :: chunks, curr) if (curr & 0x20) > 0 => ((curr & 0x1F) :: acc) :: chunks
      case (acc :: chunks, curr)                      => Nil :: ((curr & 0x1F) :: acc) :: chunks
    }
  }

  private def poly2Num(pieces: List[Int]): Double = {
    val num = pieces.reverse.zipWithIndex.foldLeft(0) {
      case (acc, (piece, idx)) =>
        acc | (piece << (idx * 5))
    }
    val num2 = if ((num & 1) != 0) ~num else num
    (num2 >> 1).toDouble / 1e5
  }

}
