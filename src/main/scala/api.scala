package promisering

import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle.Intent

object Api extends ConsoleLogged {
  import QParams._
  val signing: Intent[Any, Any] = {
    case POST(Path(Seg("api" :: "1" :: "sign" :: Nil))) & Params(p) =>
      val expecting = for {
        key <- lookup("key") is required(Valid.Required) is
          nonempty(Valid.Present) 
        circle <- lookup("circle") is required(Valid.Required) is
          nonempty(Valid.Present)
      } yield {
        log.info("Someone wanted to sign the following key in circle %s:\n %s" format(
          circle.get, key.get
        ))
        Ok
      }
    expecting(p) orFail { errors =>
      BadRequest ~> ResponseString(errors map {
          fail => fail.name + ":" + fail.error } mkString ",")
    }
  }
}
