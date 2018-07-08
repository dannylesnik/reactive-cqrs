package kafka

import java.util

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer

class KafkaJsonSerializer[T] extends Serializer[T]{

  val objectMapper = new ObjectMapper()


  override def configure (configs: util.Map[String, _], isKey: Boolean): Unit = {
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
    objectMapper.registerModule(DefaultScalaModule)
  }




  override def close (): Unit = ()

  override def serialize (topic: String, data: T): Array[Byte] = {

    if (data==null)
      return null

    try {
      objectMapper.writeValueAsBytes(data)
    }
    catch {
      case e: Exception =>
        throw new SerializationException("Error serializing JSON message", e)
    }
  }
}
