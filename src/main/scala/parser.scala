import org.jsoup._

class Parser {
	def parse(startingURL:String) : List[String] = {
		val doc = Jsoup.connect(startingURL).get()
		val aTags = doc.select("a[href]")

		(0 to aTags.size - 1).foldLeft(List[String]()){ (urls, e) => 
	    	 urls ::: List(aTags.get(e).attr("abs:href"))
	  	}
	}
}
