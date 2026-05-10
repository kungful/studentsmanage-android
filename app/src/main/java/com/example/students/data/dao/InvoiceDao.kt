package com.example.students.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.students.data.model.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY date DESC")
    fun getByStatus(status: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE studentId = :studentId ORDER BY date DESC")
    fun getByStudentId(studentId: Long): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice): Long

    @Update
    suspend fun update(invoice: Invoice)

    @Delete
    suspend fun delete(invoice: Invoice)

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM invoices WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>
}
