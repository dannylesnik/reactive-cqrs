package com.vanilla.poc.cqrs

import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Keep, RestartFlow, RestartSource, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.{Done, NotUsed}
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


class FlowActor extends Actor with ActorLogging with ElasticSearch {

  val config: Config = context.system.settings.config.getConfig("akka.kafka.consumer")

  val flow: Flow[CommittableMessage[String, String], Done, NotUsed] =
    Flow[CommittableMessage[String,String]].
      map(msg => Event(msg.committableOffset,Success(Json.parse(msg.record.value()))))
    .mapAsync(1) { event => indexEvent(event.json.get).map(f=> event.copy(json = f))}
      .mapAsync(1)(f => {
    f.json match {
      case Success(_)=> f.committableOffset.commitScaladsl()
      case Failure(ex) => throw new StreamFailedException(ex.getMessage,ex)
    }
      })

  val r: Flow[CommittableMessage[String, String], Done, NotUsed] = RestartFlow.onFailuresWithBackoff(
    minBackoff = 3.seconds,
    maxBackoff = 3.seconds,
    randomFactor = 0.2, // adds 20% "noise" to vary the intervals slightly
    maxRestarts = 20 // limits the amount of restarts to 20
  )(() => {
    println("Creating flow")
    flow
  })

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")



  val restartSource: Source[CommittableMessage[String, String], NotUsed] = RestartSource.withBackoff(
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2, // adds 20% "noise" to vary the intervals slightly
    maxRestarts = 20 // limits the amount of restarts to 20
  ) {() =>
    Consumer.committableSource(consumerSettings, Subscriptions.topics("test"))
  }


  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system).withSupervisionStrategy { e =>
    Supervision.Stop
  })



  restartSource
    .via(flow).recoverWithRetries(100,{
    case _:Throwable => restartSource.via(flow)
  })
    .toMat(Sink.ignore)(Keep.both).run()

  override def receive: Receive = Actor.emptyBehavior


  case class Event(committableOffset:CommittableOffset, json:Try[JsValue])

}


class StreamFailedException(msg:String, ex:Throwable) extends Throwable{}
