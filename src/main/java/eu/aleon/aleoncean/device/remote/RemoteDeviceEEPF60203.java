package eu.aleon.aleoncean.device.remote;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.aleon.aleoncean.device.DeviceParameter;
import eu.aleon.aleoncean.device.DeviceParameterUpdatedInitiation;
import eu.aleon.aleoncean.device.DeviceRPS;
import eu.aleon.aleoncean.device.IllegalDeviceParameterException;
import eu.aleon.aleoncean.device.RemoteDevice;
import eu.aleon.aleoncean.packet.EnOceanId;
import eu.aleon.aleoncean.packet.RadioPacket;
import eu.aleon.aleoncean.packet.radio.RadioPacketRPS;
import eu.aleon.aleoncean.packet.radio.userdata.UserDataEEPF60203;
import eu.aleon.aleoncean.packet.radio.userdata.UserDataScaleValueException;
import eu.aleon.aleoncean.rxtx.ESP3Connector;
import eu.aleon.aleoncean.values.RockerSwitchAction;
import eu.aleon.aleoncean.values.RockerSwitchButton;
import eu.aleon.aleoncean.values.RockerSwitchState;


/**
 * Implementation for EPP F6-02-03.
 * Can also be used and is tested with Eltako FSSA-230V
 * @author Manuel Klimek <e1029112@student.tuwien.ac.at>
 */
public class RemoteDeviceEEPF60203 extends DeviceRPS implements RemoteDevice{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDeviceEEPA50703.class);
	
	private final RockerSwitchState state = new RockerSwitchState();
	
	private RockerSwitchAction actionA;
    private RockerSwitchAction actionB;

	public RemoteDeviceEEPF60203(ESP3Connector conn, EnOceanId addressRemote,
			EnOceanId addressLocal) {
		super(conn, addressRemote, addressLocal);
		
	}
	
    private RockerSwitchButton getDimUpA() {
        return UserDataEEPF60203.DIM_A_UP;
    }

    private RockerSwitchButton getDimDownA() {
        return UserDataEEPF60203.DIM_A_DOWN;
    }

    private RockerSwitchButton getDimUpB() {
        return UserDataEEPF60203.DIM_B_UP;
    }

    private RockerSwitchButton getDimDownB() {
        return UserDataEEPF60203.DIM_B_DOWN;
    }

    public void setButton(final RockerSwitchButton button) {  	
    	final UserDataEEPF60203 data = new UserDataEEPF60203();
    	data.setRockerAction(button);
    	send(data);
    }
	
	
	private void buttonFireChange(final RockerSwitchButton button,
            final Boolean newState) {
		final DeviceParameterUpdatedInitiation initiation = DeviceParameterUpdatedInitiation.RADIO_PACKET;

		// Fire the action for the specific button side (A/B).
		// Use DIM, because this is different for the two RPS profiles.
		DeviceParameter dimParam;
		RockerSwitchAction dimAction;
		RockerSwitchAction dimActionOld;
		boolean dimUp;
		if (button == RockerSwitchButton.AI || button == RockerSwitchButton.AO) {
			dimParam = DeviceParameter.BUTTON_DIM_A;
			dimUp = getDimUpA().equals(button);
		} else {
			dimParam = DeviceParameter.BUTTON_DIM_B;
			dimUp = getDimUpB().equals(button);
		}
		if (dimUp) {
			dimAction = newState ? RockerSwitchAction.DIM_UP_PRESSED : RockerSwitchAction.DIM_UP_RELEASED;
		} else {
			dimAction = newState ? RockerSwitchAction.DIM_DOWN_PRESSED : RockerSwitchAction.DIM_DOWN_RELEASED;
		}
		if (button == RockerSwitchButton.AI || button == RockerSwitchButton.AO) {
			dimActionOld = actionA;
			actionA = dimAction;
		} else {
			dimActionOld = actionB;
			actionB = dimAction;
	}
		parameterChangedSupport.fireParameterUpdated(dimParam, initiation, dimActionOld, dimAction);
	}
	
	private boolean buttonChanged(final boolean onlyIfLastIsKnown,
            final Boolean curStateButtonPressed,
            final Boolean newStateButtonPressed) {
		return newStateButtonPressed != null
			&& (!onlyIfLastIsKnown || curStateButtonPressed != null)
			&& !newStateButtonPressed.equals(curStateButtonPressed);
	}
	
	private boolean handleNewStateButton(final RockerSwitchButton button,
            final RockerSwitchState newState,
            final boolean onlyIfLastIsKnown) {
		final Boolean curStateButtonPressed = state.get(button);
		final Boolean newStateButtonPressed = newState.get(button);
		
		if (buttonChanged(onlyIfLastIsKnown, curStateButtonPressed, newStateButtonPressed)) {
			state.set(button, newStateButtonPressed);
			if(button == RockerSwitchButton.AI) {
				state.set(RockerSwitchButton.AO, !newStateButtonPressed);
			}
			else if(button == RockerSwitchButton.AO) {
				state.set(RockerSwitchButton.AI, !newStateButtonPressed);
			}
			else if(button == RockerSwitchButton.BI) {
				state.set(RockerSwitchButton.BO, !newStateButtonPressed);
			}
			else {
				state.set(RockerSwitchButton.BI, !newStateButtonPressed);
			}			
			buttonFireChange(button, newStateButtonPressed);
			return true;
		} 
		else {
			return false;
		}
	}
	
	/**
     * This function handle the state change of a rocker switch.
     *
     * @param newState          The current state that was created by package inspection.
     * @param onlyIfLastIsKnown Save the current state only, if the last state was known.
     */
    private void handleNewState(final RockerSwitchState newState,
                                final boolean onlyIfLastIsKnown) {
        try {
            boolean changed = false;

            final RockerSwitchState oldState = state.clone();

            changed |= handleNewStateButton(RockerSwitchButton.AI, newState, onlyIfLastIsKnown);
            changed |= handleNewStateButton(RockerSwitchButton.AO, newState, onlyIfLastIsKnown);
            changed |= handleNewStateButton(RockerSwitchButton.BI, newState, onlyIfLastIsKnown);
            changed |= handleNewStateButton(RockerSwitchButton.BO, newState, onlyIfLastIsKnown);
            if (changed) {
            	
                final List<RockerSwitchButton> pressed = new LinkedList<>();
                final List<RockerSwitchButton> released = new LinkedList<>();

                RockerSwitchState.getChanges(oldState,
                                             state,
                                             pressed, released);

                parameterChangedSupport.fireParameterUpdated(null, null, oldState, state);
            }

        } catch (final CloneNotSupportedException ex) {
            LOGGER.debug("Catched exception: {}", ex);
        }
    }
    
    @Override
	public void parseRadioPacketRPS(RadioPacketRPS packet) {
		
		final UserDataEEPF60203 data = new UserDataEEPF60203(packet.getUserDataRaw());
		final RockerSwitchState newState = new RockerSwitchState();
		final boolean pressed = true;
		try {
            newState.set(data.getRockerAction(), pressed);
            handleNewState(newState, false);
        } catch (final UserDataScaleValueException ex) {
            LOGGER.debug("Catched exception: {}", ex);
        }
	}
	
	@Override
	public void parseRadioPacket(RadioPacket packet) {
		 if (packet instanceof RadioPacketRPS) {
			 parseRadioPacketRPS((RadioPacketRPS) packet);
	        } else {
	            LOGGER.warn("Don't know how to handle radio choice {}", String.format("0x%02X", packet.getChoice()));
	        }
	}

	@Override
	protected void fillParameters(Set<DeviceParameter> params) {
		params.add(DeviceParameter.BUTTON_DIM_A);
        params.add(DeviceParameter.BUTTON_DIM_B);
		
	}
	
	private void setByParameterButtonDim(final RockerSwitchButton buttonUp,
            final RockerSwitchButton buttonDown,
            final RockerSwitchAction action) {
		
		RockerSwitchButton button;		
		switch (action) {
			case DIM_UP_PRESSED:
				button = buttonUp;	
				break;
			case DIM_UP_RELEASED:
				button = buttonUp;
				break;
			case DIM_DOWN_PRESSED:
				button = buttonDown;
				break;
			case DIM_DOWN_RELEASED:
				button = buttonDown;
				break;
			default:
				LOGGER.warn("Unhandled rocker action ({}).", action);
				return;
		}

		setButton(button);
	}
	
	@Override
    public Object getByParameter(final DeviceParameter parameter) throws IllegalDeviceParameterException {
        switch (parameter) {
            case BUTTON_DIM_A:
                return actionA;
            case BUTTON_DIM_B:
                return actionB;
            default:
                return super.getByParameter(parameter);
        }
    }
	
	
	
	@Override
    public void setByParameter(final DeviceParameter parameter, final Object value) throws IllegalDeviceParameterException {
        assert DeviceParameter.getSupportedClass(parameter).isAssignableFrom(value.getClass());
       
            switch (parameter) {
                case BUTTON_DIM_A:
                    setByParameterButtonDim(getDimUpA(), getDimDownA(), (RockerSwitchAction) value);
                    break;
                case BUTTON_DIM_B:
                    setByParameterButtonDim(getDimUpB(), getDimDownB(), (RockerSwitchAction) value);
                    break;
                default:
                    super.setByParameter(parameter, value);
                    break;
            }
    }

}
