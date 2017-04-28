package org.scalaromandie

import java.security.SecureRandom

import io.circe.generic.auto._
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.blaze.defaultClient

import scala.util.Random

/**
  * Select the winner of the Raffle using the meetup api
  *
  */
object Raffle extends App {

  case class Member(name: String, role: Option[String])
  case class RSVP(member: Member, response: String)

  Option(System.getProperty("apikey")) match {
    case None => println("An api is required")

    case Some(apiKey) =>
      val client = defaultClient
      val rsvpsTask = {
        val eventId = "239576171"
        val target = (Uri.uri("https://api.meetup.com/Scala-Romandie/events/") / eventId / "rsvps")
          .withQueryParam("key", apiKey)
        client.expect(target)(jsonOf[List[RSVP]])
      }

      val rsvps = rsvpsTask.run
      val winners =
        new Random(new SecureRandom()).shuffle(rsvps)
          .filter(a => a.response == "yes" && a.member.role.isEmpty) /* role is empty for regular members, only present for Organisational roles */
          .take(1)
          .map(_.member.name)
          .mkString("\n")

      println(winners)

      client.shutdownNow()
  }
}
