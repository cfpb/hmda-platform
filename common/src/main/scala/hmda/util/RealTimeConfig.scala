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

  try {
    val client = io.kubernetes.client.util.Config.defaultClient()
    val api = new CoreV1Api(client)
    val factory = new SharedInformerFactory(client)
    val informer = factory.sharedIndexInformerFor((params: CallGeneratorParams) => {
      api.listNamespacedConfigMapCall(
        ns, null, null, null, s"metadata.name=$cmName", null, null, params.resourceVersion, null, null, params.timeoutSeconds, params.watch, null)
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

    factory.startAllRegisteredInformers()
    setConfig(api.readNamespacedConfigMap(cmName, ns, null))
  } catch {
    case e: ApiException =>
      log.error(s"Failed to setup informer, most likely role permission issues. ${e.getResponseBody}", e)
    case e: Throwable =>
      log.error(s"Failed to setup informer", e)
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
