package me.enkode.todo.client

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import me.enkode.todo.model.{TodoItem, TodoList}
import me.enkode.trace.Trace
import org.scalajs.dom._
import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.scalajs.js.annotation.JSExport

@JSExport("TodoClient")
class TodoClient(mountNode: Node) {
  implicit val trace = Trace("todo client")
  val TodoListComponent = ReactComponentB[TodoList]("TodoList").render_P(props ⇒ {
    def createItem(todoItem: TodoItem) = <.li(todoItem.name)
    <.ul(props.items map createItem)
  }).build

  case class State(todoList: TodoList, text: String)(implicit val trace: Trace)

  class Backend($: BackendScope[Unit, State]) {
    def onChange(e: ReactEventI) = {
      val stateText = e.target.value
      $.modState(_.copy(text = stateText))
    }

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $ modState { s ⇒
        val item = new TodoItem(s.text)
        Ajax.post(s"/todo/${s.todoList.id}", write(item), headers = Map("Content-Type" → "application/json") ++ s.trace.toHttpHeaders )
        val todoList = s.todoList.withItem(item)
        State(todoList, s.text)
      }
    }

    def render(state: State) = {
      <.div(
        <.h3("TODO"),
        TodoListComponent(state.todoList),
        <.form(^.onSubmit ==> handleSubmit,
          <.input(^.onChange ==> onChange, ^.value := state.text),
          <.button("Add #", state.todoList.items.size + 1)
        )
      )
    }
  }

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(TodoList(id = UUID.fromString("9190815b-7966-4c18-903a-26d197fc5df4")), ""))
    .renderBackend[Backend]
    .componentDidMount(scope => Callback {
      import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
      Ajax.get(s"/todo/${scope.state.todoList.id}", headers = scope.state.trace.toHttpHeaders) map { xhr =>
        read[TodoList](xhr.responseText)
      } foreach { todoList =>
        scope.modState(_.copy(todoList = todoList)).runNow()
      }
    })
    .build

  ReactDOM.render(TodoApp(), mountNode)
}
