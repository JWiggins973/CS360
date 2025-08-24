package com.zybooks.myapplication;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {InventoryItem.class}, version = 1)
public abstract class InventoryDatabase extends RoomDatabase {
    private static InventoryDatabase instance;

    public abstract InventoryDao inventoryDao();

    // Get database instance
    public static synchronized InventoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            InventoryDatabase.class, "inventory_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}