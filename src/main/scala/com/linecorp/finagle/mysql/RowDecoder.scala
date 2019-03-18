package com.linecorp.finagle.mysql

import scala.util.{ Try, Success, Failure }
import com.twitter.finagle.mysql.Row

class RowException(s: String) extends RuntimeException(s)

trait RowDecoder[A] {
  def from(row: Row): Try[A]
}

object RowDecoder {

  def apply[A : RowDecoder]: RowDecoder[A] = implicitly[RowDecoder[A]]

  def instance[A](f: Row => Try[A]): RowDecoder[A] = new RowDecoder[A] {
    def from(row: Row): Try[A] = f(row)
  }

  def fail(s: String) = Failure(new RowException(s))
}

