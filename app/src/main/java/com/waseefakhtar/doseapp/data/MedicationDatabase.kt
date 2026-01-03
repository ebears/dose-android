package com.waseefakhtar.doseapp.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.waseefakhtar.doseapp.data.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 3, to = 4, spec = MedicationDatabase.AutoMigration3to4::class),
        AutoMigration(from = 4, to = 5, spec = MedicationDatabase.AutoMigration4to5::class)
    ]
)
@TypeConverters(Converters::class)
abstract class MedicationDatabase : RoomDatabase() {

    abstract val dao: MedicationDao
    
    @DeleteColumn(tableName = "MedicationEntity", columnName = "timesOfDay")
    @RenameColumn(tableName = "MedicationEntity", fromColumnName = "date", toColumnName = "medicationTime")
    class AutoMigration3to4 : AutoMigrationSpec
    
    // Migration to make endDate nullable for lifetime medications
    class AutoMigration4to5 : AutoMigrationSpec
}
