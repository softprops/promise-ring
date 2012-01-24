package promisering 

import unfiltered.Cycle.Intent
import unfiltered.request._
import unfiltered.response._

/**
 * This idea behind circles _is_ pgp circle of trust
 * At the moment there is only on circle (for Josh's thing)
 * In the future we could probably expand on this to make circles
 * more configurable
 * */
object Rings extends ConsoleLogged with ManagedHttp {
  import com.codahale.jerkson.Json._
  import QParams._
  import dispatch._
  import java.net.URLDecoder.decode

  def pages: Intent[Any, Any] = {

    case GET(Path("/")) & Cookies(cs) =>
      (cs("gh_auth"), cs("gh_user")) match {
        case (Some(auth), Some(ghu)) =>
          Github.Cookies.userFrom(ghu).map(Views.home).getOrElse(
            Views.error("essembling humans is hard work")
          )
        case _ => Views.stranger
      }

    case GET(Path("/rings")) =>
      Views.layout(<ul>they are all here</ul>)

    case GET(Path(Seg("rings" :: ring :: Nil))) =>
      Views.layout(<ul>keys are in yo pocket foo</ul>)

    case POST(Path("/rings")) & Params(p) & Cookies(cs) =>
      val expecting = for {
        key <- lookup("key") is
          required(Valid.Required) is nonempty(Valid.Present)
        circle <- lookup("ring") is 
          required(Valid.Required) is nonempty(Valid.Present)
        token <- external("gh_auth", cs("gh_auth").map(_.value)) is
          required(Valid.Required) is nonempty(Valid.Present)
      } yield {
        log.info("%s attempted to upload the following key to %s:\n %s" format(
          token.get, circle.get, key.get
        ))
        Redirect("/")
      }
      expecting(p) orFail { errors =>
        BadRequest ~> ResponseString(
          errors map { fail => fail.name + ": " + fail.error } mkString ", "
        )
      }
  } 
}
