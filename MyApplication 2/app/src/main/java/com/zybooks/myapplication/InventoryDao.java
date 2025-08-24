package com.zybooks.myapplication;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface InventoryDao {

    @Insert
    void insertItem(InventoryItem item);

    @Update
    void updateItem(InventoryItem item);

    @Query("SELECT * FROM inventory WHERE quantity <= 1")
    LiveData<List<InventoryItem>> getLowInventoryItems();

    @Query("SELECT * FROM inventory")
    LiveData<List<InventoryItem>> getAllItemsLiveData();

    @Query("SELECT * FROM inventory")
    List<InventoryItem> getAllItems();

    @Query("SELECT * FROM inventory WHERE name = :name")
    InventoryItem getItemByName(String name);

    @Query("DELETE FROM inventory")
    void deleteAll();

    @Delete
    void deleteItem(InventoryItem item);
}