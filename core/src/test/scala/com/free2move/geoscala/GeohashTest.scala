package com.free2move.geoscala

import com.github.davidmoten.geo.{GeoHash => GeoHashLib}
import org.scalacheck.{Gen, Prop}
import org.scalatest.PropSpec
import org.scalatest.check.Checkers

class GeohashTest extends PropSpec with Checkers {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 1000)

    import geohash._

    property("combine(split) == identity") {
      check(Prop.forAll(Gen.chooseNum[Long](0, 0x0FFFFFFFFFFFFFFFL)) { l: Long =>
        (combineBits _).tupled(splitBits(l)) == l
      })
    }

    val intPairGen: Gen[(Int, Int)] = for {
      l1 <- Gen.chooseNum[Int](0, 0x0FFFFFFF)
      l2 <- Gen.chooseNum[Int](0, 0x0FFFFFFF)
    } yield (l1, l2)

    property("split(combine) == identity"){
      check(Prop.forAll(intPairGen) { case tup@(l1, l2) =>
        splitBits(combineBits(l1, l2)) == tup
      })
    }

    val coordinatesGen: Gen[(Double, Double)] = for {
      lon <- Gen.chooseNum(-180D, +180D)
      lat <- Gen.chooseNum(-90D, +90D)
    } yield (lon, lat)

    property("decode(encode) == identity") {
      check(Prop.forAllNoShrink(coordinatesGen) { case tup@(lon, lat) =>
        Prop.forAllNoShrink(Gen.choose(1, 12)) { precision =>
          val decoded = decodeLonLat(apply(lon, lat, precision))
          checkRoughEquality(decoded, tup, precision)
        }
      })
    }

    property("apply(decode) == identity") {
      check(Prop.forAllNoShrink(Gen.choose[Int](1, 12)) { precision =>
        Prop.forAllNoShrink(Gen.pick(precision, base32.toSeq)) { hashSeq =>
          val hash = hashSeq.mkString
          val (lon, lat) = decodeLonLat(hash)
          apply(lon, lat, hash.length) == hash
        }
      })
    }

    property("apply() must equal external implementation") {
      check(Prop.forAllNoShrink(coordinatesGen) { case (lon, lat) =>
        Prop.forAllNoShrink(Gen.chooseNum(1, 12)) { precision =>
          apply(lon, lat, precision) == GeoHashLib.encodeHash(lat, lon, precision)
        }
      })
    }

    property("determineBounds() must return a bounding box covering (at least) all child hashes") {
      check(Prop.forAllNoShrink(coordinatesGen) { case (lon, lat) =>
        Prop.forAllNoShrink(Gen.chooseNum(1, 11)) { precision =>
          val hash = apply(lon, lat, precision)
          val bbox = determineBounds(hash)
          val hashes = coverBounds(bbox, precision + 1)
          hashes.count(_ startsWith hash) == 32
        }
      })
    }

    // This test takes quite some time
    /*property("coverGeoHashes() must equal external implementation") {
      check(Prop.forAllNoShrink(coordinatesGen) { case (lon, lat) =>
        Prop.forAllNoShrink(Gen.chooseNum(1, 12)) { precision =>
          Prop.forAllNoShrink(Gen.chooseNum(0.0, 10.0 / precision).suchThat(_ + lat <= 90), Gen.chooseNum(0.0, 10.0 / precision).suchThat(_ + lon <= 180)) { case (latOff, lonOff) =>
            import scala.collection.JavaConverters._
            val ownHashes = GeoHash.coverBounds(Bounds(lon, lat, lon + lonOff, lat + latOff), precision)
            val libHashes = GeoHashLib.coverBoundingBox(lat + latOff, lon, lat, lon + lonOff, precision).getHashes
            ownHashes.toSet == libHashes.asScala.toSet
          }
        }
      })
    }*/

    def checkRoughEquality(l: (Double, Double), r: (Double, Double), precision: Int): Boolean = {
      math.abs(l._1 - r._1) <= (180D / (2 << precision)) &&
        math.abs(l._2 - r._2) <= (360D / (2 << precision))
    }

}
