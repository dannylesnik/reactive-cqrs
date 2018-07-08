package actors

import java.util

import kafka.KafkaConnector
import model.Entities.Event
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}

object Main extends App with KafkaConnector{

  import scala.concurrent.ExecutionContext.Implicits.global


  val event = Event("event1","doc1","something","bla")

  val producerRecord: ProducerRecord[String, Event] = new ProducerRecord[String,Event]("test",event.docId,event)
  val response: util.concurrent.Future[RecordMetadata] =   producer.send(producerRecord)
  val r: RecordMetadata = response.get()
  println(r.checksum())
  println(r.offset())
  println(r.partition())

  concurrent.Future{response.get()}.map(_=> event).foreach(println)

}
