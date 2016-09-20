package org.scalaromandie

import java.security.SecureRandom

import io.circe.generic.auto._
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.blaze.defaultClient

import scala.util.Random

/**
  * Select the two winners of the Raffle using the meetup api
  *
  */
object Raffle extends App {
  val client = defaultClient

  case class Member(name: String, role: Option[String])

  case class RSVP(member: Member, response: String)

  val apiKey = System.getProperty("apikey")

  val rsvpsTask = {
    val eventId = "234061340"
    val target = (Uri.uri("http://api.meetup.com/Scala-Romandie/events/") / eventId / "rsvps")
      .withQueryParam("key", apiKey)
    client.expect(target)(jsonOf[List[RSVP]])
  }

  val rsvps = rsvpsTask.run
  val winners =
     new Random(new SecureRandom()).shuffle(rsvps)
      .filter(a => a.response == "yes" && a.member.role.isEmpty) /* role is empty for regular members, only present for Organisational roles */
      .take(2)
      .map(_.member.name)
      .mkString("\n")

  println(winners)

  client.shutdownNow()

}
