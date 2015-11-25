package com.edinhodzic.service.util

// TODO maybe replace this with Casbah's custom conversion helper, see https://mongodb.github.io/casbah/guide/serialisation.html ?
trait Converter[A, B] {

  def serialise(subject: A): B

  def deserialise(subject: B): A

}
