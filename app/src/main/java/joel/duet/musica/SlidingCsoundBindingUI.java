package joel.duet.musica;

import android.widget.SeekBar;
import android.widget.TextView;

import com.csounds.CsoundObj;
import com.csounds.bindings.ui.CsoundSliderBinding;

import csnd6.CsoundMYFLTArray;
import csnd6.controlChannelType;

/**
 *
 * Created by joel on 19/02/16 at 01:15 at 01:18.
 */
final class SlidingCsoundBindingUI extends CsoundSliderBinding {
    private final SeekBar seekBar;
	private final String channelName;
	private CsoundObj csoundObj;
	private boolean cacheDirty = true;
	private CsoundMYFLTArray ptr = null;
 //public SlidingCsoundBindingUI(){}
    public SlidingCsoundBindingUI(SeekBar seekBar, final String channelName, double min, double max, final TextView gaindb, final boolean isInstr, final int formatId, final String componentName) {

		super();
		this.seekBar = seekBar;
		this.channelName = channelName;
		this.minValue = min;
		this.maxValue = max;

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) {
					double percent = progress / (double)seekBar.getMax();
					double value = (percent * (maxValue - minValue)) + minValue;

					try {
						csoundObj.getCsound().SetChannel(channelName, value);
						if (value != cachedValue) {
							cachedValue = value;
							cacheDirty = true;
						}
						if (!csoundObj.getAsyncStatus()) {
							csoundObj.getCsound().SetChannel(channelName, value);
						}
					} catch (NullPointerException exc){exc.printStackTrace();}

                    double val = progress / (double) seekBar.getMax();
                    gaindb.setText(String.format("%1s", val));

                    if (isInstr) {
                        if (formatId == R.string.master_line_L_format)
                            CSD.instruments.get(componentName).gainL = val;
                        else CSD.instruments.get(componentName).gainR = val;
                    } else {
                        if (componentName.equals("Master")) {
                            if (formatId == R.string.master_line_L_format)
                                CSD.master_gain_L = val;
                            else CSD.master_gain_R = val;
                        } else {
                            if (formatId == R.string.master_line_L_format)
                                CSD.effects.get(componentName).gainL = val;
                            else CSD.effects.get(componentName).gainR = val;
                        }
                    }
				}

			}
		});
	}

	@Override
	public void setup(CsoundObj csoundObj) {
		this.csoundObj = csoundObj;

		double percent = seekBar.getProgress() / (double)seekBar.getMax();
		cachedValue = (percent * (maxValue - minValue)) + minValue;
		cacheDirty = true;

		ptr = this.csoundObj.getInputChannelPtr(channelName, controlChannelType.CSOUND_CONTROL_CHANNEL);

	}

	@Override
	public void updateValuesToCsound() {
		if (cacheDirty) {
			if(ptr != null) ptr.SetValue(0, this.cachedValue);
			cacheDirty = false;
		}
	}

	@Override
	public void cleanup() {
		seekBar.setOnSeekBarChangeListener(null);
		ptr.Clear();
		ptr = null;
	}

}
