package com.github.fidgetting.kotlin

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging {
  inline fun <reified T> loggerOf(): Logger =
    LoggerFactory.getLogger(T::class.java)
}