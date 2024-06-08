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

    @Test
    fun `RealStorage commit - no open transaction - failure`() = runTest {
        // Arrange

        // Act
        val commit = sut.commitTransaction()

        // Assert
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            commit
        )
    }

    @Test
    fun `RealStorage commit - empty transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }
    }

    @Test
    fun `RealStorage commit - nested empty transactions - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.beginTransaction()
        sut.beginTransaction()
        val commit1 = sut.commitTransaction()
        val commit2 = sut.commitTransaction()
        val commit3 = sut.commitTransaction()
        val commit4 = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, commit1)
        assertEquals(TransactionStatus.Success, commit2)
        assertEquals(TransactionStatus.Success, commit3)
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            commit4
        )

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }
    }

    @Test
    fun `RealStorage commit - set unmatched key in transaction - add new key`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size + 1, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")

        assertEquals("123", foo)
    }

    @Test
    fun `RealStorage commit - set multiple unmatched keys in transaction - add new keys`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.set(key = "bar", value = "abc")
        sut.set(key = "baz", value = "2.714242")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size + 3, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertEquals("123", foo)
        assertEquals("abc", bar)
        assertEquals("2.714242", baz)
    }

    @Test
    fun `RealStorage commit - set matching key in transaction - replace key`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "key_5", value = "123")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 5L) {
                assertEquals("123", value)
            } else {
                assertEquals("value_$i", value)
            }
        }
    }

    @Test
    fun `RealStorage commit - set matching and unmatching keys in transaction - replace key`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.set(key = "key_5", value = "123") // matching key
        sut.set(key = "bar", value = "abc")
        sut.set(key = "baz", value = "2.714242")
        sut.set(key = "key_1", value = "def") // matching key

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size + 3, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            when (i) {
                1L -> assertEquals("def", value)
                5L -> assertEquals("123", value)
                else -> assertEquals("value_$i", value)
            }
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertEquals("123", foo)
        assertEquals("abc", bar)
        assertEquals("2.714242", baz)
    }

    @Test
    fun `RealStorage commit - set multiple keys in nested transactions - add new keys`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")

        sut.beginTransaction()
        sut.set(key = "bar", value = "def")

        sut.beginTransaction()
        sut.set(key = "baz", value = "3.1415")
        sut.set(key = "foo", value = "456") // overwrite

        val commit1 = sut.commitTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, commit1)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")
        val baz = sut.get(key = "baz")

        assertEquals("456", foo)
        assertEquals("3.1415", baz)

        // Act
        val commit2 = sut.commitTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, commit2)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo2 = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz2 = sut.get(key = "baz")

        assertEquals("456", foo2)
        assertEquals("def", bar)
        assertEquals("3.1415", baz2)

        // Act
        val commit3 = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size + 3, newSize)
        assertEquals(TransactionStatus.Success, commit3)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo3 = sut.get(key = "foo")
        val bar2 = sut.get(key = "bar")
        val baz3 = sut.get(key = "baz")

        assertEquals("456", foo3)
        assertEquals("def", bar2)
        assertEquals("3.1415", baz3)

        // Act
        val commit4 = sut.commitTransaction()

        // Assert
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            commit4
        )
    }

    @Test
    fun `RealStorage commit - delete unmatched key in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "foo")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")

        assertNull(foo)
    }

    @Test
    fun `RealStorage commit - delete matching key in transaction - remove key`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_5")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size - 1, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 5L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }
    }

    @Test
    fun `RealStorage commit - delete multiple matching keys in transaction - remove keys`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_5")
        sut.delete(key = "key_2")
        sut.delete(key = "key_7")
        sut.delete(key = "key_1")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size - 4, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 1L || i == 2L || i == 5L || i == 7L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }
    }

    @Test
    fun `RealStorage commit - delete matching and unmatching key in transaction - remove key`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_5") // matching key
        sut.delete(key = "foo")
        sut.delete(key = "bar")
        sut.delete(key = "key_7") // matching key
        sut.delete(key = "key_1") // matching key
        sut.delete(key = "baz")

        val commit = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size - 3, newSize)
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 1L || i == 5L || i == 7L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertNull(foo)
        assertNull(bar)
        assertNull(baz)
    }

    @Test
    fun `RealStorage commit - set and delete multiple keys in nested transactions - changed storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.set(key = "baz", value = "2.71")

        sut.beginTransaction()
        sut.set(key = "bar", value = "def")
        sut.delete(key = "key_5")

        sut.beginTransaction()
        sut.set(key = "baz", value = "3.1415")
        sut.delete(key = "bar")
        sut.delete(key = "key_2")
        sut.set(key = "foo", value = "456") // overwrite

        val commit1 = sut.commitTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, commit1)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 2L || i == 5L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertEquals("456", foo)
        assertNull(bar)
        assertEquals("3.1415", baz)

        // Act
        sut.delete(key = "key_3")
        sut.delete(key = "key_7")

        val commit2 = sut.commitTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, commit2)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 2L || i == 3L || i == 5L || i == 7L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        val foo2 = sut.get(key = "foo")
        val bar2 = sut.get(key = "bar")
        val baz2 = sut.get(key = "baz")

        assertEquals("456", foo2)
        assertNull(bar2)
        assertEquals("3.1415", baz2)

        // Act
        val commit3 = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size - 2, newSize)
        assertEquals(TransactionStatus.Success, commit3)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 2L || i == 3L || i == 5L || i == 7L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        val foo3 = sut.get(key = "foo")
        val bar3 = sut.get(key = "bar")
        val baz3 = sut.get(key = "baz")

        assertEquals("456", foo3)
        assertNull(bar3)
        assertEquals("3.1415", baz3)

        // Act
        val commit4 = sut.commitTransaction()

        // Assert
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            commit4
        )
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

    @Test
    fun `RealStorage rollback - no open transaction - failure`() = runTest {
        // Arrange

        // Act
        val rollback = sut.rollbackTransaction()

        // Assert
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            rollback
        )
    }

    @Test
    fun `RealStorage rollback - empty transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }
    }

    @Test
    fun `RealStorage rollback - set unmatched key in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")

        assertNull(foo)
    }

    @Test
    fun `RealStorage rollback - set multiple unmatched keys in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.set(key = "bar", value = "abc")
        sut.set(key = "baz", value = "2.714242")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertNull(foo)
        assertNull(bar)
        assertNull(baz)
    }

    @Test
    fun `RealStorage rollback - set matching key in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "key_5", value = "123")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }
    }

    @Test
    fun `RealStorage rollback - set matching and unmatching keys in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.set(key = "key_5", value = "123") // matching key
        sut.set(key = "bar", value = "abc")
        sut.set(key = "baz", value = "2.714242")
        sut.set(key = "key_1", value = "def") // matching key

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertNull(foo)
        assertNull(bar)
        assertNull(baz)
    }

    @Test
    fun `RealStorage rollback - delete unmatched key in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "foo")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")

        assertNull(foo)
    }

    @Test
    fun `RealStorage rollback - delete matching key in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_5")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }
    }

    @Test
    fun `RealStorage rollback - delete multiple matching keys in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_5")
        sut.delete(key = "key_2")
        sut.delete(key = "key_7")
        sut.delete(key = "key_1")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }
    }

    @Test
    fun `RealStorage rollback - delete matching and unmatching key in transaction - no changes to storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_5") // matching key
        sut.delete(key = "foo")
        sut.delete(key = "bar")
        sut.delete(key = "key_7") // matching key
        sut.delete(key = "key_1") // matching key
        sut.delete(key = "baz")

        val rollback = sut.rollbackTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size, newSize)
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertNull(foo)
        assertNull(bar)
        assertNull(baz)
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

        assertEquals(1L, count2)
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

    @Test
    fun `RealStorage rollback - set and delete multiple keys in nested transactions - changed storage`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.set(key = "baz", value = "2.71")

        sut.beginTransaction()
        sut.set(key = "bar", value = "def")
        sut.delete(key = "key_5")

        sut.beginTransaction()
        sut.set(key = "baz", value = "3.1415")
        sut.delete(key = "bar")
        sut.delete(key = "key_2")
        sut.set(key = "foo", value = "456") // overwrite

        val commit1 = sut.commitTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, commit1)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 2L || i == 5L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        val foo = sut.get(key = "foo")
        val bar = sut.get(key = "bar")
        val baz = sut.get(key = "baz")

        assertEquals("456", foo)
        assertNull(bar)
        assertEquals("3.1415", baz)

        // Act
        sut.delete(key = "key_3")
        sut.delete(key = "key_7")

        val rollback = sut.rollbackTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo2 = sut.get(key = "foo")
        val bar2 = sut.get(key = "bar")
        val baz2 = sut.get(key = "baz")

        assertEquals("123", foo2)
        assertNull(bar2) // was not added
        assertEquals("2.71", baz2)

        // Act
        val commit3 = sut.commitTransaction()

        // Assert
        val newSize = database.size()

        assertEquals(size + 2, newSize)
        assertEquals(TransactionStatus.Success, commit3)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            assertEquals("value_$i", value)
        }

        val foo3 = sut.get(key = "foo")
        val bar3 = sut.get(key = "bar")
        val baz3 = sut.get(key = "baz")

        assertEquals("123", foo3)
        assertNull(bar3) // was not added
        assertEquals("2.71", baz3)

        // Act
        val commit4 = sut.commitTransaction()

        // Assert
        assertEquals(
            TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction),
            commit4
        )
    }

    @Test
    fun `RealStorage commit and rollback nested transactions`() = runTest {
        // Arrange
        val size = fillDatabase()

        // Act
        sut.beginTransaction()
        sut.set(key = "foo", value = "123")
        sut.delete(key = "key_9")
        sut.set(key = "bar", value = "123")
        sut.set(key = "baz", value = "value_5")
        sut.set(key = "quo", value = "value_5")
        sut.delete(key = "key_1")

        // Assert
        val count123 = sut.count(value = "123")
        val countValue1 = sut.count(value = "value_1")
        val countValue5 = sut.count(value = "value_5")

        assertEquals(2L, count123)
        assertEquals(0L, countValue1)
        assertEquals(3L, countValue5)

        // Act
        sut.beginTransaction()
        sut.set(key = "bar", value = "456")
        sut.set(key = "baz", value = "123")

        // Assert
        val count456 = sut.count(value = "456")
        val count123b = sut.count(value = "123")
        val countValue1b = sut.count(value = "value_1")
        val countValue5b = sut.count(value = "value_5")

        assertEquals(1L, count456)
        assertEquals(2L, count123b)
        assertEquals(0L, countValue1b)
        assertEquals(2L, countValue5b)

        // Act
        sut.beginTransaction()
        sut.delete(key = "key_1") // already deleted
        sut.delete(key = "foo")
        sut.delete(key = "key_5")
        sut.set(key = "toa", value = "2.71")

        // Assert
        val count456c = sut.count(value = "456")
        val count123c = sut.count(value = "123")
        val countValue1c = sut.count(value = "value_1")
        val countValue5c = sut.count(value = "value_5")
        val count2e71 = sut.count(value = "2.71")

        assertEquals(1L, count456c)
        assertEquals(1L, count123c)
        assertEquals(0L, countValue1c)
        assertEquals(1L, countValue5c)
        assertEquals(1L, count2e71)

        // Act
        val commit = sut.commitTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, commit)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 1L || i == 5L || i == 9L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        assertNull(sut.get(key = "foo"))
        assertEquals("456", sut.get(key = "bar"))
        assertEquals("123", sut.get(key = "baz"))
        assertEquals("value_5", sut.get(key = "quo"))
        assertEquals("2.71", sut.get(key = "toa"))

        // Act
        val rollback = sut.rollbackTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, rollback)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            if (i == 1L || i == 9L) {
                assertNull(value)
            } else {
                assertEquals("value_$i", value)
            }
        }

        assertEquals("123", sut.get(key = "foo"))
        assertEquals("123", sut.get(key = "bar"))
        assertEquals("value_5", sut.get(key = "baz"))
        assertEquals("value_5", sut.get(key = "quo"))
        assertNull(sut.get(key = "toa"))

        val count123d = sut.count(value = "123")
        val countValue1d = sut.count(value = "value_1")
        val countValue5d = sut.count(value = "value_5")

        assertEquals(2L, count123d)
        assertEquals(0L, countValue1d)
        assertEquals(3L, countValue5d)

        // Act
        val rollback1 = sut.rollbackTransaction()

        // Assert
        assertEquals(TransactionStatus.Success, rollback1)

        for (i in 1..size) {
            val value = sut.get(key = "key_$i")
            val count = sut.count(value = "value_$i")
            assertEquals("value_$i", value)
            assertEquals(1L, count)
        }

        assertEquals(0L, sut.count(value = "123"))
        assertEquals(0L, sut.count(value = "456"))
        assertEquals(0L, sut.count(value = "2.71"))
    }
}
