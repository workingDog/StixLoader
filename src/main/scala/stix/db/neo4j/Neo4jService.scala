package stix.db.neo4j

import java.io.File

import com.kodekutters.neo4j.{Neo4jFileLoader, Neo4jLoader}
import com.kodekutters.stix.StixObj
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.slf4j.helpers.NOPLogger
import stix.controllers.StixLoaderControllerInterface

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalafx.scene.paint.Color


/**
  * the Neo4j graph database services
  * delegate all to stixtoneo4jlib
  */
object Neo4jService {

  val config: Config = ConfigFactory.load

 // implicit val logger = Logger("StixLoader") // Logger(NOPLogger.NOP_LOGGER)   //

  def saveFileToDB(file: File, dbDirectory: File, controller: StixLoaderControllerInterface): Unit = {
    controller.showSpinner(true)
    Future({
      controller.showThis("Saving: " + file.getName + " to Neo4jDB: " + dbDirectory.getName, Color.Black)
      val neoLoader = new Neo4jFileLoader(dbDirectory.getCanonicalPath)
      if (file.getName.toLowerCase.endsWith(".zip")) neoLoader.loadBundleZipFile(file)
      else neoLoader.loadBundleFile(file)
      controller.showThis("Done saving: " + file.getName + " to Neo4jDB: " + dbDirectory.getName, Color.Black)
      controller.showSpinner(false)
    })
  }

  def saveStixToNeo4j(stixList: List[StixObj], neoDir: File, controller: StixLoaderControllerInterface): Unit = {
    controller.showSpinner(true)
    Future({
      controller.showThis("Saving STIX-2 to Neo4jDB: " + neoDir.getName, Color.Black)
      val neoLoader = new Neo4jLoader(neoDir.getCanonicalPath)
      stixList.foreach(stix => neoLoader.loadIntoNeo4j(stix))
      neoLoader.close()
      controller.showThis("Done saving STIX-2 to Neo4jDB: " + neoDir.getName, Color.Black)
      controller.showSpinner(false)
    })
  }

}
