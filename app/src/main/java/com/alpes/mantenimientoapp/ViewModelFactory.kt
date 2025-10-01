// Archivo: ViewModelFactory.kt
package com.alpes.mantenimientoapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val dao: AppDao, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(dao) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(dao) as T
            modelClass.isAssignableFrom(TaskDetailViewModel::class.java) -> TaskDetailViewModel(dao) as T
            modelClass.isAssignableFrom(ChecklistViewModel::class.java) -> ChecklistViewModel(dao) as T
            modelClass.isAssignableFrom(AddEquipmentViewModel::class.java) -> AddEquipmentViewModel(dao) as T
            modelClass.isAssignableFrom(FinalizacionViewModel::class.java) -> FinalizacionViewModel(dao, application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}