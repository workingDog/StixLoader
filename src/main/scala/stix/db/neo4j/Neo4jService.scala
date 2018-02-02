package stix.db.neo4j

import java.io.File

import com.kodekutters.neo4j.Neo4jFileLoader.readBundle
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
      if (file.getName.toLowerCase.endsWith(".zip")) loadBundleZipFile(neoLoader, file)
      else neoLoader.loadBundleFile(file)
      controller.showThis("Done saving: " + file.getName + " to Neo4jDB: " + dbDirectory.getName, Color.Black)
      controller.showThis("   SDO: " + neoLoader.loader.counter.count("SDO") + " SRO: " + neoLoader.loader.counter.count("SRO") + " StixObj: " + neoLoader.loader.counter.count("StixObj"), Color.Black)
      neoLoader.loader.counter.reset()
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

  private def loadBundleZipFile(fileLoader: Neo4jFileLoader, inFile: File): Unit = {
    import scala.collection.JavaConverters._
  //  logger.info("processing file: " + inFile.getCanonicalPath)
    // get the zip file
    val rootZip = new java.util.zip.ZipFile(inFile)
    // for each entry file containing a single bundle
    rootZip.entries.asScala.foreach(f => {
      if (f.getName.toLowerCase.endsWith(".json") || f.getName.toLowerCase.endsWith(".stix")) {
        readBundle(rootZip.getInputStream(f)) match {
          case Some(bundle) =>
          //  logger.info("file: " + f.getName + " --> " + inFile)
            fileLoader.loader.loadIntoNeo4j(bundle)
            fileLoader.loader.counter.log()
          case None => println("ERROR invalid bundle JSON in zip file: \n")
            // logger.error("ERROR invalid bundle JSON in zip file: \n")
        }
      }
    })
    fileLoader.loader.close()
  }

}
