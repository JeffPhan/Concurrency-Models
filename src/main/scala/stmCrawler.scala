import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import scala.concurrent.stm._

class stmCrawler {
  
  val threadPool = Executors.newFixedThreadPool(16)
  val parser = new Parser
  val error = new scala.concurrent.SyncVar[Exception]
  val visitedURLs = TSet[String]()
  val checkpoint = Ref(1)
  val counter = Ref(0)


  def startCrawling (baseURL: String) = {
    try{
      createCrawler(baseURL)

      atomic{ implicit txn =>
        if(checkpoint() != 0){
          retry
        }
      }
    
      threadPool.shutdown()
      if(error.isSet){
        throw error.take()
      }
      else{
        atomic{ implicit txn =>
          visitedURLs.size
        }
      }            
    }
    catch{
      case e:Exception => {
        throw e
      }
    }
  }

  def createCrawler(baseURL: String) : Unit = {   
    atomic{ implicit txn =>
      counter() = counter() + 1
    }
    threadPool.execute(new Runnable() {
      def run {
          crawl(baseURL)
      }  
    })
  }

  def shouldProcess(url: String) : Boolean = {
    atomic{ implicit txn =>
      if(!visitedURLs.contains(url)){
        visitedURLs += url
        true
      }
      else{
        false
      }
    }
  }
   
  def crawl(baseURL:String) : Unit = {
    try{
        if(shouldProcess(baseURL)){
          parser parse(baseURL) foreach{createCrawler(_)}
        }
    }
    catch{
      case e:Exception =>{
        error.put(e)
        atomic{ implicit txn =>
          checkpoint() = 0
        }
      } 
    }
    atomic{ implicit txn =>
      counter() = counter() - 1
      if(counter() == 0)
        checkpoint() = 0        
    }
  }
}