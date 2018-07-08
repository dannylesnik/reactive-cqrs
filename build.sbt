
scalaVersion := "2.12.6"

name := "reactive-cqrs"
organization := "com.vanilla.poc"
version := "1.0"


lazy val write = (project in file("write"))
  .enablePlugins(PlayScala)
  .settings(
  name := "write",
  settings,
  libraryDependencies ++= commonDependencies ++ Seq(
    guice,
    dependencies.mysql,
    dependencies.kafkaNative,
    dependencies.slick,
    dependencies.jacksonCore,
    dependencies.jacksonDataBind,
    dependencies.jacksonModuleScala,
    dependencies.hikariPool
  )
)



lazy val read = project
  .settings(
    name := "read",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.playJason,
      dependencies.kafkaStreams,
      dependencies.e4sCore,
      dependencies.e4sPlayJson,
      dependencies.e4sHttp
    )
  )


lazy val dependencies =
  new {
    private val akkaStreamsVersion = "2.5.13"
    private val kafkaStreamsVersion = "0.22"
    private val playJsonVersion = "2.6.9"
    private val e4sCoreVersion = "6.2.9"
    private val mysqlVersion = "8.0.11"
    private val slickVersion: String = "3.2.3"
    private val kafkaNativeVersion = "0.10.1.0"
    private val slf4jVersion = "1.6.4"
    private val jacksonVersion = "2.9.6"

    val akkaStreams   = "com.typesafe.akka" %% "akka-stream"      % akkaStreamsVersion
    val kafkaStreams  = "com.typesafe.akka" %% "akka-stream-kafka" % kafkaStreamsVersion
    val playJason     = "com.typesafe.play" %% "play-json" % playJsonVersion
    val e4sCore       = "com.sksamuel.elastic4s" %% "elastic4s-core" % e4sCoreVersion
    val e4sPlayJson   = "com.sksamuel.elastic4s" %% "elastic4s-play-json" % e4sCoreVersion
    val e4sHttp       = "com.sksamuel.elastic4s" %% "elastic4s-http" % e4sCoreVersion
    val mysql         = "mysql" % "mysql-connector-java" % mysqlVersion
    val kafkaNative   = "org.apache.kafka" % "kafka-clients" % kafkaNativeVersion
    val slick         = "com.typesafe.slick" %% "slick" % slickVersion
    val slf4j         = "org.slf4j" % "slf4j-nop" % slf4jVersion
    val hikariPool    = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
    val jacksonCore   = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
    val jacksonDataBind  = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
    val jacksonModuleScala =  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

  }


lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val commonDependencies = Seq(
  dependencies.akkaStreams
)


lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val settings =
  commonSettings