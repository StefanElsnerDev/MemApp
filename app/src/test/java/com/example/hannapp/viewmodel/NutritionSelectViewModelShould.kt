package com.example.hannapp.viewmodel

import androidx.paging.PagingData
import com.example.hannapp.data.model.NutrimentLogModel
import com.example.hannapp.data.model.NutrimentUiLogModel
import com.example.hannapp.data.model.NutritionUiModel
import com.example.hannapp.data.model.convert.NutritionConverter
import com.example.hannapp.data.model.entity.Nutrition
import com.example.hannapp.domain.DeleteNutrimentLogUseCase
import com.example.hannapp.domain.GetNutrimentLogUseCase
import com.example.hannapp.domain.GetNutritionUseCase
import com.example.hannapp.domain.InsertNutrimentLogUseCase
import com.example.hannapp.ui.viewmodel.NutritionSelectViewModel
import com.example.hannapp.ui.viewmodel.NutritionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionSelectViewModelShould {

    private lateinit var nutritionViewModel: NutritionSelectViewModel
    private val getNutritionUseCase = mock(GetNutritionUseCase::class.java)
    private val insertNutrimentLogUseCase = mock(InsertNutrimentLogUseCase::class.java)
    private val getNutrimentLogUseCase = mock(GetNutrimentLogUseCase::class.java)
    private val deleteNutrimentLogUseCase = mock(DeleteNutrimentLogUseCase::class.java)
    private val nutritionConverter = mock(NutritionConverter::class.java)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val nutritions = listOf(
        Nutrition(uid = 100, name = "Apple", kcal = 1.2),
        Nutrition(uid = 200, name = "Banana", kcal = 3.4)
    )

    private val nutritionUiModels = listOf(
        NutritionUiModel(
            id = 100,
            name = "Apple",
            kcal = "1.2"
        ),
        NutritionUiModel(
            id = 200,
            name = "Banana",
            kcal = "3.4"
        )
    )

    private val pagingData = PagingData.from(nutritions)
    private val nutrimentsFlow = flowOf(pagingData)

    private val nutrimentLog = listOf(
        NutrimentLogModel(
            nutrition = nutritions.first(),
            quantity = 1.1,
            createdAt = 1681839531,
            modifiedAt = null
        ),
        NutrimentLogModel(
            nutrition = nutritions.last(),
            quantity = 2.2,
            createdAt = 1681234731,
            modifiedAt = null
        )
    )

    private val nutrimentUiLog = listOf(
        NutrimentUiLogModel(
            nutrition = nutritionUiModels.first(),
            quantity = 1.1,
            unit = "",
            timeStamp = 1681839531
        ),
        NutrimentUiLogModel(
            nutrition = nutritionUiModels.last(),
            quantity = 2.2,
            unit = "",
            timeStamp = 1681234731
        )
    )

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(testDispatcher)

        whenever(getNutritionUseCase.getAll()).thenReturn(
            nutrimentsFlow
        )

        whenever(getNutrimentLogUseCase.observeNutrimentLog()).thenReturn(
            flowOf(nutrimentLog)
        )

        whenever(nutritionConverter.entity(nutritions.first())).thenReturn(
            NutritionConverter.InnerNutrition(
                nutritions.first()
            )
        )

        whenever(nutritionConverter.entity(nutritions.last())).thenReturn(
            NutritionConverter.InnerNutrition(
                nutritions.last()
            )
        )

        nutritionViewModel = NutritionSelectViewModel(
            getNutritionUseCase = getNutritionUseCase,
            insertNutrimentLogUseCase = insertNutrimentLogUseCase,
            deleteNutrimentLogUseCase = deleteNutrimentLogUseCase,
            getNutrimentLogUseCase = getNutrimentLogUseCase,
            nutritionConverter = nutritionConverter,
            dispatcher = testDispatcher
        )
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun invokeGetNutritionUseCaseOn() = runTest {
        nutritionViewModel.getAll()

        verify(getNutritionUseCase).getAll()
    }

    @Test
    fun produceUIStateWithLoadingStateOnInstantiation() = runTest {
        nutritionViewModel = NutritionSelectViewModel(
            getNutritionUseCase = getNutritionUseCase,
            insertNutrimentLogUseCase = insertNutrimentLogUseCase,
            deleteNutrimentLogUseCase = deleteNutrimentLogUseCase,
            getNutrimentLogUseCase = getNutrimentLogUseCase,
            nutritionConverter = nutritionConverter,
            dispatcher = testDispatcher
        )

        Assertions.assertEquals(
            NutritionUiState(
                isLoading = true,
                errorMessage = null
            ),
            nutritionViewModel.uiState.first()
        )
    }

    @Test
    fun produceErrorUIStateOnException() = runTest {
        val errorMessage = "error"
        whenever(getNutritionUseCase.getAll()).thenReturn(
            flow {
                throw RuntimeException(errorMessage)
            }
        )

        nutritionViewModel.getAll()

        Assertions.assertEquals(
            NutritionUiState(
                isLoading = false,
                errorMessage = errorMessage
            ),
            nutritionViewModel.uiState.first()
        )
    }

    @Nested
    inner class NutrimentLogFlow {

        @Test
        fun emitsNutrimentLog() = runTest {
            assertThat(nutritionViewModel.nutrimentLog.first()).isEqualTo(nutrimentUiLog)
        }

        @Test
        fun emitsErrorStateOnFailingNutrimentLog() = runTest {
            val errorMessage = "Error"
            whenever(getNutrimentLogUseCase.observeNutrimentLog()).thenReturn(
                flow {
                    throw RuntimeException(errorMessage)
                }
            )

            nutritionViewModel = NutritionSelectViewModel(
                getNutritionUseCase = getNutritionUseCase,
                insertNutrimentLogUseCase = insertNutrimentLogUseCase,
                deleteNutrimentLogUseCase = deleteNutrimentLogUseCase,
                getNutrimentLogUseCase = getNutrimentLogUseCase,
                nutritionConverter = nutritionConverter,
                dispatcher = testDispatcher
            )

            assertThat(nutritionViewModel.uiState.value.errorMessage).contains(errorMessage)
        }
    }

    @Nested
    inner class Select {

        private val nutritionUiModel = NutritionUiModel(
            id = 123,
            name = "Cola",
            kcal = "123",
            protein = "0.1",
            fat = "0.0"
        )

        @Test
        fun emitUiStateWithEmptyNutrimentUiModel() {
            assertThat(nutritionViewModel.uiState.value.cachedNutritionUiModel).isEqualTo(
                NutritionUiModel()
            )
        }

        @Test
        fun emitUiStateWithErrorStateByDefault() {
            assertThat(nutritionViewModel.uiState.value.isSelectionValid).isFalse
        }

        @Test
        fun emitUiStateWithSelectedNutrimentUiModel() {
            nutritionViewModel.select(nutritionUiModel)

            assertThat(nutritionViewModel.uiState.value.cachedNutritionUiModel).isEqualTo(
                nutritionUiModel
            )
        }

        @Test
        fun emitUiStateWithValidationBasedOnModelId() {
            nutritionViewModel.select(nutritionUiModel)

            assertThat(nutritionViewModel.uiState.value.isSelectionValid).isTrue
        }

        @Test
        fun emitErrorStateOnInvalidSelection() {
            nutritionViewModel.select(NutritionUiModel(id = null))

            assertThat(nutritionViewModel.uiState.value.isSelectionValid).isFalse
            assertThat(nutritionViewModel.uiState.value.errorMessage).isEqualTo("Invalid selection")
        }
    }

    @Nested
    inner class AddToLog {

        private val quantity = 56.78

        @BeforeEach
        fun beforeEach() = runTest {
            whenever(nutritionConverter.uiModel(any())).thenReturn(
                NutritionConverter.InnerNutritionUiModel(
                    NutritionUiModel()
                )
            )
        }

        @Test
        fun callsUseCaseForAddingNutriment() = runTest {
            nutritionViewModel.add(quantity)

            verify(insertNutrimentLogUseCase).invoke(any())
        }

        @Test
        fun emitsErrorStateOnFailingInsertion() = runTest {
            whenever(insertNutrimentLogUseCase.invoke(any())).thenReturn(false)

            assertThat(nutritionViewModel.uiState.value.errorMessage).isNull()

            nutritionViewModel.add(quantity)

            assertThat(nutritionViewModel.uiState.value.errorMessage).isNotBlank()
        }

        @Test
        fun emitsErrorStateOnThrowingInsertion() = runTest {
            val errorMessage = "Logging failed!"
            whenever(insertNutrimentLogUseCase.invoke(any())).thenThrow(
                RuntimeException(
                    errorMessage
                )
            )

            nutritionViewModel.add(quantity)

            assertThat(nutritionViewModel.uiState.value.errorMessage).isEqualTo(errorMessage)
        }
    }

    @Nested
    inner class CastInputString {

        private var result = -1.0

        @Test
        fun executeOnSuccessfulCast() {
            val input = "123.456"
            val expected = 123.456

            nutritionViewModel.castAsDouble(input) {
                result = it
            }

            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun emitErrorStateOnEmptyInput() {
            nutritionViewModel.castAsDouble("") {
                result = it
            }

            assertThat(nutritionViewModel.uiState.value.errorMessage).containsAnyOf("Invalid Input")
        }

        @Test
        fun emitErrorStateOnNANInput() {
            nutritionViewModel.castAsDouble("4ny Num83r") {
                result = it
            }

            assertThat(nutritionViewModel.uiState.value.errorMessage).containsAnyOf("Invalid Input")
        }
    }

    @Nested
    inner class ResetHistory {

        @Test
        fun invokeDeleteLogUseCase() = runTest {
            nutritionViewModel.clearHistory()

            verify(deleteNutrimentLogUseCase).clear()
        }

        @Test
        fun emitErrorStateOnFailingDeletion() = runTest {
            whenever(deleteNutrimentLogUseCase.clear()).thenReturn(false)

            nutritionViewModel.clearHistory()

            assertThat(nutritionViewModel.uiState.value.errorMessage).isEqualTo("Deletion failed")
        }

        @Test
        fun emitErrorStateWithExceptionMessageOnFailedDeletion() = runTest {
            val errorMessage = "Any strange error"
            whenever(deleteNutrimentLogUseCase.clear()).thenThrow(RuntimeException(errorMessage))

            nutritionViewModel.clearHistory()

            assertThat(nutritionViewModel.uiState.value.errorMessage).isEqualTo(errorMessage)
        }

        @Test
        fun emitErrorStateOnUnexpectedErrorDuringDeletion() = runTest {
            whenever(deleteNutrimentLogUseCase.clear()).thenThrow(RuntimeException())

            nutritionViewModel.clearHistory()

            assertThat(nutritionViewModel.uiState.value.errorMessage).isEqualTo("Unexpected error on deletion")
        }
    }
}
