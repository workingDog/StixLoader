package stix.db.elastic

import java.io.File

import com.kodekutters.stix.StixObj._
import com.kodekutters.stix._
import com.typesafe.config.{Config, ConfigFactory}
import com.kodekutters.neo4j.Neo4jFileLoader.readBundle
import com.sksamuel.elastic4s.{ElasticsearchClientUri, RefreshPolicy}
import com.sksamuel.elastic4s.http.HttpClient
import play.api.libs.json._
import stix.controllers.StixLoaderControllerInterface

import scala.io.Source
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.{implicitConversions, postfixOps}
import scalafx.scene.paint.Color
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.playjson._
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.{HttpClientConfigCallback, RequestConfigCallback}


/**
  * for saving STIX-2 objects to a Elasticsearch db
  */
object ElasticStix {

  val customObjectType = "custom-object"

  val listOfAllTypes: Seq[String] = Util.listOfSDOTypes ++ Util.listOfSROTypes ++ Util.listOfStixTypes ++ List(customObjectType)

  val counter = mutable.Map("SDO" -> 0, "SRO" -> 0, "StixObj" -> 0)

  def resetCount(): Unit = counter.foreach({ case (k, v) => counter(k) = 0 })

  def inc(k: String): Unit = counter(k) = counter(k) + 1

  def count(stix: StixObj): Unit = {
    stix match {
      case x: SDO => inc("SDO")
      case x: SRO => inc("SRO")
      case x: StixObj => inc("StixObj")
    }
  }

  val config: Config = ConfigFactory.load

  var isReady = false

  def isConnected: Boolean = isReady

  var host = "localhost"
  var port = 9200
  var esName = ""
  var clusterName = "elasticsearch"
  var user = "elastic"
  var psw = "xs4XO9dMp46tsfmiUNyc"

  try {
    host = config.getString("elasticsearch.host")
    port = config.getInt("elasticsearch.port")
    esName = config.getString("elasticsearch.name")
    clusterName = config.getString("elasticsearch.cluster_name")
    user = config.getString("elasticsearch.user")
    psw = config.getString("elasticsearch.password")
  } catch {
    case e: Throwable => println("---> config error: " + e)
  }

  // todo ---> does not work
  //  val theUri = "http://" + user + ":" + psw + "@" + host + ":" + port.toString +"/"
  //  val theUri = clusterName + "://" + user + ":" + psw + "@" + host + ":" + port.toString + "?ssl=true"
  //  val client = HttpClient(ElasticsearchClientUri(theUri))

  lazy val provider = {
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(user, psw)
    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }

  val client = HttpClient(ElasticsearchClientUri(host, port), new RequestConfigCallback {
    override def customizeRequestConfig(requestConfigBuilder: Builder) = {
      requestConfigBuilder
    }
  }, new HttpClientConfigCallback {
    override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder) = {
      httpClientBuilder.setDefaultCredentialsProvider(provider)
    }
  })

  /**
    * initialise this singleton
    */
  def init(): Unit = {
    try {
      println("-----> trying to connect to elasticsearch: " + host + ":" + port)
      client.execute {
        clusterState()
      }.await match {
        case Right(x) => isReady = true; println("-----> elasticsearch connected")
        case Left(err) => isReady = false; println(s"-----> elasticsearch fail to connect, error: $err")
      }
    } catch {
      case ex: Throwable => isReady = false
    }
  }

  def close(): Unit = if (client != null && isReady) client.close()

  // all non-STIX-2 types are put in the designated "custom-object" index
  private def saveBundleAsStixs(bundle: Bundle): Unit = {
    client.execute {
      bulk(bundle.objects.map(stix => {
        val stixType = if (stix.`type`.startsWith("x-")) customObjectType else stix.`type`
        count(stix)
        indexInto(stixType / stixType) source stix
      }))
    }.await
  }

  def saveFileToDB(file: File, controller: StixLoaderControllerInterface): Unit = {
    if (isConnected) {
      controller.showSpinner(true)
      Future(try {
        controller.showThis("Saving: " + file.getName + " to Elasticsearch: " + esName, Color.Black)
        if (file.getName.toLowerCase.endsWith(".zip")) {
          saveBundleZipFile(file)
        } else {
          saveBundleFile(file)
        }
        controller.showThis("Done saving: " + file.getName + " to Elasticsearch: " + esName, Color.Black)
        controller.showThis("   SDO: " + counter("SDO") + " SRO: " + counter("SRO") + " StixObj: " + counter("StixObj"), Color.Black)
        resetCount()
        println("----> Done saving: " + file.getName + " to Elasticsearch: " + esName)
      } catch {
        case ex: Throwable =>
          println("----> Fail to save data to Elasticsearch: " + esName)
          controller.showThis("Fail to save data to Elasticsearch: " + esName, Color.Red)
      } finally {
        controller.showSpinner(false)
        close()
      })
    } else {
      controller.showThis("Elasticsearch: " + esName + " is not connected", Color.Red)
      println("----> Elasticsearch: " + esName + " is not connected, no saving done")
    }
  }

  def saveBundleFile(file: File): Unit = {
    // read a STIX bundle from the file
    val jsondoc = Source.fromFile(file).mkString
    Option(Json.parse(jsondoc)) match {
      case None => println("-----> could not parse JSON in file: " + file.getName)
      case Some(js) =>
        // create a bundle object from it and convert its objects to nodes and relations
        Json.fromJson[Bundle](js).asOpt match {
          case None => println("\n-----> ERROR reading bundle in file: " + file.getName)
          case Some(bundle) => saveBundleAsStixs(bundle)
        }
        close()
    }
  }

  def saveBundleZipFile(file: File): Unit = {
    import scala.collection.JavaConverters._
    // get the zip file
    val rootZip = new java.util.zip.ZipFile(file)
    // for each entry file containing a single bundle
    rootZip.entries.asScala.foreach(f => {
      if (f.getName.toLowerCase.endsWith(".json") || f.getName.toLowerCase.endsWith(".stix")) {
        readBundle(rootZip.getInputStream(f)) match {
          case Some(bundle) => saveBundleAsStixs(bundle)
          case None => println("-----> ERROR invalid bundle JSON in zip file: \n")
        }
      }
    })
    close()
  }

}
