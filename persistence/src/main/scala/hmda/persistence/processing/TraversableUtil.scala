package hmda.persistence.processing

//http://stackoverflow.com/questions/2213323/how-can-i-use-map-and-receive-an-index-as-well-in-scala
object TraversableUtil {
  class IndexMemoizingFunction[A, B](f: (Int, A) => B) extends (A => B) {
    private var index = 2
    override def apply(a: A): B = {
      val ret = f(index, a)
      index += 1
      ret
    }
  }

  def doIndexed[A, B](f: (Int, A) => B): A => B = {
    new IndexMemoizingFunction(f)
  }
}
