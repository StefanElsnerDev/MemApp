package com.example.hannapp.data.distinct

import com.example.hannapp.Constants

enum class NutritionComponent(val text: String) {
    NAME(Constants.FOOD_NAME),
    KCAL(Constants.KCAL),
    PROTEIN(Constants.PROTEIN),
    FAD(Constants.FAD),
    CARBOHYDRATES(Constants.CARBOHYDRATES),
    SUGAR(Constants.SUGAR),
    FIBER(Constants.FIBER),
    ALCOHOL(Constants.ALCOHOL),
    ENERGY(Constants.ENERGY)
}
