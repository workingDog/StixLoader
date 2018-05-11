
organization := "com.github.workingDog"

name := "stixloader"

version := (version in ThisBuild).value

scalaVersion := "2.12.6"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.3",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.3",
  "com.github.workingDog" %% "scalastix" % "0.7",
  "com.github.workingDog" %% "stixtoneolib" % "0.3",
  "org.reactivemongo" %% "reactivemongo" % "0.12.7",
  "org.reactivemongo" %% "reactivemongo-play-json" % "0.12.7-play26",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
).map(_.exclude("org.slf4j", "*"))

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "com.google.inject" % "guice" % "4.1.0",
  "com.jfoenix" % "jfoenix" % "8.0.2",
  "org.scalafx" %% "scalafx" % "8.0.144-R12",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.1.4",
  "com.sksamuel.elastic4s" %% "elastic4s-play-json" % "6.1.4",
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % "6.1.4"
)

assemblyMergeStrategy in assembly := {
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".rsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".dsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".sf" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".des" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSES.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSE.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "logback.xml" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "shaded-asynchttpclient-1.1.3.jar" => MergeStrategy.first
  case PathList(xs@_*) if xs.last endsWith "netty-all-4.1.17.Final.jar" => MergeStrategy.first

  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first

  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

homepage := Some(url("https://github.com/workingDog/stixloader"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

mainClass in(Compile, run) := Some("stix.StixLoaderApp")

mainClass in assembly := Some("stix.StixLoaderApp")

assemblyJarName in assembly := "stixloader-" + version.value + ".jar"

fork := true

//------------------------sbt-native-packager--------------------
//
//enablePlugins(JDKPackagerPlugin)
//
//packageSummary := "stixloader package"
//
//packageDescription := "loads STIX-2.0 objects from source storage systems to destination output systems."
//
//lazy val iconGlob = sys.props("os.name").toLowerCase match {
//  case os if os.contains("mac") => "*.icns"
//  case os if os.contains("win") => "*.ico"
//  case _ => "*.png"
//}
//
//jdkAppIcon :=  (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file)
//
//jdkPackagerType := "installer"
//
//jdkPackagerJVMArgs := Seq("-Xmx1g")
//
//jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value)
//
//jdkPackagerAppArgs := Seq(maintainer.value, packageSummary.value, packageDescription.value)
//
////jdkPackagerAssociations := Seq(
////  FileAssociation("foobar", "application/foobar", "Foobar file type"),
////  FileAssociation("barbaz", "application/barbaz", "Barbaz file type", jdkAppIcon.value)
////)
//
//// Example of specifying a fallback location of `ant-javafx.jar` if plugin can't find it.
//(antPackagerTasks in JDKPackager) := (antPackagerTasks in JDKPackager).value orElse {
//  for {
//    f <- Some(file("/usr/lib/jvm/java-8-oracle/lib/ant-javafx.jar")) if f.exists()
//  } yield f
//}
//
//fork := true

//------------------------sbt-proguard--------------------

//enablePlugins(SbtProguard)
//
//javaOptions in(Proguard, proguard) := Seq("-Xmx12G")
//
//proguardOptions in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings", "-dontobfuscate", "-dontusemixedcaseclassnames")
//
//proguardOptions in Proguard += ProguardOptions.keepMain("stix.StixLoaderApp")

// proguardMerge in Proguard := true

//proguardInputFilter in Proguard := { file =>
//  file.name match {
//    case "scala-library.jar" => Some("!META-INF/**")
//    case _                   => None
//  }
//}

//proguardMergeStrategies in Proguard ++= Seq(
//  ProguardMerge.discard("META-INF/.*".r),
//  ProguardMerge.discard("\\.dsa$".r),
//  ProguardMerge.discard("\\.sf$".r),
//  ProguardMerge.discard("\\.des$".r),
//  ProguardMerge.discard("\\.LICENSES.txt$".r),
//  ProguardMerge.discard("\\.NOTICE.txt$".r),
//  ProguardMerge.discard("\\.LICENSE.txt$".r),
//  ProguardMerge.discard("\\.logback.xml$".r),
//  ProguardMerge.discard("logback.xml".r),
//
//  ProguardMerge.discard("META-INF/LICENSE.txt".r),
//  ProguardMerge.discard("META-INF/LICENSES.txt".r),
//  ProguardMerge.discard("META-INF/NOTICE.txt".r),
//
//  ProguardMerge.discard("META-INF/LICENSE".r),
//  ProguardMerge.discard("META-INF/LICENSES".r),
//  ProguardMerge.discard("META-INF/NOTICE".r),
//
//  ProguardMerge.last("lib/lucene-core-4.10.4.jar"),
//  ProguardMerge.last("lib/lucene-core-5.5.0.jar"),
//
//  ProguardMerge.discard("META-INF/services/org.apache.lucene.codecs.Codec"),
//  ProguardMerge.discard("META-INF/services/org.apache.lucene.codecs.DocValuesFormat"),
//  ProguardMerge.discard("META-INF/services/org.apache.lucene.codecs.PostingsFormat"),
//
//  ProguardMerge.append("reference.conf")
//)

// proguardInputs in Proguard := (dependencyClasspath in Compile).value.files

// proguardFilteredInputs in Proguard ++= ProguardOptions.noFilter((packageBin in Compile).value)
