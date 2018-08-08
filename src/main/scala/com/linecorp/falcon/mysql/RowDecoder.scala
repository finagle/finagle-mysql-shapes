package com.linecorp.falcon.mysql

import scala.util.{ Try, Success, Failure }
import com.twitter.finagle.mysql._
import shapeless._

class RowException(s: String) extends RuntimeException(s)

trait RowDecoder[T] {
  def from(row: Row): Try[T]
}

object RowDecoder {

  def apply[A : RowDecoder]: RowDecoder[A] = implicitly[RowDecoder[A]]

  def instance[A](f: Row => Try[A]): RowDecoder[A] = new RowDecoder[A] {
    def from(row: Row): Try[A] = f(row)
  }

  def fail(s: String) = Failure(new RowException(s))

  // HList

  implicit def deriveHNil: RowDecoder[HNil] = RowDecoder.instance {
    _.values.headOption match {
      case None => Success(HNil)
      case _    => fail("Cannot convert to HNil")
    }
  }

}
