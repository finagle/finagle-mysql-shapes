package com.linecorp.falcon.mysql

import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._

trait RowDecoder[T] {
  def from(row:Row): Try[T]
}

object RowDecoder {
  def apply[A : RowDecoder]: RowDecoder[A] = implicitly[RowDecoder[A]]
}
