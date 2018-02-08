package stix.info

import java.io.File

object InfoMessages {

  case class ESInfo(info: String = "")

  case class FileInfo(file: File)

  case class MongoInfo(info: String = "")

  case class NeoInfo(dbDir: File)


}
