package hmda.publisher.qa

import akka.stream.alpakka.s3.S3Settings

case class QAFileSpec[T](
                          bucket: String,
                          key: String,
                          s3Settings: S3Settings,
                          withHeaderLine: Boolean,
                          parseLine: String => T,
                          repository: QARepository[T]
                        ) {
  def filePath = s"${bucket}/${key}"
}