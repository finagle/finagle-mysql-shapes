package com.linecorp.falcon.mysql

import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._
import shapeless._

package object syntax {

  implicit class RichRow(val row: Row) extends AnyVal {

    def get[A: ValueDecoder](column: String): Try[A] =
      row.apply(column) match {
        case Some(value) => ValueDecoder[A].from(value)
        case None        => ValueDecoder.fail(s"column not found")
      }

    def as[A: RowDecoder]: Try[A] = RowDecoder[A].from(row)

  }

}
