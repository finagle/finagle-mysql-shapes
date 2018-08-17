package com.linecorp.falcon.mysql

import org.scalatest._
import scala.util.{Try, Success, Failure}
import org.scalatest.concurrent.ScalaFutures
import com.linecorp.falcon.mysql.syntax._

class ValueDecoderSpec extends fixture.AsyncFlatSpec with MysqlSuite with Matchers {

  it should "decode a column by name" in { f: FixtureParam =>

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.get[String]("name")
    }

    fromTwitter(result) map { o =>
      assert(o == List(Success("test")))
    }
  }

  it should "fail to decode a non-existent column" in { f: FixtureParam =>

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.get[String]("undefined")
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Failure(_)) =>
      }
    }
  }

}
