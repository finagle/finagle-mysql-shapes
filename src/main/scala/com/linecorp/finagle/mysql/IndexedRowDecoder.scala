package com.linecorp.finagle.mysql

import scala.util.Try
import com.twitter.finagle.mysql.{ Row, Value }

trait IndexedRowDecoder[A] extends RowDecoder[A] {

  def fromValues(values: Seq[Value]): Try[A]

  final def from(row: Row): Try[A] = fromValues(row.values)
}

object IndexedRowDecoder {

  def apply[A : IndexedRowDecoder]: IndexedRowDecoder[A] =
    implicitly[IndexedRowDecoder[A]]

  def instance[A](f: Seq[Value] => Try[A]): IndexedRowDecoder[A] =
    new IndexedRowDecoder[A] {
      def fromValues(values: Seq[Value]): Try[A] = f(values)
    }

}
