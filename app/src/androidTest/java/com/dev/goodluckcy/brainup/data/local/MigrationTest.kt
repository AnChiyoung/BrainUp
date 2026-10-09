package com.dev.goodluckcy.brainup.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BrainUpDatabase::class.java,
    )

    @Test
    fun migrate1To2KeepsProgressAndAddsCoinTables() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                "INSERT INTO daily_progress (date, completedMask, totalScore, completed) " +
                    "VALUES ('2026-10-08', 7, 300, 1)",
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true, BrainUpDatabase.MIGRATION_1_2).use { db ->
            db.query("SELECT completed, shielded FROM daily_progress WHERE date = '2026-10-08'").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
                assertEquals(0, cursor.getInt(1))
            }
            db.query("SELECT COUNT(*) FROM coin_transaction").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
