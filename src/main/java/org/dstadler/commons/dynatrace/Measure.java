package org.dstadler.commons.dynatrace;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;


/**
 * Helper class to handle measures together with their dynamic measure values locally.
 *
 * These are then passed on to the Dynatrace measure interface.
 *
 * This class supports only one type of dynamic measure which can have a number of
 * values reported for different occurrences.
 *
 * It also supports an "adjustmentFactor", i.e. a constant value which is applied
 * to the base value and also all dynamic values whenever the value is queried
 * via getValue() or getDynamicMeasures().
 *
 * @author dominik.stadler
 */
public class Measure {
	private double value;
	private double adjustmentFactor = 1;

	private String dynamicMeasureName;
	private Map<String, Double> dynamicMeasures = new HashMap<>();

	public Measure() {
		super();
	}

	public Measure(double value) {
		this(null, value);
	}

	public Measure(String dynamicMeasureName) {
		this(dynamicMeasureName, 0);
	}

	public Measure(String dynamicMeasureName, double value) {
		this.dynamicMeasureName = dynamicMeasureName;
		this.value = value;
	}

	public void setAdjustmentFactor(double adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}

	public double getValue() {
		return value*adjustmentFactor;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getDynamicMeasureName() {
		return dynamicMeasureName;
	}

	public Map<String, Double> getDynamicMeasures() {
		Map<String, Double> adjustedMap = new HashMap<>();
		for(Map.Entry<String, Double> entry : dynamicMeasures.entrySet()) {
			adjustedMap.put(entry.getKey(), entry.getValue()*adjustmentFactor);
		}
		return adjustedMap;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void addDynamicMeasure(String dynamic, double lvalue) {
		Preconditions.checkNotNull(dynamic,
				"Cannot add a dynamic measure value when the key of the dynamic measure is null");
		Preconditions.checkNotNull(dynamicMeasureName,
				"Cannot add a dynamic measure value when the name of the dynamic measure is not set in the constructor");

		if(!dynamicMeasures.containsKey(dynamic)) {
			dynamicMeasures.put(dynamic, lvalue);
		} else {
			dynamicMeasures.put(dynamic, dynamicMeasures.get(dynamic) + lvalue);
		}
	}

	public void incValue() {
		value++;
	}

	public void addValue(double lvalue) {
		value+=lvalue;
	}
}
