name := "cog-tile-server"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "ch.megard" %% "akka-http-cors" % "0.4.0",
  "org.locationtech.geotrellis" %% "geotrellis-raster" % "2.3.1",
  "org.locationtech.geotrellis" %% "geotrellis-spark" % "2.3.1"
)