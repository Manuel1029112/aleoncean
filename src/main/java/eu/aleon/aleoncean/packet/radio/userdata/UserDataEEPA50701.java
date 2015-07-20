package eu.aleon.aleoncean.packet.radio.userdata;

import eu.aleon.aleoncean.values.Unit;

public class UserDataEEPA50701 extends UserData4BS {
	
	public static final long SUPPLY_VOLTAGE_RANGE_MIN = 0;
    public static final long SUPPLY_VOLTAGE_RANGE_MAX = 250;
    public static final double SUPPLY_VOLTAGE_SCALE_MIN = 0;
    public static final double SUPPLY_VOLTAGE_SCALE_MAX = 5;
    public static final Unit SUPPLY_VOLTAGE_UNIT = Unit.VOLTAGE;

    public static final long ILLUMINATION_RANGE_MIN = 0;
    public static final long ILLUMINATION_RANGE_MAX = 1000;
    public static final double ILLUMINATION_SCALE_MIN = 0;
    public static final double ILLUMINATION_SCALE_MAX = 1000;
    public static final Unit ILLUMINATION_UNIT = Unit.LUX;

    public UserDataEEPA50701(final byte[] eepData) {
        super(eepData);
    }

    public double getSupplyVoltage() throws UserDataScaleValueException {
        return getScaleValue(3, 7, 3, 0, SUPPLY_VOLTAGE_RANGE_MIN, SUPPLY_VOLTAGE_RANGE_MAX, SUPPLY_VOLTAGE_SCALE_MIN, SUPPLY_VOLTAGE_SCALE_MAX);
    }

    public Unit getSupplyVoltageUnit() {
        return SUPPLY_VOLTAGE_UNIT;
    }

    public double getIllumination() throws UserDataScaleValueException {
        return getScaleValue(2, 7, 1, 6, ILLUMINATION_RANGE_MIN, ILLUMINATION_RANGE_MAX, ILLUMINATION_SCALE_MIN, ILLUMINATION_SCALE_MAX);
    }

    public Unit getIlluminationUnit() {
        return ILLUMINATION_UNIT;
    }

    public boolean isPIRStatusOn() {	
    	long pirValue = getDataRange(1, 7, 1, 0);
    	//0...127: 		PIR off
    	//128...255:	PIR on
        return pirValue >= 128;
    }

}
