package org.scalaromandie

import java.security.SecureRandom

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.blaze._

import scala.util.Random

/**
  * Select the winner of the Raffle using the meetup api
  *
  */
object Raffle extends App {
  case class Member(name: String, role: Option[String])
  case class RSVP(member: Member, response: String)
  case class Params(apiKey: String, eventId: String)
  val random = new Random(new SecureRandom())

  readParams() match {
    case Valid(params) =>
      val url =
        (Uri.uri("https://api.meetup.com/Scala-Romandie/events/") / params.eventId / "rsvps")
          .withQueryParam("key", params.apiKey)

      Http1Client[IO]()
        .flatMap(_.expect(url)(jsonOf[IO, List[RSVP]]))
        .map { rsvps =>
          val candidates =
            random
              .shuffle(rsvps)
              .filter(a => a.response == "yes" && a.member.role.isEmpty) /* role is empty for regular members, only present for Organisational roles */
              .map(_.member.name)

          candidates.take(1).map("* " + _) ::: candidates.drop(1)
        }
        .map(_.foreach(println(_)))
        .unsafeRunSync

    case Invalid(f) => f.toList.foreach(println(_))
  }

  def readParams(): ValidatedNel[String, Params] =
    (readParam("apikey"), readParam("eventid")) mapN (Params.apply)

  def readParam(paramName: String): ValidatedNel[String, String] =
    Option(System.getProperty(paramName))
      .toValidNel(s"Missing parameter $paramName")
}
