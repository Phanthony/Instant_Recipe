package com.phanthony.instantrecipe.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Single


@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSettings(settings: UserSettings)

    @Query("SELECT * FROM User_Settings_Table WHERE `key` == 0")
    fun getUserSettings(): LiveData<UserSettings>

    @Query("SELECT * FROM User_Settings_Table WHERE `key` == 0")
    fun getSingleSetting(): Single<UserSettings>
}