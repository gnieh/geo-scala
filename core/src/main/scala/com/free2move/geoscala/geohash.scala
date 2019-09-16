package com.free2move.geoscala

import scala.util.Try

object geohash {

  private[geoscala] val base32: Array[Char] = "0123456789bcdefghjkmnpqrstuvwxyz".toCharArray

  private[geoscala] val deBase32: Array[Int] = Array.tabulate(128)(idx => base32.indexOf(idx.toChar))

  private[geoscala] val widthLookup: Array[Double] = Array.tabulate(13) { precision =>
    val bits = precision * 5
    360D / (1 << ((bits + 1) / 2))
  }

  private[geoscala] val heightLookup: Array[Double] = Array.tabulate(13) { precision =>
    val bits = precision * 5
    180D / (1 << (bits / 2))
  }

  def hash(lon: Double, lat: Double, precision: Int): Long = {
    val bits = precision * 5
    val multiplier = 1 << ((bits + 1) / 2)
    val lonFac = (lon + 180D) / 360D
    val latFac = (lat + 90D) / 180D
    val lonBits = (lonFac * multiplier).toInt - lonFac.toInt
    val latBits = (latFac * multiplier).toInt - latFac.toInt
    combineBits(lonBits, latBits) >>> (precision % 2)
  }

  def encode(hash: Long, precision: Int): String = {
    val chars = Array.ofDim[Char](precision)
    var restHash = hash
    var i = 0
    while(i < precision) {
      chars.update(precision - 1 - i, base32(restHash.toInt & 0x1F))
      restHash >>>= 5
      i += 1
    }
    new String(chars)
  }

  def apply(point: Point, precision: Int): String = {
    encode(hash(point.coordinates.longitude, point.coordinates.latitude, precision), precision)
  }

  def apply(lon: Double, lat: Double, precision: Int): String = {
    encode(hash(lon, lat, precision), precision)
  }

  def unapply(hash: String): Option[Point] = Try(decode(hash)).toOption

  def decode(geohash: String): Point = {
    val (lon, lat) = decodeLonLat(geohash)
    Point(Coordinate(lon, lat))
  }

  def decodeLonLat(geohash: String): (Double, Double) = {
    var hash = 0L
    var i = 0
    while(i < geohash.length) {
      hash = (hash << 5) | deBase32(geohash(i))
      i += 1
    }

    val bits = geohash.length * 5
    val (lonBits, latBits) = splitBits(hash << (bits % 2))
    val multiplier: Double = 1 << ((bits + 1) / 2)
    val lon = lonBits / multiplier * 360D - 180
    val lat = latBits / multiplier * 180D - 90
    (lon, lat)
  }

  def splitBits(hash: Long): (Int, Int) = {
    @inline
    def extractOdd(hash: Long): Int = {
      var x = hash
      x = x & 0x5555555555555555L //restrict to odd bits.

      x = (x | (x >>> 1)) & 0x3333333333333333L
      x = (x | (x >>> 2)) & 0x0f0f0f0f0f0f0f0fL
      x = (x | (x >>> 4)) & 0x00ff00ff00ff00ffL
      x = (x | (x >>> 8)) & 0x0000ffff0000ffffL
      x = (x | (x >>> 16)) & 0x00000000ffffffffL
      x.toInt
    }

    (extractOdd(hash >>> 1), extractOdd(hash))
  }

  def combineBits(lon: Int, lat: Int): Long = {
    @inline
    def interleaveWithZeros(w: Long): Long = {
      var word = w
      word = (word ^ (word << 16)) & 0x0000ffff0000ffffL
      word = (word ^ (word << 8)) & 0x00ff00ff00ff00ffL
      word = (word ^ (word << 4)) & 0x0f0f0f0f0f0f0f0fL
      word = (word ^ (word << 2)) & 0x3333333333333333L
      (word ^ (word << 1)) & 0x5555555555555555L
    }

    interleaveWithZeros(lat) | (interleaveWithZeros(lon) << 1)
  }

  def determineCenter(hash: String): Point = {
    val (lon, lat) = decodeLonLat(hash)
    val lonDelta = widthLookup(hash.length)
    val latDelta = heightLookup(hash.length)
    Point(Coordinate(lon + lonDelta / 2, lat + latDelta / 2))
  }

  def determineBounds(hash: String): Bounds = {
    val (lon, lat) = decodeLonLat(hash)
    val lonDelta = widthLookup(hash.length)
    val latDelta = heightLookup(hash.length)
    Bounds(minLon = lon, minLat = lat, maxLon = lon + lonDelta, maxLat = lat + latDelta)
  }

  def coverBounds(boundingBox: Bounds, precision: Int): Array[String] = {
    coverBounds(
      minLat = boundingBox.southwest.coordinates.latitude,
      minLon = boundingBox.southwest.coordinates.longitude,
      maxLat = boundingBox.northeast.coordinates.latitude,
      maxLon = boundingBox.northeast.coordinates.longitude,
      precision = precision
    )
  }

  def coverBounds(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double, precision: Int): Array[String] = {
    assert(maxLat >= minLat, "Invalid params: maxLat must be >= minLat")
    assert(maxLon >= minLon, "Invalid params: maxLon must be >= minLon")

    val min = hash(minLon, minLat, precision)
    val max = hash(maxLon, maxLat, precision)

    val (minLonBits, minLatBits) = splitBits(min)
    val (maxLonBits, maxLatBits) = splitBits(max)

    val latDiff = maxLatBits - minLatBits + 1
    val lonDiff = maxLonBits - minLonBits + 1

    val amount = latDiff.toLong * lonDiff.toLong
    assert(amount < Int.MaxValue, s"Max array size exceeded ($amount): choose a smaller bounding box or smaller precision")

    val result = Array.ofDim[String](amount.toInt)
    var lat = minLatBits
    var idx = 0
    while(lat <= maxLatBits) {
      var lon = minLonBits
      while(lon <= maxLonBits) {
        result(idx) = encode(combineBits(lon, lat), precision)
        idx += 1
        lon += 1
      }
      lat += 1
    }
    result
  }

}
