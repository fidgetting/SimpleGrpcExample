package com.github.fidgetting.testutil

import com.github.fidgetting.AddressOuterClass.Address
import com.github.fidgetting.address
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.util.UUID
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import org.mockito.invocation.InvocationOnMock

fun mkLong(): Long = Random.nextLong(Int.MAX_VALUE.toLong())
fun mkUuid(): UUID = UUID.randomUUID()
fun mkString(base: String): String = "${base}_${mkUuid()}"

fun mkAddress(addressId: Long = mkLong()): Address = address {
  id = addressId
  lines.add(mkString("line1"))
  lines.add(mkString("line2"))
  postalCode = mkString("postal-code")
  city = mkString("city")
  state = mkString("state")
  region = mkString("region")
}

suspend fun assertStatus(status: Status.Code, block: suspend () -> Unit): Unit =
  assertEquals(status, assertThrows<StatusRuntimeException> { runBlocking { block() } }.status.code)

inline fun <reified T> InvocationOnMock.get(idx: Int): T =
  getArgument(idx, T::class.java)
