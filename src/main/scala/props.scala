package promisering

object Props {
  private lazy val properties = {
    val f = getClass.getResourceAsStream("/pr.properties")
    val props = new java.util.Properties
    props.load(f)
    f.close()
    props
  }
  def apply(name: String) =
    get(name) match {
      case None => sys.error("missing property %s" format name)
      case Some(p) => p
    }
  def get(name: String) =
    Option(System.getenv(name)).orElse(
      Option(properties.getProperty(name)))
}
