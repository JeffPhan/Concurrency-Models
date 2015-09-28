object ParseCrawl extends App {
  import java.util.concurrent._

  try{
    val timeCrawl = new CrawlTimer    
    val url = "http://www2.cs.uh.edu/~svenkat/fall2014pl/samplepages"
    val sequentialCrawl = new SequentialCrawler
    val seqCountAndTime = timeCrawl.timer{sequentialCrawl.crawl(url)}
    println(s"Sequential: ${seqCountAndTime._1} Time: ${seqCountAndTime._2} (s)")
    
    val synchronizedCrawl = new SynchronizedCrawl
    val synchCountAndTime = timeCrawl.timer{synchronizedCrawl.startCrawling(url)}
    println(s"Synchronized: ${synchCountAndTime._1} Time: ${synchCountAndTime._2} (s)")

    val stmCrawl = new stmCrawler
    val stmCountAndTime = timeCrawl.timer{stmCrawl.startCrawling(url)}
    println(s"STM: ${stmCountAndTime._1} Time: ${stmCountAndTime._2} (s)")
    val actorCrawl = new ActorCrawler
    val actorCountAndTime = timeCrawl.timer{actorCrawl.startCrawling(url)}
    println(s"Actors: ${actorCountAndTime._1} Time: ${actorCountAndTime._2} (s)")
    
  }
  catch{
    case e:Exception => println("Exception Thrown: " + e + "\n")
  }
}