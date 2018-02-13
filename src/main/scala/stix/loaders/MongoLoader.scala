package stix.loaders

import stix.controllers.StixLoaderControllerInterface
import stix.db.mongo.MongoDbStix
import stix.info._

import scalafx.scene.paint.Color

object MongoLoader {

  def load(fromMongo: MongoInfo, destination: InfoMessage, controller: StixLoaderControllerInterface) {
    if (MongoDbStix.isConnected) {
      destination match {
        case toES: ESInfo => controller.showThis("MongoDB to Elasticsearch is not yet implemented", Color.Red)
        case toNeo: NeoInfo => MongoDbStix.saveMongoToNeo4j(toNeo.info, controller)
        case toFile: FileInfo => MongoDbStix.saveMongoToFile(toFile.info, controller)

        case x => println("-------> toDestination=" + x)
      }
    } else {
      controller.showThis("MongoDB is not available, try again", Color.Red)
    }
  }

}
