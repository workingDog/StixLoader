package stix.loaders

import java.io.File

import stix.controllers.StixLoaderControllerInterface
import stix.db.elastic.ElasticStix
import stix.db.mongo.MongoDbStix
import stix.db.neo4j.Neo4jService
import stix.info.InfoMessages._

import scalafx.scene.paint.Color

object FileLoader {

  def load(fromFile: File, destination: Any, controller: StixLoaderControllerInterface): Unit = {
    destination match {
      case toES: ESInfo =>
        if (ElasticStix.isConnected) ElasticStix.saveFileToDB(fromFile, controller)
        else controller.showThis("Elasticsearch is not connected, try again", Color.Red)

      case toNeo: NeoInfo => Neo4jService.saveFileToDB(fromFile, toNeo.dbDir, controller)

      case toMongo: MongoInfo =>
        if (MongoDbStix.isConnected) MongoDbStix.saveFileToDB(fromFile, controller)
        else controller.showThis("MongoDB is not connected, try again", Color.Red)

      case x => println("---> toDestination=" + x)
    }
  }

}
