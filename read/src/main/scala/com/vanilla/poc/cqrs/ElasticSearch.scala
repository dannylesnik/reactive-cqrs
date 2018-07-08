package com.vanilla.poc.cqrs

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import play.api.libs.json.{JsObject, JsValue, Json}
import com.sksamuel.elastic4s.http.ElasticDsl._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global


trait ElasticSearch {


  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))



  def indexEvent(jsValue:JsValue): Future[Try[JsValue]] ={

    val docId = jsValue("docId").as[String].toString
    val flatJson = JsFlattener(jsValue)
    println(flatJson)
    val string = flatJson.value.map(f=> f._1-> f._2.as[String].toString)
    val result = client.execute(indexInto("corpus" / "docs").fields(string) id docId)

    result.map {
      case Left(s) =>  Failure(new Exception(s.error.toString))
      case Right(_) => Success(jsValue)
    }
  }

}
object JsFlattener {

  def apply(js: JsValue): JsObject = flatten(js)

  private [this] def flatten(js: JsValue, prefix: String = ""): JsObject = js.as[JsObject].fields.foldLeft(Json.obj()) {
    case (acc, (k, v: JsObject)) =>
      if(prefix.isEmpty) acc.deepMerge(flatten(v, k))
      else acc.deepMerge(flatten(v, s"$prefix.$k"))

    case (acc, (k, v)) =>
      if(prefix.isEmpty) acc + (k -> v)
      else acc + (s"$prefix.$k" -> v)
  }

  def concat(prefix: String, key: String): String = if(prefix.nonEmpty) s"$prefix.$key" else key

}