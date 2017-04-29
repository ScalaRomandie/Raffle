package org.scalaromandie

import java.security.SecureRandom

import io.circe.generic.auto._
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.blaze.defaultClient

import scala.util.Random
import scalaz._
import Scalaz._

import scalaz.concurrent.Task


/**
  * Select the winner of the Raffle using the meetup api
  *
  */
object Raffle extends App {

  case class Member(name: String, role: Option[String])

  case class RSVP(member: Member, response: String)


  (readParam("apikey") |@| readParam("eventid")) (_ -> _)
  match {

    case Failure(f) => f.foreach(println(_))

    case Success(params) =>
      val (apiKey, eventId) = params
      val client = defaultClient
      val rsvpsTask = {
        val target = (Uri.uri("https://api.meetup.com/Scala-Romandie/events/") / eventId / "rsvps")
          .withQueryParam("key", apiKey)
        client.expect(target)(jsonOf[List[RSVP]])
      }.map { rsvps =>
          new Random(new SecureRandom())
            .shuffle(rsvps)
            .filter(a => a.response == "yes" && a.member.role.isEmpty) /* role is empty for regular members, only present for Organisational roles */
            .take(1)
            .map(_.member.name)
            .foreach(println(_))
      }
        .onFinish(_ => Task(client.shutdownNow()))

      rsvpsTask.run
  }

  def readParam(paramName: String): ValidationNel[String, String] =
    Option(System.getProperty(paramName)).toSuccessNel(s"Missing parameter $paramName")
}
