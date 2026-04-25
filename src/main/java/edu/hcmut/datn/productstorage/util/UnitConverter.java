package edu.hcmut.datn.productstorage.util;

import edu.hcmut.datn.productstorage.common.enums.Unit;

import java.util.Map;
import java.util.Set;

public class UnitConverter {

    private static final Set<Unit> MASS_UNITS = Set.of(Unit.KILOGRAM, Unit.GRAM);
    private static final Set<Unit> VOLUME_UNITS = Set.of(Unit.LITER, Unit.MILLILITER);

    private static final Map<Unit, Double> TO_BASE = Map.of(
            Unit.KILOGRAM,   1.0,
            Unit.GRAM,       0.001,
            Unit.LITER,      1.0,
            Unit.MILLILITER, 0.001
    );

    public static double convert(double value, Unit unitIn, Unit unitOut) {
        if (unitIn == unitOut) {
            return value;
        }

        boolean sameType = (MASS_UNITS.contains(unitIn) && MASS_UNITS.contains(unitOut))
                || (VOLUME_UNITS.contains(unitIn) && VOLUME_UNITS.contains(unitOut));

        if (!sameType) {
            throw new IllegalArgumentException(
                    "Incompatible units: cannot convert " + unitIn + " to " + unitOut
            );
        }

        // Convert to base unit first, then to target unit
        double inBase = value * TO_BASE.get(unitIn);
        return inBase / TO_BASE.get(unitOut);
    }

    public static int splitBatch(double batchValue, Unit batchUnit, double packageValue, Unit packageUnit) {
        // Convert batch to the same unit as package
        double batchConverted = convert(batchValue, batchUnit, packageUnit);
        return (int) Math.floor(batchConverted / packageValue);
    }
}
