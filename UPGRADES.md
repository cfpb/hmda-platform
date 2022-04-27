# Upgrades

## Akka Cassandra Persistence Plugin Upgrade
Migration Documentation: https://doc.akka.io/docs/akka-persistence-cassandra/current/migrations.html

##### new table needed for upgrading to 1.0+
```sql
CREATE TABLE IF NOT EXISTS akka.all_persistence_ids(
  persistence_id text PRIMARY KEY);
```

new environment variable to put into `cassandra-configmap`: `cassandra-cluster-dc` (e.g.: `dc` / `dc2`)

### Migration
newly added table needs to be populated, refer to [Migration Documentation](https://doc.akka.io/docs/akka-persistence-cassandra/current/migrations.html) in [plugin upgrade](#akka-cassandra-persistence-plugin-upgrade) section,
code snippet for migration tool:
```scala
import scala.util.Failure
import scala.util.Success

import akka.actor.ActorSystem
import akka.persistence.cassandra.reconciler.Reconciliation

// System should have the same Cassandra plugin configuration as your application
// but be careful to remove seed nodes so this doesn't join the cluster
val system = ActorSystem()
import system.dispatcher

val rec = new Reconciliation(system)
val result = rec.rebuildAllPersistenceIds()

result.onComplete {
  case Success(_) =>
    system.log.info("All persistenceIds migrated.")
    system.terminate()
  case Failure(e) =>
    system.log.error(e, "All persistenceIds migration failed.")
    system.terminate()
}
```
when running this migration code, make sure to adjust the datastax driver configurations to allow logging,
so it is visible if errors occur, or if the process stalled,
and to increase request timeout.
```HOCON
datastax-java-driver {
  basic {
    contact-points = ["localhost:9042"]
    contact-points = [${?CASSANDRA_CLUSTER_HOSTS}":9042"]
    load-balancing-policy.local-datacenter = "datacenter1"
    load-balancing-policy.local-datacenter = ${?CASSANDRA_CLUSTER_DC}
    request {
      timeout = 30 seconds
    }
  }
  advanced {
    connection {
      init-query-timeout = 30 seconds
    }
    auth-provider {
      class = PlainTextAuthProvider
      username = ""
      username = ${?CASSANDRA_CLUSTER_USERNAME}
      password = ""
      password = ${?CASSANDRA_CLUSTER_PASSWORD}
    }
    request-tracker {
      class = RequestLogger
      logs {
        success.enabled = true
        error.enabled = true
        show-values = false
        slow {
          threshold = 1 second
          enabled = true
        }
      }
    }
  }
}
```
Lightbend do not have performance figures, but they have reported the longest customer reported migration took days.
Our scenario should take 10 ~ 20 minutes.