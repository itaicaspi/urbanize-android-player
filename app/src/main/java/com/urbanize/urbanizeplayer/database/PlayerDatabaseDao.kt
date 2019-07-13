package com.urbanize.urbanizeplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlayerDatabaseDao {
    /**
     * Campaigns
     */
    @Insert
    fun insertCampaign(campaign: Campaign)

    @Update
    fun updateCampaign(campaign: Campaign)

    @Query("DELETE FROM campaigns WHERE id = :id")
    fun removeCampaign(id: String)

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun getCampaign(id: String): Campaign?

    @Query("DELETE FROM campaigns")
    fun clearCampaigns()

    @Query("SELECT * FROM campaigns ORDER BY id DESC")
    fun getAllCampaigns(): List<Campaign>

    /**
     * Info Ticker
     */
    @Insert
    fun insertInfoTickerEntry(infoTickerEntry: InfoTickerEntry)

    @Query("DELETE FROM info_ticker WHERE id = :id")
    fun removeInfoTickerEntry(id: String)

    @Query("DELETE FROM info_ticker")
    fun clearInfoTickerEntries()

    @Query("SELECT * FROM info_ticker ORDER BY position DESC")
    fun getAllInfoTickerEntries(): List<InfoTickerEntry>
}