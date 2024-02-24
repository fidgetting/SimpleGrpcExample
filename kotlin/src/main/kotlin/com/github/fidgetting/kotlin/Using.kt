package com.github.fidgetting.kotlin

import java.io.Closeable
import java.util.Collections

object Using {

  class UsingManager: Closeable {
    val closables = Collections.synchronizedList(mutableListOf<Closeable>())

    fun <T: Closeable> using(closable: T): T {
      closables.addFirst(closable)
      return closable
    }

    override fun close() {
      for (closable in closables) {
        closable.close()
      }
    }

  }

  fun <T> using(block: (UsingManager) -> T): T =
    UsingManager().use { manager ->
      block(manager)
    }

}