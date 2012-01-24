package promisering

trait Logger {
  def info(m: => String): Unit
  def warn(m: => String): Unit
  def error(m: => String, t: Option[Throwable]): Unit
}

object ConsoleLogger extends Logger {
  def info(m: => String) = println("[INFO] %s" format m)
  def warn(m: => String) = println("[WARN] %s" format m)
  def error(m: => String, t: Option[Throwable] = None) =
    Console.err.println("[ERROR] %s" format(
      m, t.map(_.getMessage).getOrElse("")))
}

trait Logged {
  def log: Logger
}

trait ConsoleLogged extends Logged {
  val log = ConsoleLogger
}


