package com.urbanize.urbanizeplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlayerDatabaseDao {
    @Insert
    fun insertCampaign(campaign: Campaign)

    @Update
    fun updateCampaign(campaign: Campaign)

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun getCampaign(id: String): Campaign?

    @Query("DELETE FROM campaigns")
    fun clearCampaigns()

    @Query("SELECT * FROM campaigns ORDER BY id DESC")
    fun getAllCampaigns(): LiveData<List<Campaign>>
}