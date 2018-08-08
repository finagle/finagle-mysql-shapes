package com.linecorp.falcon.mysql

import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._

trait RowImplicits {

  implicit class RichRow(row: Row) {

    def get[T: ValueDecoder](column: String): Try[T] =
      row.indexOf(column) match {
        case Some(x) => ValueDecoder[T].from(row.values(x))
        case None    => ValueDecoder.fail(s"column not found")
      }

    def as[T: RowDecoder]: Try[T] = RowDecoder[T].from(row)

  }

}
