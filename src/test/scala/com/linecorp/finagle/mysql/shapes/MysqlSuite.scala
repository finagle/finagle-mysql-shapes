package com.linecorp.finagle.mysql

import com.dimafeng.testcontainers.{ ForAllTestContainer, MySQLContainer }
import org.scalatest._
import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{ Client => MysqlClient, PreparedStatement }
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

  val databaseName = "mysql-test"

  override val container = MySQLContainer(
    mysqlImageVersion = "mysql:5.7",
    databaseName = databaseName
  )

  def schema: String =
    """CREATE TABLE test
         (
           id        SERIAL,
           name      VARCHAR(40) NOT NULL,
           metadata  JSON,
           fruit     ENUM('Kiwi', 'Melon', 'Mango', 'Apple'),
           create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         )
       engine=innodb
       DEFAULT charset=utf8"""

  def populate(data: PreparedStatement): TwitterFuture[Unit]

  type FixtureParam = MysqlClient

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val url = new URI(container.jdbcUrl.substring(5))

    val client: MysqlClient = Mysql.client
      .withCredentials(container.username, container.password)
      .withDatabase(databaseName)
      .newRichClient(s"${url.getHost}:${url.getPort}")

    val data =
      client.prepare("INSERT INTO test (id, name, metadata, fruit) VALUES(?, ?, ?, ?)")

    Await.result(client.modify(schema))
    Await.result(populate(data))

    try {
      self.withFixture(test.toNoArgAsyncTest((client)))
    } finally {
      for {
        _ <- client.modify("DROP table test")
        _ <- client.close()
      } yield ()
    }
  }
}
