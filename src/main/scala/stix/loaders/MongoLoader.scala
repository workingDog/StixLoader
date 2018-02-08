package stix.loaders

import stix.controllers.StixLoaderControllerInterface
import stix.db.mongo.MongoDbStix
import stix.info.InfoMessages._

import scalafx.scene.paint.Color

object MongoLoader {

  def load(fromMongo: MongoInfo, destination: Any, controller: StixLoaderControllerInterface) {
    if (MongoDbStix.isConnected) {
      destination match {
        case toES: ESInfo => controller.showThis("MongoDB to Elasticsearch is not yet implemented", Color.Red)
        case toNeo: NeoInfo => MongoDbStix.saveMongoToNeo4j(toNeo.dbDir, controller)
        case toFile: FileInfo => MongoDbStix.saveMongoToFile(toFile.file, controller)

        case x => println("-------> toDestination=" + x)
      }
    } else {
      controller.showThis("MongoDB is not available, try again", Color.Red)
    }
  }

}
