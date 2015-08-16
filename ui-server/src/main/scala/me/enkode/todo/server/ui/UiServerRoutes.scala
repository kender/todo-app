package me.enkode.todo.server.ui

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import me.enkode.todo.server.common.Router

trait UiServerRoutes extends Router {
  val getFavicon: Route = (get & path("favicon.ico")) {
    getFromResource("content/favicon.ico")
  }

  val getResource: Route = (get & path("ui" / RestPath)) { resource ⇒
    getFromResource(s"content/$resource")
  }

  val getJavaScript: Route = (get & path("js"/ RestPath)) { resource ⇒
    getFromResource(s"js/$resource")
  }

  override def routes = getFavicon :: getResource :: getJavaScript :: Nil
}

object UiServerRoutes {
  class UiServerRoutesImpl() extends UiServerRoutes
  def apply(): UiServerRoutes = new UiServerRoutesImpl()
}
