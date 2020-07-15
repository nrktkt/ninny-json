package io.github.kag0.ninny.example

import io.github.kag0.ninny.ast._
import io.github.kag0.ninny._

import scala.util.Success

sealed trait Patch[+A]
case object Clear          extends Patch[Nothing]
case object Nop            extends Patch[Nothing]
case class Update[A](a: A) extends Patch[A]

case class Profile(name: String, email: Option[String], bio: Option[String]) {
  def update(update: UpdateProfile) =
    Profile(
      update.name.getOrElse(name),
      update.email match {
        case Nop       => email
        case Update(v) => Some(v)
        case Clear     => None
      },
      update.bio match {
        case Nop       => bio
        case Update(v) => Some(v)
        case Clear     => None
      }
    )
}

case class UpdateProfile(
    name: Option[String],
    email: Patch[String],
    bio: Patch[String]
)

object Example extends App {

  implicit val profileToJson: ToSomeJson[Profile] = a =>
    obj(
      "name"  -> a.name,
      "email" -> a.email,
      "bio"   -> a.bio
    )

  implicit def patchFromJson[A: FromJson]: FromJson[Patch[A]] = {
    case None           => Success(Nop)
    case Some(JsonNull) => Success(Clear)
    case Some(value)    => value.to[A].map(Update(_))
  }

  implicit val updateProfileFromJson = FromJson.fromSome[UpdateProfile](json =>
    for {
      email <- json.email.to[Patch[String]]
      bio   <- json.bio.to[Patch[String]]
      name  <- json.name.to[Option[String]]
    } yield UpdateProfile(name, email, bio)
  )

  val userProfile = Profile("John Doe", Some("john.doe@example.com"), None)

  val userProfileJson = userProfile.toSomeJson

  println("User profile to JSON AST")
  println(userProfileJson)
  println()

  println("User profile parsed from JSON AST")
  println(userProfileJson.to[UpdateProfile])
  println()

  val profileUpdateJson =
    obj("email" -> JsonNull, "bio" -> "Just a zombie looking for his Jane")

  val profileUpdate = profileUpdateJson.to[UpdateProfile].get
  println("Parsed profile update")
  println(profileUpdate)
  println()

  val updatedProfile = userProfile.update(profileUpdate)

  println("User profile after update")
  println(updatedProfile.toSomeJson)
}
