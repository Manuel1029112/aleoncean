package eu.aleon.aleoncean.device.remote;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.aleon.aleoncean.device.DeviceParameter;
import eu.aleon.aleoncean.device.DeviceParameterUpdatedInitiation;
import eu.aleon.aleoncean.device.IllegalDeviceParameterException;
import eu.aleon.aleoncean.device.RemoteDevice;
import eu.aleon.aleoncean.device.StandardDevice;
import eu.aleon.aleoncean.packet.EnOceanId;
import eu.aleon.aleoncean.packet.RadioPacket;
import eu.aleon.aleoncean.packet.radio.RadioPacket4BS;
import eu.aleon.aleoncean.packet.radio.userdata.UserDataEEPA50701;
import eu.aleon.aleoncean.packet.radio.userdata.UserDataScaleValueException;
import eu.aleon.aleoncean.rxtx.ESP3Connector;


/**
 * Implementation for EPP A5-07-03.
 * @author Manuel Klimek <e1029112@student.tuwien.ac.at>
 */
public class RemoteDeviceEEPA50703 extends StandardDevice implements RemoteDevice {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDeviceEEPA50703.class);
	
	private Double supplyVoltage;
	private Double illumination;
	private Boolean motion;

	public RemoteDeviceEEPA50703(final ESP3Connector conn, 
								 final EnOceanId addressRemote,
								 final EnOceanId addressLocal) {
		super(conn, addressRemote, addressLocal);
		
	}
	 public Double getSupplyVoltage() {
	        return supplyVoltage;
	    }
	 
	 public void setSupplyVoltage(final DeviceParameterUpdatedInitiation initiation, final Double supplyVoltage) {
	        final Double oldSupplyVoltage = this.supplyVoltage;
	        this.supplyVoltage = supplyVoltage;
	        fireParameterChanged(DeviceParameter.SUPPLY_VOLTAGE_V, initiation, oldSupplyVoltage, supplyVoltage);
	    }
	 
	 public Double getIllumination() {
	        return illumination;
	    }
	 
	 public void setIllumination(final DeviceParameterUpdatedInitiation initiation, final Double illumination) {
	        final Double oldIllumination = this.illumination;
	        this.illumination = illumination;
	        fireParameterChanged(DeviceParameter.ILLUMINATION_LUX, initiation, oldIllumination, illumination);
	    }
	 
	 public Boolean isMotion() {
	        return motion;
	    }
	 
	 public void setMotion(final DeviceParameterUpdatedInitiation initiation, final Boolean motion) {
	        final Boolean oldMotion = this.motion;
	        this.motion = motion;
	        fireParameterChanged(DeviceParameter.MOTION, initiation, oldMotion, motion);
	    }
	 
	 private void parseRadioPacket4BS(final RadioPacket4BS packet) {
	        if (packet.isTeachIn()) {
	            LOGGER.debug("Ignore teach-in packets.");
	            return;
	        }
	        
	        final UserDataEEPA50701 userData = new UserDataEEPA50701(packet.getUserDataRaw());
	        
	        try {
	        	setSupplyVoltage(DeviceParameterUpdatedInitiation.RADIO_PACKET, userData.getSupplyVoltage());
	        } catch (final UserDataScaleValueException ex) {
	            LOGGER.warn("Received supply voltage is invalid.");
	        }
	        try {
	        	setIllumination(DeviceParameterUpdatedInitiation.RADIO_PACKET, userData.getIllumination());
	        } catch (final UserDataScaleValueException ex) {
	            LOGGER.warn("Received illumination is invalid.");
	        }
	        setMotion(DeviceParameterUpdatedInitiation.RADIO_PACKET, userData.isPIRStatusOn());	 
	        
	 }
	 

	@Override
	public void parseRadioPacket(RadioPacket packet) {
		 if (packet instanceof RadioPacket4BS) {
	            parseRadioPacket4BS((RadioPacket4BS) packet);
	        } else {
	            LOGGER.warn("Don't know how to handle radio choice {}", String.format("0x%02X", packet.getChoice()));
	        }
	}

	@Override
	protected void fillParameters(Set<DeviceParameter> params) {
		params.add(DeviceParameter.SUPPLY_VOLTAGE_V);
        params.add(DeviceParameter.ILLUMINATION_LUX);
        params.add(DeviceParameter.MOTION);	
	}
	
	@Override
    public Object getByParameter(final DeviceParameter parameter) throws IllegalDeviceParameterException {
        switch (parameter) {
            case SUPPLY_VOLTAGE_V:
                return getSupplyVoltage();
            case ILLUMINATION_LUX:
                return getIllumination();           
            case MOTION:
                return isMotion();
            default:
                return super.getByParameter(parameter);
        }
    }
	
	@Override
    public void setByParameter(final DeviceParameter parameter, final Object value) throws IllegalDeviceParameterException {
        assert DeviceParameter.getSupportedClass(parameter).isAssignableFrom(value.getClass());
        super.setByParameter(parameter, value);
    }

}
