package test.support

import play.api.test._

class FakeUsernamerApplication(
  override val additionalConfiguration: Map[String, _ <: Any] = Map(
    // Use a separate data store.
    "mongodb.uri" -> "mongodb://localhost:27017/usernamer",
    "embed.mongo.enabled" -> true,
    "embed.mongo.port" -> 27017,
    "embed.mongo.dbversion" -> "2.4.4"
  )
) extends FakeApplication {}