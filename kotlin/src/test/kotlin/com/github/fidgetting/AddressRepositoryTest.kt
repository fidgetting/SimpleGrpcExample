package com.github.fidgetting

import com.github.fidgetting.AddressOuterClass.Address
import com.github.fidgetting.testutil.DatabaseExtension
import com.github.fidgetting.testutil.DatabaseRepository
import com.github.fidgetting.testutil.mkAddress
import com.github.fidgetting.testutil.mkLong
import com.github.fidgetting.testutil.mkString
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(DatabaseExtension::class)
class AddressRepositoryTest(@DatabaseRepository val repository: AddressRepository) {

  @Test
  fun `able to set and get an address`() = runBlocking {
    val address = mkAddress()
    assertEquals(address, repository.set(address))
    assertEquals(address, repository.get(address.id))
  }

  @Test
  fun `returns null if no address is found`() = runBlocking {
    assertNull(repository.get(0))
  }

  @Test
  fun `get a set of addresses`() = runBlocking {
    val addresses = (1L..4L).associateWith { mkAddress(it) }
    addresses.forEach { repository.set(it.value) }
    assertEquals(setOf(addresses[1], addresses[3]), repository.get(setOf(1, 3)).toSet())
  }

  @Test
  fun `get all addresses`() = runBlocking {
    val addresses = (0..3).map { mkAddress() }.sortedBy { it.id }
    addresses.forEach { repository.set(it) }
    assertEquals(addresses, repository.getAll().toList().sortedBy { it.id })
  }

  @Test
  fun `remove address`() = runBlocking {
    val address = mkAddress()
    assertEquals(address, repository.set(address))
    assertEquals(address, repository.get(address.id))
    assertEquals(address, repository.remove(address.id))
    assertNull(repository.get(address.id))
  }

}