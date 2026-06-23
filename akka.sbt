ThisBuild / resolvers += "pekko-secure-mvn" at sys.env("pekko_RESOLVER")
ThisBuild / resolvers += Resolver.url("pekko-secure-ivy", url(sys.env("pekko_RESOLVER")))(Resolver.ivyStylePatterns)
