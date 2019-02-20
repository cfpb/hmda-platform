addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.10.13")
credentials += Credentials(Path.fileProperty("user.dir") / ".lightbend" / "commercial.credentials")
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/commercial-releases"))(Resolver.ivyStylePatterns)
