package com.linecorp.finagle.mysql

import scala.util.Try

trait JsonDecoder[A] {
  def decode(json: String): Try[A]
}

object JsonDecoder {
  def apply[A : JsonDecoder]: JsonDecoder[A] = implicitly[JsonDecoder[A]]

  def instance[A](f: String => Try[A]): JsonDecoder[A] = new JsonDecoder[A] {
    def decode(json: String): Try[A] = f(json)
  }
}
