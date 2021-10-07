import sbt.Keys.licenses

val json4sV = "3.6.7"
val specs2V = "4.8.1"
val slf4jV = "1.7.30"

val json4sNative = "org.json4s" %% "json4s-native" % json4sV
val javaWebSocket = "org.java-websocket" % "Java-WebSocket" % "1.4.0"
val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jV
val slf4jSimple = "org.slf4j" % "slf4j-simple" % slf4jV
val specs2 = "org.specs2" %% "specs2-core" % specs2V % "it,test"
val specs2Mock = "org.specs2" %% "specs2-mock" % specs2V % "test"
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.3" % "test"
val specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % specs2V % "test"
val json4sjackson = "org.json4s" %% "json4s-jackson" % json4sV % "test"
val jsonSchemaValidator = "com.github.java-json-tools" % "json-schema-validator" % "2.2.11" % "test"
val mockServer = "org.mock-server" % "mockserver-client-java" % "5.8.0" % "test"
val mockServerNetty = "org.mock-server" % "mockserver-netty" % "5.8.0"  % "test"

def module(name: String) = Project(name, file(name))
  .configs(IntegrationTest)
  .settings(
    organization := "com.infuse-ev",
    libraryDependencies += specs2,

    Defaults.itSettings,

    publishSettings
  )

val publishSettings = Seq(
  publishTo := sonatypePublishToBundle.value,

  sonatypeCredentialHost := "s01.oss.sonatype.org",

  scmInfo := Some(ScmInfo(
    url("https://github.com/IHomer/scala-ocpp"),
    "scm:git@github.com:IHomer/scala-ocpp.git"
  )),

  description := "Scala library for Open Charge Point Protocol (OCPP). Originally developed by NewMotion, now maintained by IHomer",

  licenses := Seq("GPLv3" -> new URL("https://www.gnu.org/licenses/gpl-3.0.en.html")),

  homepage := Some(url("https://github.com/IHomer/scala-ocpp")),

  developers := List(
    Developer(id="t3hnar", name="Yaroslav Klymko", email="t3hnar@gmail.com", url=url("https://github.com/t3hnar")),
    Developer(id="tux_rocker", name="Reinier Lamers", email="reinier.lamers@ihomer.nl", url=url("https://reinier.de/"))
  )
)

val messages = module("ocpp-messages")

val json = module("ocpp-json")
  .dependsOn(messages)
  .settings(
    libraryDependencies ++= Seq(
      json4sNative, slf4jApi, scalaCheck, specs2ScalaCheck, jsonSchemaValidator, json4sjackson
    )
  )

val ocppJApi =
  module("ocpp-j-api")
    .dependsOn(messages, json)
    .settings(
      libraryDependencies ++= Seq(
        javaWebSocket, slf4jApi, specs2Mock, mockServer, mockServerNetty),
      (fork in IntegrationTest) := true
    )

val exampleJsonClient =
  module("example-json-client")
  .dependsOn(json, ocppJApi)
  .settings(
    libraryDependencies += slf4jSimple,
    outputStrategy in run := Some(StdoutOutput),
    coverageExcludedPackages := ".*",
    publish := {}
  )

val exampleJsonServer =
  module("example-json-server")
    .dependsOn(json, ocppJApi)
    .settings(
      libraryDependencies += slf4jSimple,
      outputStrategy in run := Some(StdoutOutput),
      connectInput in run := true,
      coverageExcludedPackages := ".*",
      publish := {}
    )

val exampleJsonServer20 =
  module("example-json-server-20")
    .dependsOn(json, ocppJApi)
    .settings(
      libraryDependencies += slf4jSimple,
      outputStrategy in run := Some(StdoutOutput),
      connectInput in run := true,
      coverageExcludedPackages := ".*",
      publish := {}
    )

crossScalaVersions := Seq("2.13.1", "2.12.10", "2.11.12")

// don't publish the outer enclosing project, i.e. "com.infuse-ev" % "scala-ocpp"
publish / skip := true

// publishing settings needed at the top level project when running "sonatypeBundleRelease"
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeProfileName := "com.infuse-ev"
