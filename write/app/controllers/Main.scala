package controllers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import model.Entities.EventElement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App{

  implicit val sys: ActorSystem = ActorSystem("fdfdfdfd")
  implicit val mat: ActorMaterializer = ActorMaterializer()



  val (actorSource, done) = Source.actorRef[String](100, OverflowStrategy.fail).
    conflate { (prior, overflown) =>
      println(s"receieved $prior dropping overflown $overflown")
      prior
    }.buffer(10, OverflowStrategy.backpressure).async
    .mapAsync[String](4) (persist)
    .toMat(Sink.ignore)(Keep.both)
    .run()


  def persist(s:String):Future[String]={
    Future {
      println(s"Persisting String ${s}")
      Thread.sleep(5000)
      s
    }
  }


  for (i <- 1 to 50){
    println(s"sending event ${i}")
    actorSource ! i.toString
  }



}
