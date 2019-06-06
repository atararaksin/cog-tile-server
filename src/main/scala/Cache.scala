package cogtileserver

import java.util

import geotrellis.raster.CellGrid
import geotrellis.raster.io.geotiff.GeoTiff

case class Cache[V <: CellGrid] (val maxSize: Int) extends util.LinkedHashMap[String, GeoTiff[V]] {
  override def removeEldestEntry(eldest: util.Map.Entry[String, GeoTiff[V]]) = size() > maxSize

  def getOrElseUpdate(uri: String, createFunc: () => GeoTiff[V]): GeoTiff[V] = {
    get(uri) match {
      case g: GeoTiff[V] => g
      case _ => val g = createFunc(); put(uri, g); g
    }
  }
}
