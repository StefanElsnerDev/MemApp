package com.example.hannapp.data.distinct

import com.example.hannapp.Constants
import com.example.hannapp.data.model.NutritionModel

enum class NutritionDataComponent(val text: String) {
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

private fun addError(
    componentState: NutritionComponentState,
    component: NutritionDataComponent
): NutritionComponentState {
    val errors = componentState.error.toMutableSet()
    errors.add(component)
    return componentState.copy(error = errors.toList())
}

private fun removeError(
    nutritionComponentState: NutritionComponentState,
    component: NutritionDataComponent
): NutritionComponentState {
    val errors = nutritionComponentState.error.toMutableSet()
    errors.remove(component)
    return nutritionComponentState.copy(error = errors.toList())
}

interface NutritionComponent {
    var type: NutritionDataComponent
    var text: String
    fun update(
        nutritionModel: NutritionModel,
        value: String
    ): NutritionModel

    fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent>
}

class Name : NutritionComponent {
    override var type = NutritionDataComponent.NAME
    override var text = "name"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(name = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.name.isBlank()) {
            addError(errors, NutritionDataComponent.NAME)
        } else {
            removeError(errors, NutritionDataComponent.NAME)
        }
    }
}

class Kcal : NutritionComponent {
    override var type = NutritionDataComponent.KCAL
    override var text = "Kcal"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(kcal = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.kcal.isBlank()) {
            addError(errors, NutritionDataComponent.KCAL)
        } else {
            removeError(errors, NutritionDataComponent.KCAL)
        }
    }
}

class Protein : NutritionComponent {
    override var type = NutritionDataComponent.PROTEIN
    override var text = "Protein"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(protein = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.protein.isBlank()) {
            addError(errors, NutritionDataComponent.PROTEIN)
        } else {
            removeError(errors, NutritionDataComponent.PROTEIN)
        }
    }
}

class Fad : NutritionComponent {
    override var type = NutritionDataComponent.FAD
    override var text = "Fad"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(fad = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.fad.isBlank()) {
            addError(errors, NutritionDataComponent.FAD)
        } else {
            removeError(errors, NutritionDataComponent.FAD)
        }
    }
}

class Carbohydrates : NutritionComponent {
    override var type = NutritionDataComponent.CARBOHYDRATES
    override var text = "Carbohydrates"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(carbohydrates = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.carbohydrates.isBlank()) {
            addError(errors, NutritionDataComponent.CARBOHYDRATES)
        } else {
            removeError(errors, NutritionDataComponent.CARBOHYDRATES)
        }
    }
}

class Sugar : NutritionComponent {
    override var type = NutritionDataComponent.SUGAR
    override var text = "Sugar"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(sugar = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.sugar.isBlank()) {
            addError(errors, NutritionDataComponent.SUGAR)
        } else {
            removeError(errors, NutritionDataComponent.SUGAR)
        }
    }
}

class Fiber : NutritionComponent {
    override var type = NutritionDataComponent.FIBER
    override var text = "Fiber"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(fiber = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.fiber.isBlank()) {
            addError(errors, NutritionDataComponent.FIBER)
        } else {
            removeError(errors, NutritionDataComponent.FIBER)
        }
    }
}

class Alcohol : NutritionComponent {
    override var type = NutritionDataComponent.ALCOHOL
    override var text = "Alcohol"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(alcohol = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.alcohol.isBlank()) {
            addError(errors, NutritionDataComponent.ALCOHOL)
        } else {
            removeError(errors, NutritionDataComponent.ALCOHOL)
        }
    }
}

class Energy : NutritionComponent {
    override var type = NutritionDataComponent.ENERGY
    override var text = "Energy"

    override fun update(nutritionModel: NutritionModel, value: String) =
        nutritionModel.copy(energy = value)

    override fun validate(
        nutritionModel: NutritionModel,
        errors: Set<NutritionDataComponent>
    ): Set<NutritionDataComponent> {
        return if (nutritionModel.energy.isBlank()) {
            addError(errors, NutritionDataComponent.ENERGY)
        } else {
            removeError(errors, NutritionDataComponent.ENERGY)
        }
    }
}
