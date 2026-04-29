package com.example.pagaapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.ui.screens.expenses.ExpensesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ExpensesViewModel.debtsState.collect { state ->
                _uiState.update { 
                    it.copy(
                        debts = state.totalYouOwe,
                        owedToMe = state.totalOwedToYou,
                        balance = state.totalOwedToYou - state.totalYouOwe
                    )
                }
            }
        }
    }
}
