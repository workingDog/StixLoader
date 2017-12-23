package stix.controllers

import java.io.File
import javafx.fxml.FXML

import com.jfoenix.controls._
import com.kodekutters.stix.Timestamp
import stix.StixLoaderApp
import stix.db.mongo.MongoDbStix
import stix.info.{FileInfo, MongoInfo, NeoInfo}
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

  def doClose(): Unit

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
                           @FXML toFileButton: JFXButton,
                           @FXML toMongoButton: JFXButton,
                           @FXML toNeo4jButton: JFXButton,
                           @FXML toPostgresButton: JFXButton,
                           @FXML convertButton: JFXButton,
                           @FXML infoArea: JFXTextArea,
                           @FXML settingsArea: JFXTextArea) extends StixLoaderControllerInterface {

  def messageBar(): Label = infoLabel

  private def self = this.asInstanceOf[StixLoaderControllerInterface]

  private val fromGroup = new ButtonGroup(true)
  private val toGroup = new ButtonGroup(false)

  // todo does not work
  private val tabPaneStyle = ".jfx-tab-pane .tab-selected-line { -fx-background-color: red; }"

  def init() {
    mainTabPane.setStyle(tabPaneStyle)
    fromGroup.add(fromFileButton)
    fromGroup.add(fromMongoButton)
    fromGroup.add(fromNeo4jButton)
    fromGroup.add(fromPostgresButton)
    toGroup.add(toFileButton)
    toGroup.add(toMongoButton)
    toGroup.add(toNeo4jButton)
    toGroup.add(toPostgresButton)
    showSpinner(false)
    infoArea.appendText("Session starting at: " + Timestamp.now().toString())
  }

  def showThis(text: String, color: Color): Unit = Platform.runLater(() => {
    messageBar().setTextFill(color)
    messageBar().setText(text)
    infoArea.appendText("\n" + text)
  })

  def clearMessage(): Unit = Platform.runLater(() => { messageBar().setText("") })

  def showSpinner(onof: Boolean) = Platform.runLater(() => { theSpinner.setVisible(onof) })

  // todo close properly before exiting
  def doClose(): Unit = {}

  def quitAction() = StixLoaderApp.stopApp()

  def aboutAction() {
    new Alert(AlertType.Information) {
      initOwner(this.owner)
      title = "StixLoader-" + StixLoaderApp.version
      contentText = "R. Wathelet"
      headerText = "StixLoader is a tool to load STIX-2 objects \nfrom a source to a destination storage system."
    }.showAndWait()
  }

  def setDisableToGroup(thisButton: JFXButton): Unit = {
    toGroup.entryList.foreach(e => if (e.b == thisButton && !thisButton.isDisable) e.b.setDisable(true) else e.b.setDisable(false))
  }

  def setDisableFromGroup(thisButton: JFXButton): Unit = {
    fromGroup.entryList.foreach(e => if (e.b == thisButton && !thisButton.isDisable) e.b.setDisable(true) else e.b.setDisable(false))
  }

  def fromFileAction(): Unit = {
    clearMessage()
    setDisableToGroup(toFileButton)
    fromGroup.clearAllSelection()
    fileSelector() match {
      case None => toGroup.entryList.foreach(e => e.b.setDisable(false))
      case Some(file) =>
        fromGroup.setSelected(fromFileButton, file)
        showThis("From file: " + file.getName(), Color.Black)
    }
  }

  def fromMongoAction(): Unit = {
    clearMessage()
    setDisableToGroup(toMongoButton)
    showSpinner(true)
    fromGroup.clearAllSelection()
    // try to connect to the mongo db
    Future(try {
      showThis("Trying to connect to MongoDB: " + MongoDbStix.dbUri, Color.Black)
      // start a mongoDB connection, if not already connected
      // will wait here for the connection to complete or throw an exception
      if (!MongoDbStix.isConnected()) MongoDbStix.init()
      fromGroup.setSelected(fromMongoButton, MongoInfo(null))
      showThis("Ok connected to MongoDB: " + MongoDbStix.dbUri, Color.Black)
    } catch {
      case ex: Throwable =>
        fromGroup.clearAllSelection()
        setDisableToGroup(toMongoButton)
        showThis("Fail to connect to MongoDB: " + MongoDbStix.dbUri, Color.Red)
    } finally {
      showSpinner(false)
    })
  }

  def fromPostgresAction(): Unit = {
    clearMessage()
    setDisableToGroup(toPostgresButton)
    fromGroup.setSelected(fromPostgresButton, null)
    if(fromGroup.isSelected(fromPostgresButton)) showThis("From PostgreSQL not yet implemented", Color.Red)
    println("---> fromPostgresAction")
  }

  def fromNeo4jAction(): Unit = {
    clearMessage()
    setDisableToGroup(toNeo4jButton)
    fromGroup.setSelected(fromNeo4jButton, null)
    if(fromGroup.isSelected(fromNeo4jButton)) showThis("From Neo4j not yet implemented", Color.Red)
    println("---> fromNeo4jAction")
  }

  def toFileAction(): Unit = {
    clearMessage()
    setDisableFromGroup(fromFileButton)
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
    setDisableFromGroup(fromMongoButton)
    showSpinner(true)
    toGroup.clearAllSelection()
    toGroup.setSelected(toMongoButton, null)
    // try to connect to the mongo db
    Future(try {
      showThis("Trying to connect to MongoDB: " + MongoDbStix.dbUri, Color.Black)
      // start a mongoDB connection, if not already connected
      // will wait here for the connection to complete or throw an exception
      if (!MongoDbStix.isConnected()) MongoDbStix.init()
      toGroup.setSelected(toMongoButton, MongoInfo(null))
      showThis("Ok connected to MongoDB: " + MongoDbStix.dbUri, Color.Black)
    } catch {
      case ex: Throwable =>
        toGroup.clearAllSelection()
        setDisableFromGroup(fromMongoButton)
        showThis("Fail to connect to MongoDB: " + MongoDbStix.dbUri, Color.Red)
    } finally {
      showSpinner(false)
    })
  }

  def toNeo4jAction(): Unit = {
    clearMessage()
    setDisableFromGroup(fromNeo4jButton)
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
    setDisableFromGroup(fromPostgresButton)
    toGroup.setSelected(toPostgresButton, null)
    if(toGroup.isSelected(toPostgresButton)) showThis("To PostgreSQL not yet implemented", Color.Red)
    println("---> toPostgresAction")
  }

  def convertAction(): Unit = {
    clearMessage()
    toGroup.getSelected().map(toSelection => {
      fromGroup.getSelected().map(fromSelection => {
        fromSelection.getUserData match {
          case fromFile: File => FileLoader.load(fromFile, toSelection.getUserData, self)
          case fromMongo: MongoInfo => MongoLoader.load(fromMongo, toSelection.getUserData, self)
          case x => showThis("Conversion not yet implemented", Color.Red)
        }
      })
    })
    fromGroup.clearAllSelection()
    toGroup.clearAllSelection()
    fromGroup.entryList.foreach(e => e.b.setDisable(false))
    toGroup.entryList.foreach(e => e.b.setDisable(false))
  }

}
