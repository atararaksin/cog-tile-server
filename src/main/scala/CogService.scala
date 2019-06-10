package cogtileserver

import geotrellis.proj4.WebMercator
import geotrellis.raster.crop.Crop
import geotrellis.raster.io.geotiff.{Auto, GeoTiff, MultibandGeoTiff}
import geotrellis.raster.{MultibandTile, RasterExtent}
import geotrellis.spark.SpatialKey
import geotrellis.spark.tiling.ZoomedLayoutScheme
import geotrellis.util.{FileRangeReader, StreamingByteReader}
import geotrellis.vector.Extent
import geotrellis.raster.reproject.Reproject

object CogService {

  def getTile(x: Int, y: Int, zoom: Int, uri: String): Option[Array[Byte]] = {
    val layoutScheme = ZoomedLayoutScheme(WebMercator)
    val layout = layoutScheme.levelForZoom(zoom).layout

    val geoTiff = CachingGeoTiffReader[MultibandTile].cacheRead(uri)

    val keyExtent: Extent = layout.mapTransform(SpatialKey(x, y))
    val reprojectedKeyExtent = keyExtent.reproject(layoutScheme.crs, geoTiff.crs)

    def extractTile(extent: Extent, tiff: GeoTiff[MultibandTile]) = tiff
      .getClosestOverview(layout.cellSize, Auto(0))
      .crop(extent, Crop.Options(clamp = false))
      .raster
      .reproject(geoTiff.crs, layoutScheme.crs, Reproject.Options(targetCellSize = Some(layout.cellSize)))
      .resample(RasterExtent(keyExtent, layoutScheme.tileSize, layoutScheme.tileSize))
      .tile

    geoTiff
      .extent
      .intersection(reprojectedKeyExtent)
      .map(extent => extractTile(extent, geoTiff).renderPng().bytes)
  }

  def getBounds(uri: String): String = {

    val geotiff = MultibandGeoTiff.streaming(StreamingByteReader(FileRangeReader(uri)))
    val extent = geotiff.extent.reproject(geotiff.crs, WebMercator)

    "{\"bounds\"" + s": [${extent.xmin}, ${extent.ymin}, ${extent.xmax}, ${extent.ymax}]}"
  }
}