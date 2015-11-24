package com.edinhodzic.service.domain

import scala.beans.BeanProperty

trait Identifiable {
  @BeanProperty var id: String = _
}
