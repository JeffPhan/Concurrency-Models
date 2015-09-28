class SequentialCrawler {
	var visitedURLs = List[String]()
  	val parser = new Parser
	
	def crawl(baseURL:String) : Int = {
		if (!visitedURLs.contains(baseURL))
		{
			visitedURLs ::= baseURL
			parser.parse(baseURL).foldLeft(1){(total, link) =>
				total + crawl(link)
			}
		}
		else
			return 0
	}
}