package test.support

import play.api.test._

import test.support._

abstract class WithFakeUsernamerApplication(
  override val app: FakeApplication = new FakeUsernamerApplication
) extends WithApplication {}