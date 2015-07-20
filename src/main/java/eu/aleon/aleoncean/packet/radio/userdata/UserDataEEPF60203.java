package eu.aleon.aleoncean.packet.radio.userdata;

import eu.aleon.aleoncean.packet.radio.RadioPacketRPS.NUState;
import eu.aleon.aleoncean.packet.radio.RadioPacketRPS.T21State;
import eu.aleon.aleoncean.values.RockerSwitchButton;

/**
 * 
 * @author Manuel Klimek <e1029112@student.tuwien.ac.at>
 */
public class UserDataEEPF60203 extends UserDataRPS{
	
	
	public static final RockerSwitchButton DIM_A_UP = RockerSwitchButton.AO;
    public static final RockerSwitchButton DIM_A_DOWN = RockerSwitchButton.AI;
    public static final RockerSwitchButton DIM_B_UP = RockerSwitchButton.BO;
    public static final RockerSwitchButton DIM_B_DOWN = RockerSwitchButton.BI;
    
    public UserDataEEPF60203() {
    	super(T21State.PTM_TYPE2, NUState.NORMALMESSAGE);
    }
    
    public UserDataEEPF60203(final byte[] eepData) {
    	super(eepData, T21State.PTM_TYPE2, NUState.NORMALMESSAGE);
    }
    
    public RockerSwitchButton getRockerAction() throws UserDataScaleValueException {
        final int range = (int) getDataRange(0, 7, 0, 0);
        RockerSwitchButton buttonState = null;
        switch(range)
        {
        	//0x30 Button A0:
	        case 48:
	        	buttonState = RockerSwitchButton.AO;
	            break;
	       //0x10 Button A1:
	        case 16:
	        	buttonState = RockerSwitchButton.AI;
	            break;
	          //0x70 Button B0 (Dim Light up):
	        case 112:
	        	buttonState = RockerSwitchButton.BO;
	            break;
	          //0x50 Button B1 (Dim light down):
	        case 80:
	        	buttonState = RockerSwitchButton.BI;
	            break;
	        default:
	            break;
        	
        }
        if (buttonState == null) {
            throw new UserDataScaleValueException(String.format("Button state unknown (%d).", range));
        }
        return buttonState;
    }
    
    public void setRockerAction(final RockerSwitchButton button) {
    	Integer range = 0;
    	if(button == RockerSwitchButton.AO) {
    		range = 48;		//0x30 Button A0
		}
		else if(button == RockerSwitchButton.AI) {
			range = 16;		//0x10 Button A1
		}
		else if(button == RockerSwitchButton.BO) {
			range = 112;	//0x70 Button B0 (Dim Light up)
		}
		else {
			range = 80;	 	//0x50 Button B1 (Dim light down)
		}
    	setDataRange(range.longValue(), 0, 7, 0, 0);
    }

}
