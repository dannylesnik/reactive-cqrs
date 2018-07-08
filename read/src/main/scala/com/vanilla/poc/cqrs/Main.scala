package com.vanilla.poc.cqrs

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}

object Main extends App {

  println("Starting CQRS Read!!!")

  val appName = "Cqrs-Read"

  val system = ActorSystem(appName)

  var cqrsReads = CQRSReaad(system)
}


  object CQRSReaad extends ExtensionId[CQRSReaad] with ExtensionIdProvider {

    override def lookup: ExtensionId[_ <: Extension] = CQRSReaad

    override def createExtension(system: ExtendedActorSystem) = new CQRSReaad(system)




  }

  class CQRSReaad(system: ExtendedActorSystem) extends Extension {

    val flowActor: ActorRef = system.actorOf(Props[FlowActor],"flowActor")

    system.registerOnTermination(shutdown())

    def shutdown ():Unit = {

      println("Shutting Down CQRS Read")

    }


  }





