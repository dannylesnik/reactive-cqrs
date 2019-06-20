package com.vanilla.poc.cqrs.read

import org.specs2.mutable.Specification

class MyTest extends Specification {

  "myTest" should{
    "1 = 1 " in{
       true shouldEqual  true
    }

    "true = true" in{
      1 shouldEqual  1
    }
  }

}
