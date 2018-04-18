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
  case class Params(meetupName: String, apiKey: String, eventId: String)

  val fetchAttendeesAndDrawWinnerIO = readParams() match {
    case Valid(params) =>
      for {
        url <- IO.pure(Uri
          .uri("https://api.meetup.com") / params.meetupName / "events" / params.eventId / "rsvps"
          withQueryParam ("key", params.apiKey))
        client <- Http1Client[IO]()
        rsvps <- client.expect(url)(jsonOf[IO, List[RSVP]])
        winner <- IO.pure(drawWinner(rsvps))
        _ <- IO { winner.foreach(println(_)) }
      } yield ()

    case Invalid(f) => IO { f.toList.foreach(println(_)) }
  }

  val random = new Random(new SecureRandom())

  def drawWinner(rsvps: List[RSVP]): List[String] = {
    val candidates =
      random
        .shuffle(rsvps)
        .filter(a => a.response == "yes" && a.member.role.isEmpty) /* role is empty for regular members, only present for Organisational roles */
        .map(_.member.name)

    candidates.take(1).map("* " + _) ::: candidates.drop(1)
  }

  def readParams(): ValidatedNel[String, Params] =
    (readParam("meetup").recover { case _ => "Scala-Romandie" },
     readParam("apikey"),
     readParam("eventid")) mapN (Params.apply)

  def readParam(paramName: String): ValidatedNel[String, String] =
    Option(System.getProperty(paramName))
      .toValidNel(s"Missing system property $paramName")

  fetchAttendeesAndDrawWinnerIO.unsafeRunSync
}
