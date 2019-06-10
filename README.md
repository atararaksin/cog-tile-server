# COG tile server
A fast on-the-fly tile server built with `Geotrellis` and `akka-http`. Serves XYZ tyles from a one-piece Cloud Optimized GeoTiff.

Run the server with `sbt run`

Tile server endpoint will be available at http://localhost:8080/tiles/X/Y/Z.png?uri=/path/to/tiff
