package promisering

import unfiltered.netty.cycle.Planify
import unfiltered.netty.Http
import java.net.URL

object Server {
  def main(args: Array[String]) {
    Http(8080)
      .resources(new URL(getClass().getResource("/www/robots.txt"), "."))
      .handler(Planify(Github.oauth orElse Rings.pages)).run
  }
}
