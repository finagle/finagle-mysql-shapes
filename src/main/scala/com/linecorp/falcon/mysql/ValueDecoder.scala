package com.linecorp.falcon.mysql

import scala.util.{ Try, Success, Failure }
import com.twitter.finagle.mysql._
import java.nio.charset.StandardCharsets._
import cats.syntax.either._
import cats.Functor
import java.sql.{ Date, Timestamp }

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
    def map[A, B](d: ValueDecoder[A])(f: A => B): ValueDecoder[B] = instance {
      d.from(_).map(f)
    }
  }

  implicit val decodeByte: ValueDecoder[Byte] = instance {
    case ByteValue(b) => Success(b)
    case _ => fail("failed to decode Byte")
  }

  implicit val decodeBoolean: ValueDecoder[Boolean] = instance {
    case ByteValue(v)  => Success(v != 0)
    case _ => fail("failed to decode boolean")
  }

  implicit val decodeShort: ValueDecoder[Short] = instance {
    case ShortValue(s) => Success(s)
    case _ => fail("failed to decode Short")
  }

  implicit val decodeInt: ValueDecoder[Int] = instance {
    case IntValue(i) => Success(i)
    case _ => fail("failed to decode Int")
  }

  implicit val decodeLong: ValueDecoder[Long] = instance {
    case LongValue(l) => Success(l)
    case _ => fail("failed to decode Long")
  }

  implicit val decodeBigInt: ValueDecoder[BigInt] = instance {
    case BigIntValue(bi) => Success(bi)
    case _ => fail("failed to decode BigInt")
  }

  implicit val decodeFloat: ValueDecoder[Float] = instance {
    case FloatValue(f) => Success(f)
    case _ => fail("failed to decode Float")
  }

  implicit val decodeDouble: ValueDecoder[Double] = instance {
    case DoubleValue(d) => Success(d)
    case _ => fail("failed to decode Double")
  }

  implicit val decodeString: ValueDecoder[String] = instance {
    case StringValue(s) => Success(s)
    case _ => fail("failed to decode String")
  }

  implicit val decodeDate: ValueDecoder[Date] = instance {
    DateValue.unapply(_) match {
      case Some(date) => Success(date)
      case None => ValueDecoder.fail("failed to decode java.sql.Date")
    }
  }

  implicit val decodeTimestamp: ValueDecoder[Timestamp] = instance {
    TimestampValue.unapply(_) match {
      case Some(timestamp) => Success(timestamp)
      case None => ValueDecoder.fail("failed to decode java.sql.Timestamp")
    }
  }

  implicit val decodeBigDecimal: ValueDecoder[BigDecimal] = instance {
    BigDecimalValue.unapply(_) match {
      case Some(b) => Success(b)
      case None => ValueDecoder.fail("failed to decode java.sql.BigDecimal")
    }
  }

  // Binary

  private def isBinary(`type`: Short) =
    Type.TinyBlob to Type.String contains `type`

  implicit val decodeBinary: ValueDecoder[Array[Byte]] = instance {
    case RawValue(typ, _, _, bytes) if isBinary(typ) => Success(bytes)
    case EmptyValue => Success(Array.emptyByteArray)
    case _ => fail("failed to decode binary blob")
  }

  // Option

  implicit def decodeOption[T: ValueDecoder]: ValueDecoder[Option[T]] =
    new ValueDecoder[Option[T]] {
      def from(value: Value): Try[Option[T]] = value match {
        case NullValue => Success(None)
        case _ => ValueDecoder[T].from(value).map(Some(_))
      }
    }

  // JSON

  private[this] val JsonType: Short = 0xf5

  implicit def decodeJson[T: JsonDecoder] = instance {
    case RawValue(JsonType, Charset.Binary, _, bytes) =>
      JsonDecoder[T].decode(new String(bytes, UTF_8))
    case StringValue(str) =>
      JsonDecoder[T].decode(str)
    case _ => fail("failed to read json")
  }

}
