ThisBuild / resolvers += "akka-secure-mvn" at sys.env("AKKA_RESOLVER")
ThisBuild / resolvers += Resolver.url("akka-secure-ivy", url(sys.env("AKKA_RESOLVER")))(Resolver.ivyStylePatterns)
