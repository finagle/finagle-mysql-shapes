package com.linecorp.falcon.mysql

import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._
import java.nio.charset.StandardCharsets._
import io.circe.{Decoder => JsonDecoder}
import io.circe.parser._
import cats.syntax.either._
import cats.Functor

class ValueException(s: String) extends RuntimeException(s)

trait ValueDecoder[T] {
  def from(value: Value): Try[T]
}

object ValueDecoder {

  def apply[A : ValueDecoder]: ValueDecoder[A] = implicitly[ValueDecoder[A]]

  def instance[A](f: Value => Try[A]): ValueDecoder[A] = new ValueDecoder[A] {
    def from(value: Value): Try[A] = f(value)
  }

  def fail(s: String) = Failure(new ValueException(s))

  implicit val functor: Functor[ValueDecoder] = new Functor[ValueDecoder] {
    def map[A, B](d: ValueDecoder[A])(f: A => B): ValueDecoder[B] = ValueDecoder.instance {
      d.from(_).map(f)
    }
  }

  implicit val decodeInt: ValueDecoder[Int] = new ValueDecoder[Int] {
    def from(value: Value): Try[Int] = value match {
      case IntValue(x) => Success(x)
      case _           => fail("failed to read int")
    }
  }

  implicit val decodeString: ValueDecoder[String] = new ValueDecoder[String] {
    def from(value: Value): Try[String] = value match {
      case StringValue(x) => Success(x)
      case _              => fail("failed to read int")
    }
  }

  // Option

  implicit def decodeOption[T: ValueDecoder]: ValueDecoder[Option[T]] =
    new ValueDecoder[Option[T]] {
      def from(value: Value): Try[Option[T]] = value match {
        case NullValue => Success(None)
        case _         => ValueDecoder[T].from(value).map(Some(_))
      }
    }


  // JSON

  private[this] val JsonType: Short = 0xf5

  implicit def decodeJson[T: JsonDecoder] = ValueDecoder.instance {
    case RawValue(JsonType, Charset.Binary, _, bytes) =>
      decode[T](new String(bytes, UTF_8)).toTry
    case StringValue(str) =>
      decode[T](str).toTry
    case _ => fail("failed to read json")
  }

}
