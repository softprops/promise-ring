package promisering

import unfiltered.Cycle.Intent
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cookie
import QParams._
import dispatch._

trait GithubHooks {
  import unfiltered.response.ResponseFunction
  def onMalformedAuthResponse[T](errors: Seq[Fail[T]]): ResponseFunction[Any]
  def onMalformedAuthBody: ResponseFunction[Any]
  def onMalformedUserResponse: ResponseFunction[Any]
  def afterAccess: ResponseFunction[Any]
  def onDeny(msg: String): ResponseFunction[Any]
  def onInvalidAccess(msg: String): ResponseFunction[Any]
  def afterDisconnect: ResponseFunction[Any]
  def onUnknownResponse: ResponseFunction[Any]
  def scopes: Seq[String]
}

trait GhClient {
  def clientId: String
  def clientSecret: String
}

trait DefaultGhClient extends GhClient {
  def clientId = Props("gh.client_id")
  def clientSecret = Props("gh.client_secret")
}

trait DefaultGithubHooks extends GithubHooks {
  def onMalformedAuthResponse[T](errors: Seq[Fail[T]]) = Views.error(errors map {
    e => e.name + ":" + e.error } mkString ","
  )
  def onMalformedAuthBody = Views.error("")
  def onMalformedUserResponse = Views.error("malformed response from github")
  def afterAccess = Redirect("/")
  def onDeny(msg: String) = Redirect("/")
  def onInvalidAccess(msg: String) =  Views.error(msg)
  def afterDisconnect = Redirect("/")
  def onUnknownResponse = Views.layout(<p>github is down?</p>)
  def scopes = Seq("user")
}

/**
 * Provides default configured gh auth support. To customize, use
 * new Github with DefaultHooks {
 *   override def afterAccess = Redirect("/somewhere/else")
 * }
 */
object Github extends Github with DefaultGithubHooks with DefaultGhClient {
  object Json {
    def parse(raw: String): Option[Map[String, Any]] =
      scala.util.parsing.json.JSON.parseFull(raw).map {
          _.asInstanceOf[Map[String,Any]]                                                  
      }
  }

  case class Client(token: String) {
    private val access = Map("access_token" -> token)
    val Api = :/("api.github.com").secure <:< Map(
      "Accept" -> "application/json"
    )
    def user = Api / "user" <<? access
    def gist(id: Int) = Api / "gists" / id.toString <<? access
  }

  object Auth {
    private val Access = :/("github.com").secure / "login" / "oauth" / "access_token" <:< Map(
      "accept" -> "application/json")
    
    object AccessResponseExtract {
      def from(raw: String): Option[AccessResponse] =
        Json.parse(raw).map { m =>
          AccessResponse(
            m.get("access_token").map(_.toString),
            m.get("token_type").map(_.toString),
            m.get("error").map(_.toString)
          )                                  
        }
    }
    case class AccessResponse(
      access_token: Option[String], token_type: Option[String],
      error: Option[String])

    def authorize(scopes: Seq[String] = Nil) =
      "https://github.com/login/oauth/authorize?client_id=%s%s".format(
        clientId,
        if(scopes.isEmpty) "" else "&scope=%s".format(scopes.mkString(","))
      )
    
    def access(code: String) =
      Access << Map(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code
      )
  }

  case class User(id: String, login: String, img: String)

  case class Token(value: String)

  object Cookies {
    import java.net.URLEncoder.encode
    import org.apache.commons.codec.binary.Base64.{
      encodeBase64URLSafeString => b64encode,
      decodeBase64 => b64decode }

    def user(id: String, login: String, img: String) =
      new UserCookie(id, login, img)

    def auth(token: String) = new AuthCookie(token)

    def userFrom(c: Cookie): Option[User] =
      (Map.empty[String, String] /: new String(
        b64decode(c.value.getBytes("utf8")), "utf8").split("[|]").grouped(2)) {
          (a, e) => e match {
            case Array(k, v) => a + (k -> v)
          }
        } match {
          case m if(Seq("id", "login", "img").forall(m.isDefinedAt)) =>
            Some(User(m("id"), m("login"), m("img")))
          case m => println(m); None
        }

    def tokenFrom(c: Cookie) = Token(c.value)

    class AuthCookie(token: String) extends Cookie("gh_auth", token, path = Some("/"))
    class UserCookie(id: String, login: String, img: String) extends Cookie(
      "gh_user", b64encode(
        "id|%s|login|%s|img|%s".format(id, login, img).getBytes("utf8")
      ), path = Some("/")
    )
  }
}


class Github extends ConsoleLogged with ManagedHttp { self: GithubHooks with GhClient =>
  import Github._
  def oauth: Intent[Any, Any] = {

    case GET(Path(Seg("gh" :: "connect" :: Nil))) =>
      Redirect(Auth.authorize(scopes))

    case GET(Path(Seg("gh" :: "authorized" :: Nil))) & Params(p) =>
      val expected = for {
        code <- lookup("code") is optional[String, String]
        error <- lookup("error") is optional[String, String]
      } yield {
        (code.get, error.get) match {
          case (Some(c), _) =>
            val body = http(Auth.access(c) as_str)
            Auth.AccessResponseExtract.from(body) match {
              case Some(Auth.AccessResponse(Some(at), _, _)) =>
                 try {
                   Json.parse(http(Github.Client(at).user as_str)) match {
                     case Some(user) =>
                       log.info("authenticated user %s" format user("login"))
                       ResponseCookies(
                         Cookies.auth(at)) ~>
                       ResponseCookies(
                         Cookies.user(
                           user("id").toString,
                           user("login").toString,
                           user("avatar_url").toString)) ~>
                       afterAccess
                     case _ => onMalformedUserResponse
                   }
                 } catch {
                   case StatusCode(400, msg) =>
                     log.warn("bad auth request for token %s" format at)
                     onInvalidAccess(msg)                  
                 }                
              case r => onMalformedAuthBody             
            }
          case (_, Some(e)) => onDeny(e)
            
          case _ => onUnknownResponse
        }
      }
      expected(p) orFail { errors =>
        onMalformedAuthResponse(errors)
      }
    
    case POST(Path(Seg("gh" :: "disconnect" :: Nil))) =>
      ResponseCookies(
        Cookie("gh_auth", "", path = Some("/"), maxAge = Some(0))) ~>
      ResponseCookies(Cookie("gh_user", "", path = Some("/"), maxAge = Some(0))) ~>
        afterDisconnect
        
  }
}
