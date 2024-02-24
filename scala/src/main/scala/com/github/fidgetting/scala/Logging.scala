package com.github.fidgetting.scala

import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag

object Logging {

  def loggerOf[T](implicit ct: ClassTag[T]): Logger =
    LoggerFactory.getLogger(ct.runtimeClass)

}
