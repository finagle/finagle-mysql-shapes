package com.linecorp.falcon.mysql

import scala.util.Try
import io.circe.Decoder
import io.circe.jawn

package object circe {

  implicit def circeDecoder[A: Decoder]: JsonDecoder[A] = JsonDecoder.instance {
    s => jawn.decode(s).toTry
  }
}
