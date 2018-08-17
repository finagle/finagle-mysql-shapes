package com.linecorp.falcon.mysql

import com.dimafeng.testcontainers.{ ForAllTestContainer, MySQLContainer }
import org.scalatest._
import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{ Client => MysqlClient }
import com.twitter.finagle.mysql.Parameter._
import com.twitter.util.Await
import java.net.URI
import scala.concurrent.{Future, Promise}
import com.twitter.util.{Return, Throw, Future => TwitterFuture}


trait MysqlSuite extends ForAllTestContainer { self: fixture.AsyncTestSuite =>

  implicit def fromTwitter[A](twitterFuture: TwitterFuture[A]): Future[A] = {
    val promise = Promise[A]()
    twitterFuture respond {
      case Return(a) => promise success a
      case Throw(e) => promise failure e
    }
    promise.future
  }

  val schema =
    """CREATE TABLE test
          (
            id   SERIAL,
            name VARCHAR(40) NOT NULL,
            data JSON
          )
        engine=innodb
        DEFAULT charset=utf8"""

  val databaseName = "mysql-test"

  override val container = MySQLContainer(
    mysqlImageVersion = "mysql:5.7",
    databaseName = databaseName
  )

  case class FixtureParam(client: MysqlClient)

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val url = new URI(container.jdbcUrl.substring(5))

    val client: MysqlClient = Mysql.client
      .withCredentials(container.username, container.password)
      .withDatabase(databaseName)
      .newRichClient(s"${url.getHost}:${url.getPort}")

    Await.result(client.modify(schema))

    val preparedStatement = client.prepare("INSERT INTO test VALUES(?, ?, ?)")

    val result = for {
      _ <- preparedStatement.modify(1, "test", "{}")
      _ <- preparedStatement.modify(2, "some", """{"foo": "bar", "bar": true}""")
    } yield ()

    Await.result(result)

    try {
      self.withFixture(test.toNoArgAsyncTest(FixtureParam(client)))
    } finally {
      for {
        _ <- client.modify("DROP table test")
        _ <- client.close()
      } yield ()
    }
  }
}
