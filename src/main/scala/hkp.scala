package promisering

// http://www.mit.edu/afs/net.mit.edu/project/pks/thesis/paper/thesis.html

object Hkp {
  import unfiltered.request._
  import unfiltered.response._
  import unfiltered.Cycle.Intent
  import QParams._

  private val SupportedLookupOps = Seq("get", "index")

  private def withErrors(errs: Seq[Fail[_]]) =
    BadRequest ~> ResponseString(errs.map { e => e.name + ": "+ e.error.toString } mkString(", "))

  val query: Intent[Any, Any] = {
    case Path("/pks/lookup") & Params(params) =>
      val expected = for {
        options <- lookup("options") is required("required")
        op <- lookup("op") is required("required") is
          nonempty("must be present") is
          pred({ SupportedLookupOps.contains }, {
            _ + " must be one of %s" format SupportedLookupOps.mkString(", ")
          })
        search <- lookup("search") is required("required") is nonempty("must be present")
      } yield {
        op.get match {
          case "get" =>
            ResponseString("get: %s" format search)
          case "index" =>
            ResponseString("index: %s" format search)
          case _ => BadRequest
        }
      }
      expected(params) orFail withErrors
  }

  val add: Intent[Any, Any] = {
    case Path("/pks/add") & Params(params) =>
      val expected = for {
        options <- lookup("options") is required("required")
        keytext <- lookup("keytext") is required("required") is nonempty("must be present")
      } yield {
        ResponseString("add %s" format keytext)
      }
      expected(params) orFail withErrors
  }

}
