package com.linecorp.falcon.mysql

import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._
import shapeless._

trait RowImplicits {

  implicit class RichRow(row: Row) {

    def get[A: ValueDecoder](column: String): Try[A] =
      row.apply(column) match {
        case Some(value) => ValueDecoder[A].from(value)
        case None        => ValueDecoder.fail(s"column not found")
      }

    def as[A: RowDecoder]: Try[A] = RowDecoder[A].from(row)

  }

}
