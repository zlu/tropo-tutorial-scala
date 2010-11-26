package com.example

import scala.xml.{Text, Node}
import org.scalatra._
import java.net.URL
import scalate.ScalateSupport
import scala.io.Source.fromInputStream
import java.net.URL

class MyScalatraFilter extends ScalatraFilter with ScalateSupport with UrlSupport{

  object Template {

    def style() = 
      """
      pre { border: 1px solid black; padding: 10px; } 
      body { font-family: Helvetica, sans-serif; } 
      h1 { color: #8b2323 }
      """

    def page(title:String, content:Seq[Node]) = {
      <html>
        <head>
          <title>{ title }</title>
          <style>{ Template.style }</style>
        </head>
        <body>
          <h1>{ title }</h1>
          { content }
          <hr/>
          <a href={url("/")}>Sending SMS using Tropo With Scalatra</a>
        </body>
      </html>
    }
  }
  
  get("/") {        
	Template.page("Sending SMS Using Tropo With Scalatra",
	<form action={url("/send")} method='POST'>
	  Phone Number: <input name='to' type='text'/><br />
	  Message: <input name='msg' type='text'/><br />
	  <input type='submit'/>
         </form>
         <pre>Route: /</pre>
	 )
  }

  post("/send") {
    var tropoURL = "http://api.tropo.com/1.0/sessions?action=create"
    val tropoToken = "1aba4b151514ae4caaf8340879b3e456893a5f5f7d13be43d5df9546c147090c4773a68016b0dae4da7d66bc"
java.lang.System.out.println(request.getAttribute("to"))
    tropoURL += "&to=" + request.getParameter("to").toString() + "&msg=" + request.getParameter("msg").toString() + "&token=" + tropoToken
    val url = new URL(tropoURL)
    fromInputStream(url.openStream).getLines.foreach(print)

    Template.page("SMS Send",
    <p>You send to: {params("to")} with the following message: {params("msg")}</p>
    <pre>Route: /send</pre>
    )
  }

  notFound {
    // If no route matches, then try to render a Scaml template
    val templateBase = requestPath match {
      case s if s.endsWith("/") => s + "index"
      case s => s
    }
    val templatePath = "/WEB-INF/scalate/templates/" + templateBase + ".scaml"
    servletContext.getResource(templatePath) match {
      case url: URL => 
        contentType = "text/html"
        templateEngine.layout(templatePath)
      case _ => 
        filterChain.doFilter(request, response)
    } 
  }

  protected def contextPath = request.getContextPath
}
