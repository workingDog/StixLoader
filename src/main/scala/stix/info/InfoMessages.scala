package stix.info

import java.io.File

sealed trait InfoMessage {
  val info: AnyRef
}

case class ESInfo(info: String = "") extends InfoMessage

case class FileInfo(info: File) extends InfoMessage

case class MongoInfo(info: String = "") extends InfoMessage

case class NeoInfo(info: File) extends InfoMessage

case class NoInfo(info: String = "") extends InfoMessage

