package stix.loaders

import java.io.File

import stix.controllers.StixLoaderControllerInterface
import stix.db.mongo.MongoDbStix
import stix.db.neo4j.Neo4jService
import stix.info.{MongoInfo, NeoInfo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalafx.scene.paint.Color

case class FileLoader(fromFile: File, destination: Any, controller: StixLoaderControllerInterface) {

  destination match {
    case toNeo: NeoInfo => Neo4jService.saveFileToDB(fromFile, toNeo.dbDir, controller)
    case toMongo: MongoInfo =>
      if (MongoDbStix.isConnected()) MongoDbStix.saveFileToDB(fromFile, controller)
      else controller.showThis("MongoDB is not connected, try again", Color.Red)

    case x => println("---> toDestination=" + x)
  }

}
