
organization := "com.github.workingDog"

name := "stixloader"

version := (version in ThisBuild).value

scalaVersion := "2.12.4"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.neo4j" % "neo4j" % "3.2.1",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.3",
  "com.github.workingDog" %% "scalastix" % "0.7",
  "com.github.workingDog" %% "stixtoneolib" % "0.2",
  "org.reactivemongo" %% "reactivemongo" % "0.12.7",
  "org.reactivemongo" %% "reactivemongo-play-json" % "0.12.7-play26",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
).map(_.exclude("org.slf4j", "*"))

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "com.google.inject" % "guice" % "4.1.0",
  "com.jfoenix" % "jfoenix" % "1.9.1",
  "org.scalafx" %% "scalafx" % "8.0.144-R12",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.4",
  "com.typesafe" % "config" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

assemblyMergeStrategy in assembly := {
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".dsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".sf" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".des" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSES.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSE.txt" => MergeStrategy.discard
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

homepage := Some(url("https://github.com/workingDog/stixloader"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

mainClass in(Compile, run) := Some("stix.StixLoaderApp")

mainClass in assembly := Some("stix.StixLoaderApp")

assemblyJarName in assembly := "stixloader-" + version.value + ".jar"

enablePlugins(SbtProguard)

javaOptions in(Proguard, proguard) := Seq("-Xmx12G")

proguardOptions in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings", "-dontobfuscate", "-dontusemixedcaseclassnames")

proguardOptions in Proguard += ProguardOptions.keepMain("stix.StixLoaderApp")

//proguardMerge in Proguard := true
//
//proguardMergeStrategies in Proguard ++= Seq(
//  ProguardMerge.last("lib/lucene-core-4.10.4.jar"),
//  ProguardMerge.last("lib/lucene-core-5.5.0.jar"),
//  ProguardMerge.discard("META-INF/.*".r),
//  ProguardMerge.discard("\\.dsa$".r),
//  ProguardMerge.discard("\\.sf$".r),
//  ProguardMerge.discard("\\.des$".r),
//  ProguardMerge.discard("\\.LICENSES.txt$".r),
//  ProguardMerge.discard("\\.NOTICE.txt$".r),
//  ProguardMerge.discard("\\.LICENSE.txt$".r),
//
//  ProguardMerge.first("META-INF/LICENSE.txt"),
//  ProguardMerge.first("META-INF/NOTICE.txt"),
//  ProguardMerge.first("META-INF/services/org.apache.lucene.codecs.Codec"),
//  ProguardMerge.first("META-INF/services/org.apache.lucene.codecs.DocValuesFormat"),
//  ProguardMerge.first("META-INF/services/org.apache.lucene.codecs.PostingsFormat"),
//
//  ProguardMerge.append("reference.conf")
//)
//
//proguardInputs in Proguard := (dependencyClasspath in Compile).value.files
//
//proguardFilteredInputs in Proguard ++= ProguardOptions.noFilter((packageBin in Compile).value)

//-------------------------------------

//proguardInputs in Proguard := Seq(baseDirectory.value / "target" / s"scala-${scalaVersion.value.dropRight(2)}" / s"${name.value}-${version.value}.jar")
//proguardLibraries in Proguard := Seq()
//proguardInputFilter in Proguard := { file => None }
//proguardMerge in Proguard := false
