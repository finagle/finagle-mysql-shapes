package com.linecorp.falcon.mysql

import org.scalatest._
import com.twitter.finagle.mysql.{ Client => MysqlClient }
import scala.util.{Try, Success, Failure}
import org.scalatest.concurrent.ScalaFutures
import io.circe.{ Decoder, Encoder, Json }

class RowDecoderSpec extends fixture.AsyncFlatSpec with MysqlSuite with Matchers {

  case class Data(foo: String, bar: Boolean)

  implicit val decodeData: Decoder[Data] =
    Decoder.forProduct2("foo", "bar")(Data.apply)

  implicit val encodeData: Encoder[Data] =
    Encoder.forProduct2("foo", "bar")(d => (d.foo, d.bar))

  it should "decode a row into a case class" in { f: FixtureParam =>

    import com.linecorp.falcon.mysql.generic._

    case class Foo(id: Long, name: String)

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      assert(o == List(Success(Foo(1, "test"))))
    }
  }

  it should "decode a row into a tuple" in { f: FixtureParam =>

    import com.linecorp.falcon.mysql.generic.tuples._

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.as[(Long, String, Json)]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success((_,_,_))) =>
      }

    }
  }

  it should "decode a row with Json using an implicit decoder" in { f: FixtureParam =>

    import com.linecorp.falcon.mysql.generic._

    case class Foo(id: Long, name: String, data: Data)

    val result = f.client.select("SELECT * FROM test WHERE id = 2") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,_,Data(_,_)))) =>
      }

    }
  }

  it should "decode a row with custom decoder" in { f: FixtureParam =>

    case class Foo(id: Long, name: String, data: Data)

    implicit val decoder: RowDecoder[Foo] = RowDecoder.instance { row =>
      for {
        id   <- row.get[Long]("id")
        name <- row.get[String]("name")
        data <- row.get[Data]("data")
      } yield Foo(id, name, data)
    }

    val result = f.client.select("SELECT * FROM test WHERE id = 2") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,_,Data(_,_)))) =>
      }

    }
  }

}
