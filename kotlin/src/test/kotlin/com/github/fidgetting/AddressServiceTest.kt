package com.github.fidgetting

import com.github.fidgetting.AddressOuterClass.Address
import com.github.fidgetting.testutil.assertStatus
import com.github.fidgetting.testutil.get
import com.github.fidgetting.testutil.mkAddress
import io.grpc.Status.Code.INTERNAL
import io.grpc.Status.Code.INVALID_ARGUMENT
import io.grpc.Status.Code.NOT_FOUND
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class AddressServiceTest {

  val mockRepo = mock<AddressRepository>()
  val service = AddressService(mockRepo)

  @AfterEach
  fun checkMock() {
    verifyNoMoreInteractions(mockRepo)
  }

  @Test
  fun `get an address`(): Unit = runBlocking {
    val address = mkAddress()

    whenever(mockRepo.get(eq(address.id)))
      .thenReturn(address)

    assertEquals(
      expected = address,
      actual = service.getAddress(getAddressRequest { id = address.id }).address,
    )

    verify(mockRepo, times(1)).get(address.id)
  }

  @Test
  fun `require an id argument be passed`(): Unit = runBlocking {
    assertStatus(INVALID_ARGUMENT) {
      service.getAddress(getAddressRequest {  })
    }
    verifyNoInteractions(mockRepo)
  }

  @Test
  fun `return NOT_FOUND if the address does not exist`(): Unit = runBlocking {
    assertStatus(NOT_FOUND) {
      service.getAddress(getAddressRequest { id = 1L })
    }
    verify(mockRepo, times(1)).get(1L)
  }

  @Test
  fun `get all addresses in request`(): Unit = runBlocking {
    val addresses = loadAddresses()
    val request = getAddressesRequest { ids.addAll(listOf(3, 5)) }

    assertEquals(
      expected = listOf(addresses[3], addresses[5]),
      actual = service.getAddresses(request).toList().map { it.address },
    )

    verify(mockRepo, times(1)).get(setOf(3, 5))
  }

  @Test
  fun `get all addresses`(): Unit = runBlocking {
    val addresses = loadAddresses()
    val request = getAddressesRequest { }

    assertEquals(
      expected = addresses.values.toList(),
      actual = service.getAddresses(request).toList().map { it.address },
    )

    verify(mockRepo, times(1)).getAll()
  }

  @Test
  fun `set address`(): Unit = runBlocking {
    val addresses = loadAddresses()
    val request = setAddressRequest { address = mkAddress() }

    assertEquals(
      expected = request.address,
      actual = service.setAddress(request).address,
    )

    assertEquals(
      expected = request.address,
      actual = addresses[request.address.id],
    )

    verify(mockRepo, times(1)).set(request.address)
  }

  @Test
  fun `set address fails on invalid input`(): Unit = runBlocking {
    assertStatus(INVALID_ARGUMENT) {
      service.setAddress(setAddressRequest { })
    }
  }

  @Test
  fun `set address translates null into INTERNAL`(): Unit = runBlocking {
    val testAddress = mkAddress()
    whenever(mockRepo.set(any<Address>())).thenReturn(null)
    assertStatus(INTERNAL) {
      service.setAddress(setAddressRequest { address = testAddress })
    }
    verify(mockRepo).set(testAddress)
  }

  @Test
  fun `remove address correctly`(): Unit = runBlocking {
    val addresses = loadAddresses()
    val request = removeAddressRequest { id = 3L }

    assertEquals(
      expected = addresses[3L],
      actual = service.removeAddress(request).address,
    )

    assertNull(addresses[3L])

    verify(mockRepo).remove(3L)
  }

  @Test
  fun `remove address fails on invalid request`(): Unit = runBlocking {
    assertStatus(INVALID_ARGUMENT) {
      service.removeAddress(removeAddressRequest { })
    }
  }

  @Test
  fun `remove address does not fail when removing missing address`(): Unit = runBlocking {
    assertEquals(
      expected = Address.getDefaultInstance(),
      actual = service.removeAddress(removeAddressRequest { id = 100L }).address,
    )
    verify(mockRepo, times(1)).remove(100L)
  }

  suspend fun loadAddresses(): Map<Long, Address> {
    val addresses = (1L..10L).associateWith { mkAddress(it) }.toMutableMap()
    whenever(mockRepo.get(any<Long>())).thenAnswer { inv -> addresses[inv.get<Long>(0)] }
    whenever(mockRepo.getAll()).thenReturn(addresses.values.asFlow())
    whenever(mockRepo.get(any<Set<Long>>())).thenAnswer { inv ->
      addresses.filter { inv.get<Set<Long>>(0).contains(it.key) }.values.asFlow()
    }
    whenever(mockRepo.set(any<Address>())).thenAnswer { inv ->
      inv.get<Address>(0).also { addresses[it.id] = it }
    }
    whenever(mockRepo.remove(any<Long>())).thenAnswer { inv ->
      val id = inv.get<Long>(0)
      val result = addresses[id]
      addresses.remove(id)
      result
    }
    return addresses
  }

}