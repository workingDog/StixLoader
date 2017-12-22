package stix.support

import java.io.File
import java.net.URL

import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{DirectoryChooser, FileChooser, Stage}


object CyberUtils {

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

  def fileSaver(): Option[File] = {
    val fileChooser = new FileChooser()
    Option(fileChooser.showSaveDialog(new Stage()))
  }

  def directorySelector(): Option[File] = {
    Option(new DirectoryChooser().showDialog(new Stage()))
  }

}
