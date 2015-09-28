import java.util.concurrent._

class SynchronizedCrawl {
  
	val threadPool = Executors.newFixedThreadPool(16)
	val parser = new Parser
	val synchPhaser = new Phaser
	val error = new scala.concurrent.SyncVar[Exception]
	val visitedURLs = new ConcurrentHashMap[String, String]

	def startCrawling (baseURL: String) = {
		try {
			val crawlPhase = synchPhaser.getPhase()
			createCrawler(baseURL)
			synchPhaser.awaitAdvance(crawlPhase)
			threadPool.shutdownNow()
      
			if(error.isSet)
				throw error.take()
			else        
				visitedURLs.size
		}
		catch {
			case e:Exception => {
				synchPhaser.forceTermination
				throw e
			}
		}
	}

	def createCrawler(baseURL: String) : Unit = {
		synchPhaser.register()
    
		threadPool.execute(new Runnable {
			def run() {
				try {
					crawl(baseURL)
				}
				catch {
					case e:Exception => {
						error.put(e)
						synchPhaser.forceTermination
					} 
				}
			}
		})
	}

	def shouldProcess(url: String) : Boolean = {
		this.synchronized {
			if(!visitedURLs.containsKey(url)) {
				visitedURLs.put(url, url)
				true
			}
			else
				false
		}
	}
   
	def crawl(baseURL: String) = {
		if (shouldProcess(baseURL)) {
			parser.parse(baseURL).foreach{createCrawler(_)}
		}      
		synchPhaser.arrive()
	}
}