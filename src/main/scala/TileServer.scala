package cogtileserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import akka.stream.ActorMaterializer

import scala.util.{Failure, Success}


object TileServer extends App {

  implicit val system = ActorSystem("cog-tile-server")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route = cors() {
    get {
      rejectEmptyResponse {
        pathPrefix("tiles" / IntNumber / IntNumber / IntNumber) { (x, y, z) =>
          pathSuffix(".png") {
            parameter("uri") { uri =>
              pathEnd {
                val png = CogService.getTile(x, y, z, uri)
                complete(png.map(HttpEntity(ContentType(MediaTypes.`image/png`), _)))
              }
            }
          }
        } ~
        pathPrefix("bounds") {
          parameter("uri") { uri =>
            pathEnd {
              complete(HttpEntity(CogService.getBounds(uri)))
            }
          }
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  bindingFuture.onComplete {
    case Success(serverBinding) => println(s"Listening to ${serverBinding.localAddress}")
    case Failure(error) => println(s"Error: ${error.getMessage}")
  }
}
