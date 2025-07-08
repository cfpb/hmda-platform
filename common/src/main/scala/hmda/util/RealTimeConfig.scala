package hmda.util

import com.typesafe.config.{ Config, ConfigFactory }
import io.kubernetes.client.informer.{ ResourceEventHandler, SharedInformerFactory }
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.{ V1ConfigMap, V1ConfigMapList }
import io.kubernetes.client.util.CallGeneratorParams
import org.slf4j.LoggerFactory

class RealTimeConfig(val cmName: String, val ns: String) {
  private val log = LoggerFactory.getLogger(getClass)
  private var currentConfig: Option[Config] = None
  private var factory: Option[SharedInformerFactory] = None

  private val config = ConfigFactory.load()

  private val runMode = if (config.hasPath("hmda.runtime.mode")) config.getString("hmda.runtime.mode") else "dev"

  if (runMode == "kubernetes") {
    try {
      val client = io.kubernetes.client.util.Config.defaultClient()
      val api = new CoreV1Api(client)
      factory = Option(new SharedInformerFactory(client))
      val informer = factory.get.sharedIndexInformerFor((params: CallGeneratorParams) => {
        api.listNamespacedConfigMap(ns)
          .fieldSelector(s"metadata.name=$cmName")
          .resourceVersion(params.resourceVersion)
          .timeoutSeconds(params.timeoutSeconds)
          .watch(params.watch)
          .buildCall(null)
      }, classOf[V1ConfigMap], classOf[V1ConfigMapList])
      informer.addEventHandler(new ResourceEventHandler[V1ConfigMap] {
        override def onAdd(obj: V1ConfigMap): Unit = {
          log.debug("cm added: {}", obj)
          setConfig(obj)
        }

        override def onUpdate(oldObj: V1ConfigMap, newObj: V1ConfigMap): Unit = {
          log.debug("cm updated: {}", newObj)
          setConfig(newObj)
        }

        override def onDelete(obj: V1ConfigMap, deletedFinalStateUnknown: Boolean): Unit = log.warn("cm deleted: {}, deleteStateUnknown: {}", obj, deletedFinalStateUnknown)
      })

      factory.get.startAllRegisteredInformers()
      setConfig(api.readNamespacedConfigMap(cmName, ns).execute())
    } catch {
      case e: ApiException =>
        log.error(s"Failed to setup informer, most likely role permission issues. ${e.getResponseBody}", e)
        factory.get.stopAllRegisteredInformers()
      case e: Throwable =>
        log.error(s"Failed to setup informer", e)
        factory.get.stopAllRegisteredInformers()
    }
  }

  private def setConfig(cm: V1ConfigMap): Unit = {
    try {
      currentConfig = Some(ConfigFactory.parseMap(cm.getData))
    } catch {
      case e: Throwable => log.error(s"failed to parse configmap $cm", e)
    }
  }

  def getString(key: String): String = currentConfig match {
    case Some(config) => config.getString(key)
    case _ => ""
  }

  def getSeq(key: String, delimiter: String = ","): Seq[String] = getString(key).split(delimiter).toSeq
}
