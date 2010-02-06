import sbt._

class SmelterProject(info: ProjectInfo) extends DefaultProject(info) {
  val restletRepo = "Restlet Maven Repository" at "http://maven.restlet.org"
  val restlet = "org.restlet.jse" % "org.restlet" % "2.0-M7"
  val restletFreemarker =
    "org.restlet.jse" % "org.restlet.ext.freemarker" % "2.0-M7"

  override def javaCompileOptions =
    JavaCompileOption("-Xlint") :: super.javaCompileOptions.toList
}
