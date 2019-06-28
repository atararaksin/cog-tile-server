package cogtileserver

import geotrellis.contrib.vlm.gdal.{GDALRasterSource, GDALWarpOptions}
import geotrellis.proj4.WebMercator
import geotrellis.spark.SpatialKey
import geotrellis.spark.tiling.ZoomedLayoutScheme
import geotrellis.raster.resample.Bilinear

object CogService {

  def getTile(x: Int, y: Int, zoom: Int, uri: String): Option[Array[Byte]] = {
    val layoutScheme = ZoomedLayoutScheme(WebMercator)
    val layout = layoutScheme.levelForZoom(zoom).layout

    GDALRasterSource(uri)
      .reproject(WebMercator)
      .tileToLayout(layout, Bilinear)
      .read(SpatialKey(x, y))
      .map(_.renderPng().bytes)
  }

  def getBounds(uri: String): String = {

    val rasterSource = GDALRasterSource(uri)
    val extent = rasterSource.extent.extent.reproject(rasterSource.crs, WebMercator)

    "{\"bounds\"" + s": [${extent.xmin}, ${extent.ymin}, ${extent.xmax}, ${extent.ymax}]}"
  }
}