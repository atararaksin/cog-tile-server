package cogtileserver

import geotrellis.raster.{CellGrid, MultibandTile, Tile}
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import geotrellis.util.{ByteReader, FileRangeReader, StreamingByteReader}

object CachingGeoTiffReader {
  private val maxCacheSize = 100

  private implicit val singlebandCache = new ThreadLocal[Cache[Tile]]()
  private implicit val multibandCache = new ThreadLocal[Cache[MultibandTile]]()

  implicit def getByteReader(uri: String) = StreamingByteReader(FileRangeReader(uri))

  private def getLocalCache[V <: CellGrid](implicit cache: ThreadLocal[Cache[V]]): Cache[V] = {
    cache.get match {
      case c: Cache[V] => c
      case _ => val lc = Cache[V](maxCacheSize); cache.set(lc); lc
    }
  }

  implicit val cachingSinglebandReader: CachingGeoTiffReader[Tile] = new CachingGeoTiffReader[Tile] {
    def cacheRead(uri: String): GeoTiff[Tile] = {
      getLocalCache[Tile].getOrElseUpdate(uri, () => read(uri, true))
    }

    def read(byteReader: ByteReader, streaming: Boolean): GeoTiff[Tile] = {
      GeoTiffReader.readSingleband(byteReader, streaming)
    }
  }

  implicit val cachingMultibandReader: CachingGeoTiffReader[MultibandTile] = new CachingGeoTiffReader[MultibandTile] {
    def cacheRead(uri: String): GeoTiff[MultibandTile] = {
      getLocalCache[MultibandTile].getOrElseUpdate(uri, () => read(uri, true))
    }

    def read(byteReader: ByteReader, streaming: Boolean): GeoTiff[MultibandTile] = {
      GeoTiffReader.readMultiband(byteReader, streaming)
    }
  }

  def apply[V <: CellGrid](implicit ev: CachingGeoTiffReader[V]): CachingGeoTiffReader[V] = ev
}

trait CachingGeoTiffReader[V <: CellGrid] extends GeoTiffReader[V] {
  def cacheRead(uri: String): GeoTiff[V]
}