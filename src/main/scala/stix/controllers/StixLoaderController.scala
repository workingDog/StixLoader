package stix.controllers

import java.io.File
import javafx.fxml.FXML

import com.jfoenix.controls._
import com.kodekutters.stix.{Bundle, Timestamp}
import play.api.libs.json.Json
import stix.StixLoaderApp
import stix.db.elastic.ElasticStix
import stix.db.mongo.MongoDbStix
import stix.info._
import stix.loaders.{FileLoader, MongoLoader}
import stix.support.ButtonGroup
import stix.support.CyberUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafxml.core.macros.{nested, sfxml}


trait StixLoaderControllerInterface {

  def init(): Unit

  def messageBar(): Label

  def showThis(text: String, color: Color): Unit

  def showSpinner(onof: Boolean): Unit
}

@sfxml
class StixLoaderController(aboutItem: MenuItem,
                           quitItem: MenuItem,
                           infoLabel: Label,
                           @FXML mainTabPane: JFXTabPane,
                           @FXML theSpinner: JFXSpinner,
                           @FXML fromFileButton: JFXButton,
                           @FXML fromMongoButton: JFXButton,
                           @FXML fromPostgresButton: JFXButton,
                           @FXML fromNeo4jButton: JFXButton,
                           @FXML fromESButton: JFXButton,
                           @FXML toFileButton: JFXButton,
                           @FXML toMongoButton: JFXButton,
                           @FXML toNeo4jButton: JFXButton,
                           @FXML toPostgresButton: JFXButton,
                           @FXML toESButton: JFXButton,
                           @FXML convertButton: JFXButton,
                           @FXML infoArea: JFXTextArea,
                           @FXML settingsArea: JFXTextArea) extends StixLoaderControllerInterface {

  def messageBar(): Label = infoLabel

  private def self = this.asInstanceOf[StixLoaderControllerInterface]

  private val fromGroup = new ButtonGroup(true)
  private val toGroup = new ButtonGroup(false)

  def init() {
    showSpinner(false)
    fromGroup.add(fromFileButton)
    fromGroup.add(fromMongoButton)
    fromGroup.add(fromNeo4jButton)
    fromGroup.add(fromPostgresButton)
    fromGroup.add(fromESButton)
    toGroup.add(toFileButton)
    toGroup.add(toMongoButton)
    toGroup.add(toNeo4jButton)
    toGroup.add(toPostgresButton)
    toGroup.add(toESButton)
    infoArea.appendText("Session starting at: " + Timestamp.now().toString())
  }

  def showThis(text: String, color: Color): Unit = Platform.runLater(() => {
    messageBar().setTextFill(color)
    messageBar().setText(text)
    infoArea.appendText("\n" + text)
  })

  def clearMessage(): Unit = Platform.runLater(() => {
    messageBar().setText("")
  })

  def showSpinner(onof: Boolean) = Platform.runLater(() => {
    theSpinner.setVisible(onof)
  })

  def quitAction() = StixLoaderApp.stopApp()

  def aboutAction() {
    new Alert(AlertType.Information) {
      initOwner(this.owner)
      title = "StixLoader-" + StixLoaderApp.version
      contentText = "R. Wathelet"
      headerText = "StixLoader is a tool to load STIX-2 objects \nfrom a source to a destination storage system."
    }.showAndWait()
  }

  def disableInToGroup(thisButton: JFXButton): Unit = {
    toGroup.entryList.foreach(e => if (e.b == thisButton && !thisButton.isDisable) e.b.setDisable(true) else e.b.setDisable(false))
  }

  def disableInFromGroup(thisButton: JFXButton): Unit = {
    fromGroup.entryList.foreach(e => if (e.b == thisButton && !thisButton.isDisable) e.b.setDisable(true) else e.b.setDisable(false))
  }

  def fromFileAction(): Unit = {
    clearMessage()
    disableInToGroup(toFileButton)
    fromGroup.clearAllSelection()
    fileSelector() match {
      case None => toGroup.entryList.foreach(e => e.b.setDisable(false))
      case Some(file) =>
        fromGroup.setSelected(fromFileButton, FileInfo(file))
        showThis("From file: " + file.getName(), Color.Black)
    }
  }

  def fromMongoAction(): Unit = {
    clearMessage()
    disableInToGroup(toMongoButton)
    showSpinner(true)
    fromGroup.clearAllSelection()
    // try to connect to the mongo db
    Future(try {
      showThis("Trying to connect to MongoDB: " + MongoDbStix.dbUri, Color.Black)
      // start a mongoDB connection, if not already connected
      // will wait here for the connection to complete or throw an exception
      if (!MongoDbStix.isConnected) MongoDbStix.init()
      fromGroup.setSelected(fromMongoButton, MongoInfo(MongoDbStix.dbUri))
      showThis("Ok connected to MongoDB: " + MongoDbStix.dbUri, Color.Black)
    } catch {
      case ex: Throwable =>
        fromGroup.clearAllSelection()
        disableInToGroup(toMongoButton)
        showThis("Fail to connect to MongoDB: " + MongoDbStix.dbUri, Color.Red)
    } finally {
      showSpinner(false)
    })
  }

  def fromPostgresAction(): Unit = {
    clearMessage()
    disableInToGroup(toPostgresButton)
    fromGroup.setSelected(fromPostgresButton, NoInfo())
    if (fromGroup.isSelected(fromPostgresButton)) showThis("From PostgreSQL not yet implemented", Color.Red)
    println("---> fromPostgresAction")
  }

  def fromNeo4jAction(): Unit = {
    clearMessage()
    disableInToGroup(toNeo4jButton)
    fromGroup.setSelected(fromNeo4jButton, NoInfo())
    if (fromGroup.isSelected(fromNeo4jButton)) showThis("From Neo4j not yet implemented", Color.Red)
    println("---> fromNeo4jAction")
  }

  def toFileAction(): Unit = {
    clearMessage()
    disableInFromGroup(fromFileButton)
    toGroup.clearAllSelection()
    fileSaver() match {
      case None => fromGroup.entryList.foreach(e => e.b.setDisable(false))
      case Some(file) =>
        toGroup.setSelected(toFileButton, FileInfo(file))
        showThis("To file: " + file.getName(), Color.Black)
    }
  }

  def toMongoAction(): Unit = {
    clearMessage()
    disableInFromGroup(fromMongoButton)
    showSpinner(true)
    toGroup.clearAllSelection()
    // try to connect to the mongo db
    Future(try {
      showThis("Trying to connect to MongoDB: " + MongoDbStix.dbUri, Color.Black)
      // start a mongoDB connection, if not already connected
      // will wait here for the connection to complete or throw an exception
      if (!MongoDbStix.isConnected) {
        // try to connect
        MongoDbStix.init()
        // if could connect
        if (MongoDbStix.isConnected) {
          toGroup.setSelected(toMongoButton, MongoInfo(MongoDbStix.dbUri))
          showThis("Ok connected to MongoDB: " + MongoDbStix.dbUri, Color.Black)
        } else {
          showThis("Fail to connect to MongoDB: " + MongoDbStix.dbUri, Color.Red)
        }
      } else {
        // already connected
        toGroup.setSelected(toMongoButton, MongoInfo(MongoDbStix.dbUri))
        showThis("Ok connected to MongoDB: " + MongoDbStix.dbUri, Color.Black)
      }
    } catch {
      case ex: Throwable =>
        toGroup.clearAllSelection()
        disableInFromGroup(fromMongoButton)
        showThis("Fail to connect to MongoDB: " + MongoDbStix.dbUri, Color.Red)
    } finally {
      showSpinner(false)
    })
  }

  def toNeo4jAction(): Unit = {
    clearMessage()
    disableInFromGroup(fromNeo4jButton)
    toGroup.clearAllSelection()
    directorySelector() match {
      case None => fromGroup.entryList.foreach(e => e.b.setDisable(false))
      case Some(dir) =>
        toGroup.setSelected(toNeo4jButton, NeoInfo(dir))
        showThis("To Neo4j directory: " + dir.getName(), Color.Black)
    }
  }

  def toPostgresAction(): Unit = {
    clearMessage()
    disableInFromGroup(fromPostgresButton)
    toGroup.setSelected(toPostgresButton, NoInfo())
    if (toGroup.isSelected(toPostgresButton)) showThis("To PostgreSQL not yet implemented", Color.Red)
    println("---> toPostgresAction")
  }

  // todo load a bundle from a network feed
//  def loadNetBundle(thePath: String) {
//    showThis("Loading bundle from: " + thePath, Color.Black)
//    showSpinner(true)
//    // try to load the data
//    try {
//      // request the data
//      getDataFrom(thePath).map(jsData => {
//        // create a bundle object from it
//        Json.fromJson[Bundle](jsData).asOpt match {
//          case Some(bundle) =>
//            showThis("Bundle loaded from: " + thePath, Color.Black)
//
//          case None => showThis("Fail to load bundle from: " + thePath, Color.Red)
//        }
//      })
//    } catch {
//      case ex: Throwable => showThis("Fail to load bundle from: " + thePath, Color.Red)
//    } finally {
//      showSpinner(false)
//    }
//  }

  def fromESAction(): Unit = {
    clearMessage()
    disableInFromGroup(toESButton)
    fromGroup.setSelected(fromESButton, NoInfo())
    if (fromGroup.isSelected(fromESButton)) showThis("From Elasticsearch not yet implemented", Color.Red)
    println("---> fromESAction")
  }

  def toESAction(): Unit = {
    clearMessage()
    disableInFromGroup(fromESButton)
    showSpinner(true)
    toGroup.clearAllSelection()
    // try to connect to elasticsearch
    Future(try {
      // start a elasticsearch connection, if not already connected
      if (!ElasticStix.isConnected) {
        showThis("Trying to connect to Elasticsearch: " + ElasticStix.esName, Color.Black)
        // try to connect
        ElasticStix.init()
        // if could connect
        if (ElasticStix.isConnected) {
          toGroup.setSelected(toESButton, ESInfo(ElasticStix.esName))
          showThis("Ok connected to Elasticsearch: " + ElasticStix.esName, Color.Black)
        } else {
          showThis("Fail to connect to Elasticsearch: " + ElasticStix.esName, Color.Red)
        }
      } else {
        // already connected
        toGroup.setSelected(toESButton, ESInfo(ElasticStix.esName))
        showThis("Ok connected to Elasticsearch: " + ElasticStix.esName, Color.Black)
      }
    } catch {
      case ex: Throwable =>
        toGroup.clearAllSelection()
        disableInFromGroup(fromESButton)
        showThis("Fail to connect to Elasticsearch: " + ElasticStix.esName, Color.Red)
    } finally {
      showSpinner(false)
    })
  }

  def loadAction(): Unit = {
    clearMessage()
    toGroup.getSelected().map(toSelection => {
      val destination = toSelection.getUserData.asInstanceOf[InfoMessage]
      fromGroup.getSelected().map(fromSelection => {
        fromSelection.getUserData match {
          case fromFile: FileInfo => FileLoader.load(fromFile, destination, self)
          case fromMongo: MongoInfo => MongoLoader.load(fromMongo, destination, self)
          case x => showThis("Loading from this not yet implemented", Color.Red)
        }
      })
    })
    fromGroup.clearAllSelection()
    toGroup.clearAllSelection()
    fromGroup.entryList.foreach(e => e.b.setDisable(false))
    toGroup.entryList.foreach(e => e.b.setDisable(false))
  }

}
