package com.urbanize.urbanizeplayer

import androidx.room.Database
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.urbanize.urbanizeplayer.database.Campaign
import com.urbanize.urbanizeplayer.database.PlayerDatabase
import com.urbanize.urbanizeplayer.database.PlayerDatabaseDao
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class PlayerDatabaseTest {

    private lateinit var playerDao: PlayerDatabaseDao
    private lateinit var db: PlayerDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, PlayerDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        playerDao = db.playerDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetCampaign() {
        val campaign = Campaign()
        playerDao.insertCampaign(campaign)
        val campaigns = playerDao.getAllCampaigns()
        assertEquals(campaigns.value?.get(0)?.originalFilename, "")
    }
}