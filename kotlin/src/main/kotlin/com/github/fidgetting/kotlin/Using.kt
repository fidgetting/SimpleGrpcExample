package com.github.fidgetting.kotlin

import io.grpc.Server
import java.io.Closeable
import java.util.Collections

object Using {

  class UsingManager: AutoCloseable {
    val closables = Collections.synchronizedList(mutableListOf<AutoCloseable>())

    fun <T: AutoCloseable> using(closable: T): T {
      closables.addFirst(closable)
      return closable
    }

    fun using(server: Server): Server {
      closables.addFirst(Closeable { server.shutdown() })
      return server
    }

    fun using(f: () -> Unit) {
      closables.addFirst(Closeable { f() })
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