name := Common.appName

lazy val core = project.
  settings(Common.settings: _*).
  settings(name := Common.appName + "-core").
  settings(libraryDependencies ++= Dependencies.core)

lazy val dynamodb = project.
  settings(Common.settings: _*).
  settings(name := Common.appName + "-dynamodb").
  settings(libraryDependencies ++= Dependencies.dynamodb).
  dependsOn(core)

lazy val root = (project in file("."))
  .settings(Common.settings: _*)
  .aggregate(core, dynamodb)