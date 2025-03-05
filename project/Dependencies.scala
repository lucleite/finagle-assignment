import sbt.*

object Dependencies {

  lazy val finchVersion      = "0.34.0"
  lazy val catsEffectVersion = "3.5.7"
  lazy val shapelessVersion  = "2.3.13"
  lazy val circeVersion      = "0.14.2"

  lazy val finchDependecies = Seq(
    "com.github.finagle" %% "finch-core"  % finchVersion,
    "com.github.finagle" %% "finch-circe" % finchVersion
  )

  lazy val JsonDependencies = Seq(
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser"  % circeVersion
  )

  lazy val loggingDependencies = Seq(
    "org.slf4j"                   % "slf4j-api"       % "2.0.17",
    "ch.qos.logback"              % "logback-classic" % "1.5.17",
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
  )

  lazy val otherDependencies = Seq(
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "com.chuusai"   %% "shapeless"   % shapelessVersion,
    "com.typesafe" % "config" % "1.4.3"
  )

  lazy val compileDependencies = finchDependecies ++ JsonDependencies ++ loggingDependencies ++ otherDependencies

  lazy val testDependencies = Seq(
    "org.scalameta" %% "munit"     % "0.7.29" % Test,
    "org.scalactic" %% "scalactic" % "3.2.19" % Test,
    "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
    "org.mockito" %% "mockito-scala" % "1.16.42" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0" % Test
  )
}
