/*
 * Copyright (c) 2018 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.global.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.duckduckgo.app.blockingObserve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppDatabaseTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testHelper = MigrationTestHelper(getInstrumentation(), AppDatabase::class.qualifiedName, FrameworkSQLiteOpenHelperFactory())

    @Test
    fun whenMigratingFromVersion1To2ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 1).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, AppDatabase.MIGRATION_1_TO_2)
    }

    @Test
    fun whenMigratingFromVersion2To3ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 2).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 3, true, AppDatabase.MIGRATION_2_TO_3)
    }

    @Test
    fun whenMigratingFromVersion2To3ThenOldLeaderboardDataIsDeleted() {
        testHelper.createDatabase(TEST_DB_NAME, 2).use {
            it.execSQL("INSERT INTO `network_leaderboard` VALUES ('Network2', 'example.com')")
        }
        assertTrue(database().networkLeaderboardDao().trackerNetworkTally().blockingObserve()!!.isEmpty())
    }

    @Test
    fun whenMigratingFromVersion3To4ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 3).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 4, true, AppDatabase.MIGRATION_3_TO_4)
    }

    @Test
    fun whenMigratingFromVersion4To5ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 4).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 5, true, AppDatabase.MIGRATION_4_TO_5)
    }

    @Test
    fun whenMigratingFromVersion5To6ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 5).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 6, true, AppDatabase.MIGRATION_5_TO_6)
    }

    @Test
    fun whenMigratingFromVersion6To7ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 6).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, AppDatabase.MIGRATION_6_TO_7)
    }

    @Test
    fun whenMigratingFromVersion7To8ThenValidationSucceeds() {
        testHelper.createDatabase(TEST_DB_NAME, 7).close()
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, AppDatabase.MIGRATION_7_TO_8)
    }

    @Test
    fun whenMigratingFromVersion4To5ThenUpdatePositionsOfStoredTabs() {

        testHelper.createDatabase(TEST_DB_NAME, 4).use {
            it.execSQL("INSERT INTO `tabs` values ('tabid1', 'url', 'title') ")
            it.execSQL("INSERT INTO `tabs` values ('tabid2', 'url', 'title') ")
        }

        assertEquals(0, database().tabsDao().tabs()[0].position)
        assertEquals(1, database().tabsDao().tabs()[1].position)
    }

    @Test
    fun whenMigratingFromVersion4To5ThenTabsAreConsideredViewed() {

        testHelper.createDatabase(TEST_DB_NAME, 4).use {
            it.execSQL("INSERT INTO `tabs` values ('tabid1', 'url', 'title') ")
        }

        assertTrue(database().tabsDao().tabs()[0].viewed)
    }

    private fun database(): AppDatabase {
        val database = Room
            .databaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java, TEST_DB_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_1_TO_2,
                AppDatabase.MIGRATION_2_TO_3,
                AppDatabase.MIGRATION_3_TO_4,
                AppDatabase.MIGRATION_4_TO_5,
                AppDatabase.MIGRATION_5_TO_6,
                AppDatabase.MIGRATION_6_TO_7,
                AppDatabase.MIGRATION_7_TO_8
            )
            .allowMainThreadQueries()
            .build()

        testHelper.closeWhenFinished(database)

        return database
    }

    companion object {
        private const val TEST_DB_NAME = "TEST_DB_NAME"
    }
}