package kafka

import java.util
import java.util.Properties
import model.Entities.Event
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import scala.concurrent.ExecutionContext.Implicits.global

trait KafkaConnector {


  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("acks", "all")
  props.put("retries", 2.toString)
  props.put("batch.size", 16384.toString)
  props.put("linger.ms", 1.toString)
  props.put("buffer.memory", 33554432.toString)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "kafka.KafkaJsonSerializer")
  props.put("request.timeout.ms", 1000.toString)
  props.put("max.block.ms",1000.toString)

  val producer = new KafkaProducer[String, Event](props)

  def deliverToKafka(event: Event)(topic:String):scala.concurrent.Future[Event] = {
   val producerRecord: ProducerRecord[String, Event] = new ProducerRecord[String,Event](topic,event.docId,event)
    val response: util.concurrent.Future[RecordMetadata] =   producer.send(producerRecord)
    concurrent.Future{response.get()}.map(_=> event)

  }

}



/*
for( i <- 0 until 2){
  val student = Student("ida" + i,"Danny","Lesnik")
  val data = new ProducerRecord[String, Student]("mytopic", student.id, student)
  producer.send(data)
}

  producer.close()
}

*/