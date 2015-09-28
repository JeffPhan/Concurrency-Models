import akka.actor._
import akka.routing.RoundRobinPool
class ActorCrawler {

	sealed trait CrawlMessage
	case class CreateWorker(url: String) extends CrawlMessage
	case class Crawl(url: String) extends CrawlMessage
	case object Result extends CrawlMessage
	case class FinishUp(count: Int) extends CrawlMessage

	object FinalResult{
		var answer = 0
	}

	class WorkerActor extends Actor {
		
		val parser = new Parser

		def receive = {
		    case Crawl(url) =>
		    	parser.parse(url).foreach{sender ! CreateWorker(_)}
		    	sender ! Result
		}
	}

	class MainActor (nrOfWorkers: Int, listener: ActorRef) extends Actor {
		
		var visitedURLs = List[String]()
		var working : Int = _
		val workerRouter = context.actorOf(Props(new WorkerActor).withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")
		
		def completeProcess = {
			listener ! FinishUp(visitedURLs.size)
			context.stop(self)
		}


		def receive = {
			case CreateWorker(url) =>
				if (!visitedURLs.contains(url))
				{
					visitedURLs ::= url
					working += 1
					workerRouter ! Crawl(url)
				}

				if(working == 0)
				{
					completeProcess
				}
			case Result =>
				working -= 1
				if(working == 0)
				{
					completeProcess
				}
		}
	}

	class Listener extends Actor {
		def receive = {
			case FinishUp(count)=>
				FinalResult.answer = count
				context.system.shutdown()
		}
	}

	def startCrawling(baseURL: String): Int = {
		val system = ActorSystem("CrawlSystem")
		val listener = system.actorOf(Props(new Listener), name = "listener")
		val master = system.actorOf(Props(new MainActor (16, listener)), name = "master")

		master ! CreateWorker(baseURL)

		while(FinalResult.answer == 0){}

		FinalResult.answer
	}
}