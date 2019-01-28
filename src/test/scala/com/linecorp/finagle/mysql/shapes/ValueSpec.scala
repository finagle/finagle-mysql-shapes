package com.linecorp.finagle.mysql.shapes

import org.scalatest._
import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._
import com.twitter.finagle.mysql.Parameter._
import com.linecorp.finagle.mysql.shapes.syntax._

class ValueDecoderSpec extends fixture.AsyncFlatSpec with MysqlSuite with Matchers {

  override def populate(data: PreparedStatement) =
    for {
      _ <- data.modify(1, "test", "{}")
      _ <- data.modify(2, "some", """{"foo": "bar", "bar": true}""")
      _ <- data.modify(3, "some", null)
    } yield ()

  it should "decode a column by name" in { client: FixtureParam =>

    val result = client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.get[String]("name")
    }

    fromTwitter(result) map { o =>
      assert(o == List(Success("test")))
    }
  }

  it should "fail to decode a non-existent column" in { client: FixtureParam =>

    val result = client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.get[String]("undefined")
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Failure(_)) =>
      }
    }
  }

  it should "piggyback on existing decoders" in { client: FixtureParam =>

    import java.time.LocalDateTime
    import cats.syntax.functor._

    implicit val decodeMysqlTimestamp: ValueDecoder[LocalDateTime] =
      ValueDecoder.decodeTimestamp.map(_.toLocalDateTime)

    val result = client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.get[LocalDateTime]("create_ts")
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(_)) =>
      }
    }
  }

}
