package com.orcchg.trustwallet.task.data.local

import com.orcchg.trustwallet.task.data.local.model.DatabaseKey
import com.orcchg.trustwallet.task.data.local.model.DatabaseValue
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class TestInMemoryDatabase {
    private val sut: Database = InMemoryDatabase()

    private suspend fun fillStorage(size: Long = 9): Long {
        for (i in 1..size) {
            sut.set(key = "key_$i", value = "value_$i")
        }
        return size
    }

    @BeforeTest
    fun setUp() = runTest {
        sut.clear()
    }

    @Test
    fun `InMemoryDatabase size - empty storage - 0`() = runTest {
        // Arrange

        // Act
        val size = sut.size()

        // Assert
        assertEquals(size, 0)
    }

    @Test
    fun `InMemoryDatabase size - not empty storage - not 0`() = runTest {
        // Arrange
        val oldSize = fillStorage()

        // Act
        val size = sut.size()

        // Assert
        assertEquals(size, oldSize)
    }

    @Test
    fun `InMemoryDatabase get - empty storage - null`() = runTest {
        // Arrange
        val key: DatabaseKey = "key"

        // Act
        val value = sut.get(key = key)

        // Assert
        assertNull(value)
    }

    @Test
    fun `InMemoryDatabase get - not empty storage, missing key - null`() = runTest {
        // Arrange
        val key: DatabaseKey = "missing key"

        fillStorage()

        // Act
        val value = sut.get(key = key)

        // Assert
        assertNull(value)
    }

    @Test
    fun `InMemoryDatabase get - not empty storage, existing key - value`() = runTest {
        // Arrange
        val key: DatabaseKey = "key_5"

        fillStorage()

        // Act
        val value = sut.get(key = key)

        // Assert
        assertNotNull(value)
        assertEquals(value, "value_5")
    }

    @Test
    fun `InMemoryDatabase set - empty storage - single key`() = runTest {
        // Arrange
        val key: DatabaseKey = "key"
        val value: DatabaseValue = "value"

        // Act
        sut.set(key = key, value = value)

        // Assert
        val size = sut.size()
        val storedValue = sut.get(key = key)

        assertEquals(size, 1)
        assertNotNull(storedValue)
        assertEquals(storedValue, value)
    }

    @Test
    fun `InMemoryDatabase set - empty storage, value null - empty storage`() = runTest {
        // Arrange
        val key: DatabaseKey = "key"

        // Act
        sut.set(key = key, value = null)

        // Assert
        val size = sut.size()
        val storedValue = sut.get(key = key)

        assertEquals(size, 0)
        assertNull(storedValue)
    }

    @Test
    fun `InMemoryDatabase set - not empty storage - added key`() = runTest {
        // Arrange
        val key: DatabaseKey = "key"
        val value: DatabaseValue = "value"

        val oldSize = fillStorage()

        // Act
        sut.set(key = key, value = value)

        // Assert
        val size = sut.size()
        val storedValue = sut.get(key = key)

        assertEquals(size, oldSize + 1) // newly added key
        assertNotNull(storedValue)
        assertEquals(storedValue, value)
    }

    @Test
    fun `InMemoryDatabase set - not empty storage, key already exists - overwritten key`() = runTest {
        // Arrange
        val key: DatabaseKey = "key_5"
        val value: DatabaseValue = "new_value_5"

        val oldSize = fillStorage()

        // Act
        sut.set(key = key, value = value)

        // Assert
        val size = sut.size()
        val storedValue = sut.get(key = key)

        assertEquals(size, oldSize) // no added keys
        assertNotNull(storedValue)
        assertEquals(storedValue, value)
    }

    @Test
    fun `InMemoryDatabase set - not empty storage, key already exists, value null - key removed`() = runTest {
        // Arrange
        val key: DatabaseKey = "key_5"

        val oldSize = fillStorage()

        // Act
        sut.set(key = key, value = null)

        // Assert
        val size = sut.size()
        val storedValue = sut.get(key = key)

        assertEquals(size, oldSize - 1) // removed key
        assertNull(storedValue)
    }

    @Test
    fun `InMemoryDatabase count - empty storage - 0`() = runTest {
        // Arrange
        val value: DatabaseValue = "value"

        // Act
        val count = sut.count(value = value)

        // Assert
        assertEquals(count, 0)
    }

    @Test
    fun `InMemoryDatabase count - not empty storage, no matching values - 0`() = runTest {
        // Arrange
        val value: DatabaseValue = "value"

        fillStorage()

        // Act
        val count = sut.count(value = value)

        // Assert
        assertEquals(count, 0)
    }

    @Test
    fun `InMemoryDatabase count - not empty storage, with matching values - not 0`() = runTest {
        // Arrange
        val value: DatabaseValue = "value_5"

        fillStorage()

        // Act
        val count = sut.count(value = value)

        // Assert
        assertEquals(count, 1)
    }

    @Test
    fun `InMemoryDatabase count - not empty storage, with multiple matching values - not 0`() = runTest {
        // Arrange
        val value: DatabaseValue = "value_5"

        for (i in 1..9) {
            sut.set(key = "key_X_$i", value = "value_$i")
            sut.set(key = "key_Y_$i", value = "value_$i")
            sut.set(key = "key_Z_$i", value = "value_$i")
        }

        // Act
        val count = sut.count(value = value)

        // Assert
        assertEquals(count, 3)
    }

    @Test
    fun `InMemoryDatabase findByValue - empty storage - empty map`() = runTest {
        // Arrange
        val value: DatabaseValue = "value"

        // Act
        val found = sut.findByValue(value = value)

        // Assert
        assertTrue { found.isEmpty() }
    }

    @Test
    fun `InMemoryDatabase findByValue - not empty storage, missing value - empty map`() = runTest {
        // Arrange
        val value: DatabaseValue = "value"

        fillStorage()

        // Act
        val found = sut.findByValue(value = value)

        // Assert
        assertTrue { found.isEmpty() }
    }

    @Test
    fun `InMemoryDatabase findByValue - not empty storage, existing value - not empty map`() = runTest {
        // Arrange
        val value: DatabaseValue = "value_5"

        fillStorage()

        // Act
        val found = sut.findByValue(value = value)

        // Assert
        assertTrue { found.isNotEmpty() }
        assertEquals(found, mapOf("key_5" to "value_5"))
    }

    @Test
    fun `InMemoryDatabase findByValue - not empty storage, existing values - not empty map`() = runTest {
        // Arrange
        val value: DatabaseValue = "value_5"

        for (i in 1..9) {
            sut.set(key = "key_X_$i", value = "value_$i")
            sut.set(key = "key_Y_$i", value = "value_$i")
            sut.set(key = "key_Z_$i", value = "value_$i")
        }

        // Act
        val found = sut.findByValue(value = value)

        // Assert
        assertTrue { found.isNotEmpty() }
        assertEquals(
            found,
            mapOf(
                "key_X_5" to "value_5",
                "key_Y_5" to "value_5",
                "key_Z_5" to "value_5"
            )
        )
    }

    @Test
    fun `InMemoryDatabase delete - empty storage - empty storage`() = runTest {
        // Arrange
        val key: DatabaseKey = "key"

        // Act
        sut.delete(key = key)

        // Assert
        assertEquals(sut.size(), 0)
    }

    @Test
    fun `InMemoryDatabase delete - not empty storage, key not match - key not removed`() = runTest {
        // Arrange
        val key: DatabaseKey = "key"

        val oldSize = fillStorage()

        // Act
        sut.delete(key = key)

        // Assert
        val value = sut.get(key)

        assertEquals(sut.size(), oldSize)
        assertNull(value)
    }

    @Test
    fun `InMemoryDatabase delete - not empty storage, key matches - key removed`() = runTest {
        // Arrange
        val key: DatabaseKey = "key_5"

        val oldSize = fillStorage()

        // Act
        sut.delete(key = key)

        // Assert
        val value = sut.get(key)

        assertEquals(sut.size(), oldSize - 1)
        assertNull(value)
    }

    @Test
    fun `InMemoryDatabase clear - empty storage - empty storage`() = runTest {
        // Arrange

        // Act
        sut.clear()

        // Assert
        assertEquals(sut.size(), 0)
    }

    @Test
    fun `InMemoryDatabase clear - not empty storage - empty storage`() = runTest {
        // Arrange
        fillStorage()

        // Act
        sut.clear()

        // Assert
        assertEquals(sut.size(), 0)
    }
}
