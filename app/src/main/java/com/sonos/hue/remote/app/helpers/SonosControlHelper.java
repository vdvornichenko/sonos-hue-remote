package com.sonos.hue.remote.app.helpers;

import java.io.IOException;

import com.github.kilianB.exception.SonosControllerException;
import com.github.kilianB.sonos.SonosDevice;

import io.github.zeroone3010.yahueapi.DimmerSwitchButtonEvent;

public class SonosControlHelper {
	
	private static final int SMALL_DELTA = 1;
	private static final int MEDIUM_DELTA = 3;
	private static final int VOLUME_MAX = 100;
	
	public static void sendSonosAction(SonosDevice device, DimmerSwitchButtonEvent event) throws IOException, SonosControllerException {
		try {
			switch (event.getButton().name()) {
			case "ON":
				handleON(device, event);
				break;
			case "OFF":
				handleOFF(device);
				break;
			case "DIM_UP":
				handleHandleUP(device, event);
				break;
			case "DIM_DOWN":
				handleHandleDOWN(device, event);
				break;
			}
		} catch (Exception e) {
			System.out.println("Cannot send the request!" + e.getMessage());
		}
	}

	private static void handleON(SonosDevice device, DimmerSwitchButtonEvent event) throws IOException, SonosControllerException {
		switch (event.getAction().name()) {
			case "HOLD":
				device.next();
			case "LONG_RELEASED":
				break;
			case "SHORT_RELEASED":
			case "INITIAL_PRESS":
				switch (device.getPlayState().name()) {
					case "PAUSED_PLAYBACK":
					case "TRANSITIONING":
					case "STOPPED":
						device.play();
						break;
					default:
						device.next();
						break;
				}
				break;
		}
	}

	private static void handleOFF(SonosDevice device) throws IOException, SonosControllerException {
		device.pause();
	}

	private static void handleHandleUP(SonosDevice device, DimmerSwitchButtonEvent event) throws IOException, SonosControllerException {
		if (device.getVolume() < VOLUME_MAX) {
			switch (event.getAction().name()) {
				case "HOLD":
					if (device.getVolume() < VOLUME_MAX - MEDIUM_DELTA) {
						device.setVolume(device.getVolume() + MEDIUM_DELTA);
					} else {
						device.setVolume(device.getVolume() + SMALL_DELTA);
					}
					break;
				case "LONG_RELEASED":
					break;
				case "SHORT_RELEASED":
				case "INITIAL_PRESS":
					device.setVolume(device.getVolume() + SMALL_DELTA);
					break;
			}
		}
	}
	
	private static void handleHandleDOWN(SonosDevice device, DimmerSwitchButtonEvent event) throws IOException, SonosControllerException {
		if (device.getVolume() > 0) {
			switch (event.getAction().name()) {
				case "HOLD":
					if (device.getVolume() < MEDIUM_DELTA) {
						device.setVolume(device.getVolume() - SMALL_DELTA);
					} else {
						device.setVolume(device.getVolume() - MEDIUM_DELTA);
					}
					break;
				case "LONG_RELEASED":
					break;
				case "SHORT_RELEASED":
				case "INITIAL_PRESS":
					device.setVolume(device.getVolume() - SMALL_DELTA);
					break;
			}
		}
	}
}
