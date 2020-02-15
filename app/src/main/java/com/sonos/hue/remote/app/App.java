package com.sonos.hue.remote.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.kilianB.exception.SonosControllerException;
import com.github.kilianB.sonos.SonosDevice;
import com.github.kilianB.sonos.SonosDiscovery;
import com.sonos.hue.remote.app.helpers.SonosControlHelper;

import io.github.zeroone3010.yahueapi.DimmerSwitch;
import io.github.zeroone3010.yahueapi.DimmerSwitchButtonEvent;
import io.github.zeroone3010.yahueapi.Hue;

public class App {
	static final String bridgeIp = "10.10.0.21"; // Fill in the IP address of your Bridge
	static final String apiKey = ""; // Fill in an API key to access your Bridge
	static final Hue hue = new Hue(bridgeIp, apiKey);
	static final int INTERVAL = 1000;

	public static void main(String[] args) throws InterruptedException, IOException, SonosControllerException {
		if (args.length == 0 || args[0] == "help") {
			System.out.println("Please specify the devices in the following format:\n '<dimmer_name>:<sonos_name>' in single quoutes, \n where\n <dimmer_name> - name of the Hue Dimmer Swith,\n <sonos_name> - name of the Sonos Product to control,\n 'Bathroom - Sonos:Bathroom';'Master Bathroom - Sonos:Master Bathroom';'Living Room - Sonos:Living Room (LF,RF)'");
		}
		Map<String, String> controllingMap = null;
		try {
			controllingMap = handleInputParams(args);		
		} catch (Exception e) {
			System.out.println(e.getMessage()+e.getCause());
		}
		finally {
			if (controllingMap == null)
				return;
		}
		worker(controllingMap);
	}
	
	public static Map<String, String> handleInputParams(String[] args) {
		String inputArguments = String.join("",args);
		String[] argsList = inputArguments.split(";");
		Map<String, String> controllingMap = new HashMap<String, String>();
		for (int i = 0; i < argsList.length; i++) {
			if (argsList[i] != null) {
				String[] arg = argsList[i].split(":");
				if (arg.length > 0)
					controllingMap.put(arg[0].replace("\'",""),arg[1].replace("\'",""));
			}
		}
		System.out.println('%'+controllingMap.toString()+'%');
		return controllingMap;
	}
	
	public static Map<String, SonosDevice> populateSonosDevicesMap(Map<String, String> controllingMap) throws IOException {
		Map<String, SonosDevice> sonosDeviceMap = new HashMap<String, SonosDevice>();
		List<SonosDevice> devices = SonosDiscovery.discover();
		for (SonosDevice device : devices) {
			for (String deviceName : controllingMap.values()) {
				try {
					if (deviceName.equalsIgnoreCase(device.getDeviceName())) {
						sonosDeviceMap.put(deviceName, device);
					}
				} catch (Exception e) {
					System.out.println("Cannot connect due to " + e.getCause().toString());
				}
			}
		}
		return  sonosDeviceMap;
	}
	
	public static void worker(Map<String, String> controllingMap) throws InterruptedException, IOException {
		Map<String, SonosDevice> sonosDeviceMap = populateSonosDevicesMap(controllingMap);
		Map<String, DimmerSwitch> hueDeviceMap = new HashMap<String, DimmerSwitch>();
		Map<String, String> dimmerToggleStatuses = new HashMap<String, String>();

		while (true) {
			
			for (String hueRemote : controllingMap.keySet()) {
				Optional<DimmerSwitch> presentDimmer = hue.getDimmerSwitchByName(hueRemote);
				DimmerSwitch dimmer = presentDimmer.get();
				hueDeviceMap.put(hueRemote, dimmer);
			}

			for (String hueRemote : hueDeviceMap.keySet()) {
				DimmerSwitch dimmer = hueDeviceMap.get(hueRemote);
				SonosDevice device = sonosDeviceMap.get(controllingMap.get(hueRemote));
				String lastUpdated = dimmerToggleStatuses.get(hueRemote);

				try {
					DimmerSwitchButtonEvent event = dimmer.getLatestButtonEvent();

					if (lastUpdated == null) {
						lastUpdated = dimmer.getLastUpdated().toString();
						dimmerToggleStatuses.put(hueRemote, lastUpdated);
					}

					if (dimmer.getLastUpdated().toString().equalsIgnoreCase(lastUpdated)) {
						continue;
					}

					SonosControlHelper.sendSonosAction(device, event);
					lastUpdated = dimmer.getLastUpdated().toString();
					dimmerToggleStatuses.put(hueRemote, lastUpdated);
				} catch (Exception e) {
					System.out.println("Wrong event. "+e.getMessage());
				}
			}
			Thread.sleep(INTERVAL);
		}
	}
}
