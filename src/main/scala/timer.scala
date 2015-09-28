class CrawlTimer {
  def timer[R](block: => R) = {
    val start = System.nanoTime()
    val result = block
    (result, (System.nanoTime - start).toDouble/1000000000.0000)
  }
}