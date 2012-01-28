package promisering

import unfiltered.response.Html
import scala.xml.NodeSeq

object Views {

  def css(path: String) =
    <link rel="stylesheet" type="text/css" href={path}/>

  def script(path: String) =
    <script type="text/javascript" src={path}></script>

  def layout(contents: NodeSeq) = Html(
    <html>
      <head>
        <title>promise ring</title>
        { css("/css/pr.css") }
      </head>
      <body>
        <div id="trust">
          <div class="about contained">
            Promise Ring is a <a href="http://en.wikipedia.org/wiki/Pretty_Good_Privacy" target="_blank">PGP</a> server, well suited for honorable patrons of the githubosphere.
          </div>
        </div>
        <div id="head">
          <div id="branding" class="contained">
            <h1 id="brand"><span class="ring"></span>Promise Ring</h1>
            <p><a href="/trust" id="trust-toggle">Trust</a> is a special thing</p>
          </div>
        </div>
        <div id="content" class="contained">
          { contents }
        </div>
        { script("/js/jquery.min.js") }
        { script("/js/pr.js") }
      </body>
    </html>
  )

  def error(m: String) =
    Views.layout(
      <p>whoopsie! { m }</p>
    )

  def home(user: Github.User) =
    Views.layout(
      <div class="user-info clearfix">
        <img class="medium-avatar left" src={ user.img }/>
        <div class="meta left">
          { "Hi %s." format user.login }
          <div class="actions">
            <form action="/gh/disconnect" method="POST">
              <input type="submit" class="btn" value="Pull out"/>
            </form>
          </div>
        </div>
      </div>
      <div>
        <p>
          Upload a new key
        </p>
        <div>
          <form action="/rings" method="POST" class="clearfix">
            <input type="hidden" name="ring" value="scala"/>
            <textarea class="key" name="key"/>
            <input type="submit" class="btn right" value="Upload Key"/>
          </form>
        </div>
      </div>
    )

  def stranger =
    Views.layout(
      <div id="pitch">        
        <div>
          Are you sure those artifacts you are downloading came from the organic farm on the label? PGP can help.
        </div>
        <ul id="reasons">
          <li>blabity blah</li>
          <li>yadity yada</li>
        </ul>
        <div>
          How does it work? Ask Josh!
        </div>
        <div id="join">
          So, wanna join our little circle of trust?
          <div><a href="/gh/connect">Login with Github</a></div>
        </div>
      </div>
    )
}
