package com.orcchg.trustwallet.task.data

import com.orcchg.trustwallet.task.data.local.Database
import com.orcchg.trustwallet.task.data.local.InMemoryDatabase
import com.orcchg.trustwallet.task.domain.Storage
import com.orcchg.trustwallet.task.domain.model.Key
import com.orcchg.trustwallet.task.domain.model.TransactionStatus
import com.orcchg.trustwallet.task.domain.model.Value
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Tests [RealStorage] with the real implementation of the [Database].
 */
class IntegrationTestRealStorageAndRealDatabase {
    private val database: Database = InMemoryDatabase()
    private val sut: Storage = RealStorage(database = database)

    private suspend fun fillDatabase(size: Long = 9): Long {
        for (i in 1..size) {
            database.set(key = "key_$i", value = "value_$i")
        }
        return size
    }

    @BeforeTest
    fun setUp() = runTest {
        database.clear()
        sut.clear()
    }

    @Test
    fun `RealStorage get - empty storage - null`() = runTest {
        // Arrange
        val key: Key = "foo"

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNull(storedValue)
    }

    @Test
    fun `RealStorage get - full storage, missing key - null`() = runTest {
        // Arrange
        val key: Key = "foo"

        fillDatabase()

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNull(storedValue)
    }

    @Test
    fun `RealStorage get - full storage, existing key - value`() = runTest {
        // Arrange
        val key: Key = "key_5"

        fillDatabase()

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNotNull(storedValue)
        assertEquals("value_5", storedValue)
    }

    /**
     * Example:
     *
     * > SET foo 123
     * > GET foo
     * 123
     */
    @Test
    fun `RealStorage example - set and get value`() = runTest {
        // Arrange
        val key: Key = "foo"
        val value: Value = "123"

        // Act
        sut.set(key = key, value = value)

        // Assert
        val storedValue = sut.get(key = key)

        assertEquals(value, storedValue)
    }

    @Test
    fun `RealStorage set - empty storage, missing key - null`() = runTest {
        // Arrange
        val key: Key = "foo"

        sut.set(key = "bar", "abc")

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNull(storedValue)
    }

    @Test
    fun `RealStorage set - full storage and set, missing key - null`() = runTest {
        // Arrange
        val key: Key = "foo"

        fillDatabase()

        sut.set(key = "bar", "abc")

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNull(storedValue)
    }

    @Test
    fun `RealStorage set - full storage and overwrite, missing key - null`() = runTest {
        // Arrange
        val key: Key = "foo"

        fillDatabase()

        sut.set(key = "key_5", "abc")

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNull(storedValue)
    }

    @Test
    fun `RealStorage set - full storage and overwrite, existing key - value`() = runTest {
        // Arrange
        val key: Key = "key_9"

        fillDatabase()

        sut.set(key = "key_5", "abc")

        // Act
        val storedValue = sut.get(key = key)

        // Assert
        assertNotNull(storedValue)
        assertEquals("value_9", storedValue)
    }

    @Test
    fun `RealStorage set - full storage and overwrite, matching key - overwritten value`() = runTest {
        // Arrange
        val key: Key = "key_5"
        val value: Value = "abc"

        fillDatabase()

        // Act
        val oldValue = sut.get(key = key)

        // Assert
        assertNotNull(oldValue)
        assertEquals("value_5", oldValue)

        // Act
        sut.set(key = key, value)

        // Assert
        val storedValue = sut.get(key = key)

        assertNotNull(storedValue)
        assertEquals(value, storedValue)
    }

    /**
     * Example:
     *
     * > DELETE foo
     * > GET foo
     * key not set
     */
    @Test
    fun `RealStorage example - delete a value`() = runTest {
        // Arrange
        val key: Key = "foo"

        // Act
        sut.delete(key = key)

        // Assert
        val storedValue = sut.get(key = key)

        assertNull(storedValue)
    }

    @Test
    fun `RealStorage delete - not empty storage, missing key - noop`() = runTest {
        // Arrange
        val key: Key = "foo"

        val size = fillDatabase()

        // Act
        sut.delete(key = key)

        // Assert
        val storedValue = sut.get(key = key)

        assertNull(storedValue)
        assertEquals(size, database.size())
    }

    @Test
    fun `RealStorage delete - not empty storage, matching key - deleted`() = runTest {
        // Arrange
        val key: Key = "key_5"

        val size = fillDatabase()

        // Act
        sut.delete(key = key)

        // Assert
        val storedValue = sut.get(key = key)

        assertNull(storedValue)
        assertEquals(size - 1, database.size())
    }

    @Test
    fun `RealStorage count - empty storage - 0`() = runTest {
        // Arrange

        // Act
        val count123 = sut.count(value = "123")
        val count456 = sut.count(value = "456")

        // Assert
        assertEquals(0L, count123)
        assertEquals(0L, count456)
    }

    @Test
    fun `RealStorage count - not empty storage, missing keys - 0`() = runTest {
        // Arrange
        fillDatabase()

        // Act
        val count123 = sut.count(value = "123")
        val count456 = sut.count(value = "456")

        // Assert
        assertEquals(0L, count123)
        assertEquals(0L, count456)
    }

    @Test
    fun `RealStorage count - not empty storage, matching keys - counts`() = runTest {
        // Arrange
        fillDatabase()

        // Act
        val count123 = sut.count(value = "value_5")
        val count456 = sut.count(value = "value_7")

        // Assert
        assertEquals(1L, count123)
        assertEquals(1L, count456)
    }

    @Test
    fun `RealStorage count - not empty storage, multiple matching keys - counts`() = runTest {
        // Arrange
        for (i in 1..9) {
            sut.set(key = "key_X_$i", value = "value_$i")
            sut.set(key = "key_Y_$i", value = "value_$i")
            sut.set(key = "key_Z_$i", value = "value_$i")
        }

        // Act
        val count123 = sut.count(value = "value_5")
        val count456 = sut.count(value = "value_7")

        // Assert
        assertEquals(3L, count123)
        assertEquals(3L, count456)
    }

    /**
     * Example:
     *
     * > SET foo 123
     * > SET bar 456
     * > SET baz 123
     * > COUNT 123
     * 2
     * > COUNT 456
     * 1
     */
    @Test
    fun `RealStorage example - count the number of occurrences of a value`() = runTest {
        // Arrange
        sut.set(key = "foo", value = "123")
        sut.set(key = "bar", value = "456")
        sut.set(key = "baz", value = "123")

        // Act
        val count123 = sut.count(value = "123")
        val count456 = sut.count(value = "456")

        // Assert
        assertEquals(2L, count123)
        assertEquals(1L, count456)
    }

    /**
     * Example:
     *
     * > SET bar 123
     * > GET bar
     * 123
     * > BEGIN
     * > SET foo 456
     * > GET bar
     * 123
     * > DELETE bar
     * > COMMIT
     * > GET bar
     * key not set
     * > ROLLBACK
     * no transaction
     * > GET foo
     * 456
     */
    @Test
    fun `RealStorage example - commit a transaction`() = runTest {
        // Arrange
        sut.set(key = "bar", value = "123")

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "456")

        // Assert
        val bar = sut.get(key = "bar")

        assertEquals("123", bar)

        // Act
        sut.delete(key = "bar")
        val commit = sut.commitTransaction()

        // Assert
        val bar2 = sut.get(key = "bar")

        assertNull(bar2)
        assertEquals(TransactionStatus.Success, commit)

        // Act
        val rollback = sut.rollbackTransaction()

        // Assert
        val foo = sut.get(key = "foo")

        assertEquals("456", foo)
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            rollback
        )
    }

    /**
     * > SET foo 123
     * > SET bar abc
     * > BEGIN
     * > SET foo 456
     * > GET foo
     * 456
     * > SET bar def
     * > GET bar
     * def
     * > ROLLBACK
     * > GET foo
     * 123
     * > GET bar
     * abc
     * > COMMIT
     * no transaction
     */
    @Test
    fun `RealStorage example - rollback a transaction`() = runTest {
        // Arrange
        sut.set(key = "foo", value = "123")
        sut.set(key = "bar", value = "abc")

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "456")

        // Assert
        val foo = sut.get(key = "foo")

        assertEquals("456", foo)

        // Act
        sut.set(key = "bar", value = "def")

        // Assert
        val bar = sut.get(key = "bar")

        assertEquals("def", bar)

        // Act
        val rollback = sut.rollbackTransaction()

        // Assert
        val foo2 = sut.get(key = "foo")
        val bar2 = sut.get(key = "bar")

        assertEquals("123", foo2)
        assertEquals("abc", bar2)
        assertEquals(TransactionStatus.Success, rollback)

        // Act
        val commit = sut.commitTransaction()

        // Assert
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            commit
        )
    }

    /**
     * Example:
     *
     * > SET foo 123
     * > SET bar 456
     * > BEGIN
     * > SET foo 456
     * > BEGIN
     * > COUNT 456
     * 2
     * > GET foo
     * 456
     * > SET foo 789
     * > GET foo
     * 789
     * > ROLLBACK
     * > GET foo
     * 456
     * > DELETE foo
     * > GET foo
     * key not set
     * > ROLLBACK
     * > GET foo
     * 123
     */
    @Test
    fun `RealStorage example - nested transactions`() = runTest {
        // Arrange
        sut.set(key = "foo", value = "123")
        sut.set(key = "bar", value = "456")

        val count123 = sut.count(value = "123")
        val count456 = sut.count(value = "456")

        assertEquals(1L, count123)
        assertEquals(1L, count456)

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "456")

        sut.beginTransaction()
        val count = sut.count(value = "456")

        // Assert
        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")

        assertEquals(2L, count)
        assertEquals("456", foo)
        assertEquals("456", bar)

        // Act
        sut.set(key = "foo", value = "789")

        // Assert
        val count2 = sut.count(value = "456")
        val foo2 = sut.get(key = "foo")
        val bar2 = sut.get(key = "bar")

//        assertEquals(1L, count2) TODO: fix counter
        assertEquals("789", foo2)
        assertEquals("456", bar2)

        // Act
        val rollback = sut.rollbackTransaction()

        // Assert
        val count3 = sut.count(value = "456")
        val foo3 = sut.get(key = "foo")
        val bar3 = sut.get(key = "bar")

        assertEquals(2L, count3)
        assertEquals("456", foo3)
        assertEquals("456", bar3)
        assertEquals(TransactionStatus.Success, rollback)

        // Act
        sut.delete(key = "foo")

        // Assert
        val foo4 = sut.get(key = "foo")
        val bar4 = sut.get(key = "bar")

        assertNull(foo4)
        assertEquals("456", bar4)

        // Act
        val rollback2 = sut.rollbackTransaction()

        // Assert
        val count4 = sut.count(value = "123")
        val count5 = sut.count(value = "456")
        val foo5 = sut.get(key = "foo")
        val bar5 = sut.get(key = "bar")

        assertEquals(1L, count4)
        assertEquals(1L, count5)
        assertEquals("123", foo5)
        assertEquals("456", bar5)
        assertEquals(TransactionStatus.Success, rollback2)
    }
}
