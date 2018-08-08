package com.linecorp.falcon.mysql

import scala.util.{ Try, Success, Failure }
import com.twitter.finagle.mysql._
import shapeless._

class RowException(s: String) extends RuntimeException(s)

trait RowDecoder[T] {
  def from(row: Seq[Value]): Try[T]
}

object RowDecoder {

  def apply[A : RowDecoder]: RowDecoder[A] = implicitly[RowDecoder[A]]

  def instance[A](f: Seq[Value] => Try[A]): RowDecoder[A] =
    new RowDecoder[A] {
      def from(row: Seq[Value]): Try[A] = f(row)
    }

  def fail(s: String) = Failure(new RowException(s))

  // HList

  implicit def deriveHNil: RowDecoder[HNil] = RowDecoder.instance {
    _.headOption match {
      case None => Success(HNil)
      case _    => fail("Cannot convert to HNil")
    }
  }

  implicit def deriveHCons[V, T <: HList]
    (implicit rv: Lazy[ValueDecoder[V]], rt: Lazy[RowDecoder[T]])
    : RowDecoder[V :: T] = RowDecoder.instance {
      case x +: xs => for {
        front <- rv.value.from(x)
        back  <- rt.value.from(xs)
      } yield front :: back

      case _       => fail("Cannot convert to HList")
    }

  // Anything with a Generic

  implicit def deriveClass[A, R]
    (implicit gen: Generic.Aux[A, R], decoder: RowDecoder[R])
    : RowDecoder[A] = RowDecoder.instance {
      decoder.from(_).map(gen.from)
    }
}
