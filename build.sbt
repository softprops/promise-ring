organization := "me.lessis"

name := "promise-ring"

version := "0.1.0"

seq(coffeeSettings:_*)

seq(lessSettings:_*)

(resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (resourceManaged in Compile)(_ / "www" / "js")

(resourceManaged in (Compile, LessKeys.less)) <<= (resourceManaged in Compile)(_ / "www" / "css")

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.5.3",
  "net.databinder" %% "dispatch-http" % "0.8.7",
  "com.codahale" %% "jerkson" % "0.5.0",
  "org.bouncycastle" % "bcpg-jdk16" % "1.46",
  "com.jsuereth" %% "gpg-library" % "0.5"
)

resolvers += "coda" at "http://repo.codahale.com"

resolvers += Resolver.url(
  "sbt-plugin-releases",
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

