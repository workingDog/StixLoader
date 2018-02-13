import sbt.Resolver

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

addSbtPlugin("com.lightbend.sbt" % "sbt-proguard" % "0.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

