name := "brumhack4"

version := "1.0"

lazy val `brumhack4` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc, cache, ws, specs2 % Test,
  "org.scalikejdbc" %% "scalikejdbc" % "2.3.5",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  