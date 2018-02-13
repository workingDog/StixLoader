package stix.support

import java.io.File
import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.kodekutters.stix.{SDO, SRO, StixObj}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.libs.ws.JsonBodyReadables._

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{DirectoryChooser, FileChooser, Stage}


object CyberUtils {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // check that the url is valid
  def urlValid(url: String): Boolean = {
    try {
      val checkUrl: URL = new URL(url) // this checks for the protocol
      checkUrl.toURI() // does the extra checking required for validation of URI
      true
    } catch {
      case x: Throwable => false
    }
  }

  def fileSelector(filter: Seq[String] = Seq("*.*", "*.zip")): Option[File] = {
    val fileChooser = new FileChooser {
      extensionFilters.add(new ExtensionFilter("bundle", filter))
    }
    Option(fileChooser.showOpenDialog(new Stage()))
  }

  def fileSaver(): Option[File] = Option(new FileChooser().showSaveDialog(new Stage()))

  def directorySelector(): Option[File] = Option(new DirectoryChooser().showDialog(new Stage()))

  /**
    * fetch json data from a network feed
    *
    * @param thePath the full url of the data to load
    * @return a Future[JsValue]
    */
  def getDataFrom(thePath: String): Future[JsValue] = {
    val wsClient = StandaloneAhcWSClient()
    wsClient.url(thePath).get().map { response =>
      response.status match {
        case 200 => response.body[JsValue]
        case x => println("----> response.status: " + x); JsNull
      }
    }.recover({
      case e: Exception => println("could not connect to: " + thePath); JsNull
    })
  }

  class Counter() {
    val count = mutable.Map("SDO" -> 0, "SRO" -> 0, "StixObj" -> 0)

    def resetCount(): Unit = count.foreach({ case (k, v) => count(k) = 0 })

    def inc(k: String): Unit = count(k) = count(k) + 1

    def countStix(stix: StixObj): Unit = {
      stix match {
        case x: SDO => inc("SDO")
        case x: SRO => inc("SRO")
        case x: StixObj => inc("StixObj")
      }
    }
  }

}
