package stix

import java.io.IOException
import java.security.Security
import javafx.{scene => jfxs}

import controllers.StixLoaderControllerInterface

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import scala.language.{implicitConversions, postfixOps}


/**
  * StixLoader Application
  *
  * @author Ringo Wathelet
  */
object StixLoaderApp extends JFXApp {

  val version = "1.0"
  // needed for (SSL) TLS-1.2 in https, requires jdk1.8.0_152
  Security.setProperty("crypto.policy", "unlimited")
  // create the application
  val resource = getClass.getResource("ui/stixLoader.fxml")
  if (resource == null) {
    throw new IOException("Cannot load resource: ui/stixLoader.fxml")
  }
  val loader = new FXMLLoader(resource, NoDependencyResolver)
  loader.load()
  val root: jfxs.Parent = loader.getRoot[jfxs.Parent]
  val controller = loader.getController[StixLoaderControllerInterface]
  stage = new PrimaryStage() {
    title = "StixLoader-" + version
    scene = new Scene(root)
    onCloseRequest = (e) => stopApp()
  }
  // initialise the main controller
  controller.init()

  // close properly before exiting
  override def stopApp(): Unit = {
    super.stopApp
    Platform.exit
    System.exit(0)
  }

}


