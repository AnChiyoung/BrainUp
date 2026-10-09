package com.dev.goodluckcy.brainup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dev.goodluckcy.brainup.data.local.dao.CoinDao
import com.dev.goodluckcy.brainup.data.local.dao.DailyProgressDao
import com.dev.goodluckcy.brainup.data.local.dao.GameRecordDao
import com.dev.goodluckcy.brainup.data.local.entity.CoinTransactionEntity
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import com.dev.goodluckcy.brainup.data.local.entity.EquippedItemEntity
import com.dev.goodluckcy.brainup.data.local.entity.GameRecordEntity
import com.dev.goodluckcy.brainup.data.local.entity.InventoryEntity

@Database(
    entities = [
        GameRecordEntity::class,
        DailyProgressEntity::class,
        CoinTransactionEntity::class,
        InventoryEntity::class,
        EquippedItemEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class BrainUpDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun coinDao(): CoinDao

    companion object {
        const val NAME = "brainup.db"

        /** v2: 코인·상점 테이블 추가, 방패로 지킨 날 표시 */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `coin_transaction` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`amount` INTEGER NOT NULL, `source` TEXT NOT NULL, `itemId` TEXT, `date` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_coin_transaction_source` ON `coin_transaction` (`source`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_coin_transaction_date` ON `coin_transaction` (`date`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `inventory` (`itemId` TEXT NOT NULL, `quantity` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`itemId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `equipped_item` (`slot` TEXT NOT NULL, `itemId` TEXT NOT NULL, " +
                        "PRIMARY KEY(`slot`))",
                )
                db.execSQL("ALTER TABLE `daily_progress` ADD COLUMN `shielded` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
