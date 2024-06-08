package com.orcchg.trustwallet.task.data.model

import com.orcchg.trustwallet.task.domain.model.Key
import kotlin.test.Test
import kotlin.test.assertTrue

class TestTransaction {
    private fun filledMap(): Map<Key, Operation> =
        mapOf(
            "foo" to Operation.Set(key = "foo", value = "123"),
            "bar" to Operation.Set(key = "bar", value = "abc"),
            "baz" to Operation.Set(key = "baz", value = "play"),
            "del" to Operation.Delete(key = "del"),
            "quo" to Operation.Set(key = "quo", value = "9801"),
            "how" to Operation.Delete(key = "how"),
            "toa" to Operation.Set(key = "toa", value = "zoi4")
        )

    private fun assertMapEquals(expected: Map<Key, Operation>, actual: Map<Key, Operation>) {
        var equality = expected.size == actual.size
        expected.forEach { (key, value) ->
            equality = equality && actual[key] == value
        }
        assert(equality)
    }

    @Test
    fun `Transaction applyOnTop - both maps empty - empty map`() {
        // Arrange
        val map = mapOf<Key, Operation>()
        val ontop = mapOf<Key, Operation>()

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        assertTrue { result.isEmpty() }
    }

    @Test
    fun `Transaction applyOnTop - ontop map empty - this map unmodified`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf<Key, Operation>()

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        assertMapEquals(map, result)
    }

    @Test
    fun `Transaction applyOnTop - this map empty - ontop map without deletes`() {
        // Arrange
        val map = mapOf<Key, Operation>()
        val ontop = filledMap()

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val ontopNoDeletes = ontop.filterValues { it !is Operation.Delete }

        assertMapEquals(ontopNoDeletes, result)
    }

    @Test
    fun `Transaction applyOnTop - maps have unmatching keys - merged map`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "key_1" to Operation.Set(key = "key_1", value = "3.1415"),
            "key_2" to Operation.Set(key = "key_2", value = "trust"),
            "key_3" to Operation.Set(key = "key_3", value = "wallet"),
            "key_4" to Operation.Delete(key = "key_4"),
            "key_5" to Operation.Set(key = "key_5", value = "007"),
            "key_6" to Operation.Delete(key = "key_6"),
            "key_7" to Operation.Set(key = "key_7", value = "42")
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val merged = map.toMutableMap().apply {
            putAll(ontop.filterValues { it !is Operation.Delete })
        }

        assertMapEquals(merged, result)
    }

    @Test
    fun `Transaction applyOnTop - maps have matching keys, ontop has only sets - this map modified`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "key_1" to Operation.Set(key = "key_1", value = "3.1415"),
            "foo" to Operation.Set(key = "foo", value = "trust"),
            "key_3" to Operation.Set(key = "key_3", value = "wallet"),
            "bar" to Operation.Set(key = "bar", value = "007"),
            "quo" to Operation.Set(key = "quo", value = "42")
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val applied = mapOf(
            "foo" to Operation.Set(key = "foo", value = "trust"), // modified
            "bar" to Operation.Set(key = "bar", value = "007"), // modified
            "baz" to Operation.Set(key = "baz", value = "play"),
            "del" to Operation.Delete(key = "del"),
            "quo" to Operation.Set(key = "quo", value = "42"), // modified
            "how" to Operation.Delete(key = "how"),
            "toa" to Operation.Set(key = "toa", value = "zoi4"),
            "key_1" to Operation.Set(key = "key_1", value = "3.1415"),
            "key_3" to Operation.Set(key = "key_3", value = "wallet")
        )

        assertMapEquals(applied, result)
    }

    @Test
    fun `Transaction applyOnTop - maps have all matching keys, ontop has only sets - this map overwritten completely`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "foo" to Operation.Set(key = "foo", value = "123-x"),
            "bar" to Operation.Set(key = "bar", value = "abc-x"),
            "baz" to Operation.Set(key = "baz", value = "play-x"),
            "del" to Operation.Delete(key = "del"), // matching, to be deleted
            "quo" to Operation.Set(key = "quo", value = "9801-x"),
            "how" to Operation.Delete(key = "how"), // matching, to be deleted
            "toa" to Operation.Set(key = "toa", value = "zoi4-x")
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val applied = mapOf(
            "foo" to Operation.Set(key = "foo", value = "123-x"),
            "bar" to Operation.Set(key = "bar", value = "abc-x"),
            "baz" to Operation.Set(key = "baz", value = "play-x"),
            "quo" to Operation.Set(key = "quo", value = "9801-x"),
            "toa" to Operation.Set(key = "toa", value = "zoi4-x")
        )

        assertMapEquals(applied, result)
    }

    @Test
    fun `Transaction applyOnTop - maps have matching keys, ontop has only deletes - this map modified`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "key_1" to Operation.Delete(key = "key_1"),
            "foo" to Operation.Delete(key = "foo"), // matching, to be deleted
            "key_3" to Operation.Delete(key = "key_3"),
            "bar" to Operation.Delete(key = "bar"), // matching, to be deleted
            "quo" to Operation.Delete(key = "quo") // matching, to be deleted
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val applied = mapOf(
            "baz" to Operation.Set(key = "baz", value = "play"),
            "del" to Operation.Delete(key = "del"),
            "how" to Operation.Delete(key = "how"),
            "toa" to Operation.Set(key = "toa", value = "zoi4")
        )

        assertMapEquals(applied, result)
    }

    @Test
    fun `Transaction applyOnTop - maps have all matching keys, ontop has only deletes - empty map`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "foo" to Operation.Delete(key = "foo"), // matching, to be deleted
            "bar" to Operation.Delete(key = "bar"), // matching, to be deleted
            "baz" to Operation.Delete(key = "baz"), // matching, to be deleted
            "del" to Operation.Delete(key = "del"), // matching, to be deleted
            "quo" to Operation.Delete(key = "quo"), // matching, to be deleted
            "how" to Operation.Delete(key = "how"), // matching, to be deleted
            "toa" to Operation.Delete(key = "toa") // matching, to be deleted
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        assertTrue { result.isEmpty() }
    }

    @Test
    fun `Transaction applyOnTop - maps have matching keys, ontop has sets and deletes - this map modified`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "key_1" to Operation.Set(key = "key_1", value = "3.1415"),
            "foo" to Operation.Set(key = "foo", value = "trust"),
            "key_3" to Operation.Set(key = "key_3", value = "wallet"),
            "bar" to Operation.Delete(key = "bar"), // matching, to be deleted
            "quo" to Operation.Set(key = "quo", value = "42"),
            "baz" to Operation.Delete(key = "baz"), // matching, to be deleted
            "must" to Operation.Delete(key = "must")
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val applied = mapOf(
            "foo" to Operation.Set(key = "foo", value = "trust"), // modified
            "del" to Operation.Delete(key = "del"),
            "quo" to Operation.Set(key = "quo", value = "42"), // modified
            "how" to Operation.Delete(key = "how"),
            "toa" to Operation.Set(key = "toa", value = "zoi4"),
            "key_1" to Operation.Set(key = "key_1", value = "3.1415"),
            "key_3" to Operation.Set(key = "key_3", value = "wallet")
        )

        assertMapEquals(applied, result)
    }

    @Test
    fun `Transaction applyOnTop - maps have all matching keys, ontop has sets and deletes - this map modified`() {
        // Arrange
        val map = filledMap()
        val ontop = mapOf(
            "foo" to Operation.Set(key = "foo", value = "modified"),
            "bar" to Operation.Set(key = "bar", value = "abc"),
            "baz" to Operation.Delete(key = "baz"),
            "some_key" to Operation.Set(key = "some_key", value = "12345"),
            "quo" to Operation.Set(key = "quo", value = "9801"),
            "how" to Operation.Delete(key = "how"),
            "toa" to Operation.Set(key = "toa", value = "zoi4-x"),
            "del_key" to Operation.Delete(key = "del_key"),
            "new_key" to Operation.Set(key = "new_key", value = "hello")
        )

        // Act
        val result = map.applyOnTop(ontop)

        // Assert
        val applied = mapOf(
            "foo" to Operation.Set(key = "foo", value = "modified"), // modified
            "bar" to Operation.Set(key = "bar", value = "abc"), // not changed
            // baz deleted
            "del" to Operation.Delete(key = "del"),
            "quo" to Operation.Set(key = "quo", value = "9801"), // not changed
            // how deleted
            "toa" to Operation.Set(key = "toa", value = "zoi4-x"), // modified
            "some_key" to Operation.Set(key = "some_key", value = "12345"), // added
            "new_key" to Operation.Set(key = "new_key", value = "hello") // added
        )

        assertMapEquals(applied, result)
    }
}
