package com.jamfsoftware.jssmonitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.jamfsoftware.eventnotifications.JAMFEventNotificationMonitor;
import com.jamfsoftware.eventnotifications.JAMFEventNotificationMonitorResponse;
import com.jamfsoftware.eventnotifications.JAMFEventNotificationParameter;
import com.jamfsoftware.eventnotifications.events.EventType;
import com.jamfsoftware.eventnotifications.events.EventType.EventTypeIdentifier;
import com.jamfsoftware.eventnotifications.events.MobileDeviceCommandCompleted;
import com.jamfsoftware.eventnotifications.shellobjects.MobileDeviceEventShell;
import com.jamfsoftware.eventnotifications.shellobjects.ComputerEventShell;


public class ScriptRunner implements JAMFEventNotificationMonitor {
	
	@Override
	public JAMFEventNotificationMonitorResponse eventOccurred(JAMFEventNotificationParameter param) {
		JAMFEventNotificationMonitorResponse response = new JAMFEventNotificationMonitorResponse(this);
		
		performAction(param.getEventType(), param.getInfoMap());
		
		return response;
	}
	
	public void performAction(EventType e, HashMap<Object, Object> hm){
		boolean commandOccurred = false;
		String arg1DeviceType = "";
		String arg2Identifier = "";
		String arg3Event = "";
		
		//Mobile Device Enroll Handler
		if(e.getIdentifier() == EventTypeIdentifier.MobileDeviceEnrolled && e.getEventObject() instanceof MobileDeviceEventShell){
			commandOccurred = true;
			//Cast to explicit types
			MobileDeviceEventShell mdes = (MobileDeviceEventShell)e.getEventObject();
			//write the data to a log
			writeToLog("==Mobile Device Enrolled==");
			writeToLog("udid: " + mdes.getUdid());
			writeToLog("wifi mac: " + mdes.getWifiMacAddress());
			writeToLog("----------");
			//populate our args
			arg1DeviceType = "MobileDevice";
			arg2Identifier = mdes.getUdid();
			arg3Event = "enrolled";
		}
		
		//Mobile Device Unenroll Handler
		if(e.getIdentifier() == EventTypeIdentifier.MobileDeviceUnEnrolled && e.getEventObject() instanceof MobileDeviceEventShell){
			commandOccurred = true;
			//Cast to explicit types
			MobileDeviceEventShell mdes = (MobileDeviceEventShell)e.getEventObject();
			//write the data to a log
			writeToLog("==Mobile Device Unenrolled==");
			writeToLog("udid: " + mdes.getUdid());
			writeToLog("wifi mac: " + mdes.getWifiMacAddress());
			writeToLog("----------");
			//populate our args
			arg1DeviceType = "MobileDevice";
			arg2Identifier = mdes.getUdid();
			arg3Event = "unenrolled";
		}
		
		//Mobile Device Command Handler
		if(e.getIdentifier() == EventTypeIdentifier.MobileDeviceCommandCompleted && e.getEventObject() instanceof MobileDeviceEventShell){
			commandOccurred = true;
			//Cast to explicit types
			MobileDeviceEventShell mdes = (MobileDeviceEventShell)e.getEventObject();
			MobileDeviceCommandCompleted mdcc = (MobileDeviceCommandCompleted)e;
			//write the data to a log
			writeToLog("==Mobile Device Command Completed==");
			writeToLog("command: " + mdcc.getCommand());
			writeToLog("udid: " + mdes.getUdid());
			writeToLog("resultStatus: " + mdcc.getResultStatus());
			writeToLog("wifi mac: " + mdes.getWifiMacAddress());
			writeToLog("----------");
			//populate our args
			arg1DeviceType = "MobileDevice";
			arg2Identifier = mdes.getUdid();
			arg3Event = mdcc.getCommand();
		}
		
		//Computer Command Handler
		if((e.getIdentifier() == EventTypeIdentifier.ComputerAdded || e.getIdentifier() == EventTypeIdentifier.ComputerCheckIn || e.getIdentifier() == EventTypeIdentifier.ComputerInventoryCompleted) && e.getEventObject() instanceof ComputerEventShell){
			commandOccurred = true;
			//Cast to explicit types
			ComputerEventShell ces = (ComputerEventShell)e.getEventObject();
			//use the data
			writeToLog("==Computer Command Completed==");
			writeToLog("command: " + e.getIdentifier());
			writeToLog("mac: " + ces.getMacAddress());
			writeToLog("udid: " + ces.getUdid());
			writeToLog("----------");
			//populate our args
			arg1DeviceType = "Computer";
			arg2Identifier = ces.getMacAddress();
			arg3Event = e.getIdentifier().toString();
		}
		
		if(commandOccurred){
			//Execute the script
			String pathToScript = "/private/etc/scripts/JAMFEventNotificationHandler.py";
			String stringToExec = "/usr/bin/python " + pathToScript + " " + arg1DeviceType + " " + arg2Identifier + " " + arg3Event;
			
			Runtime cmd = Runtime.getRuntime();
			Process launch;
			
			try {
				writeToLog("Running script " + pathToScript + "...");
				launch = cmd.exec(stringToExec);
				launch.waitFor();
				String exitCode = Integer.toString(launch.exitValue());
				writeToLog("Script complete.  Exit Code: " + exitCode);
			} catch (IOException e1) {
				writeToLog("Error: Could not execute script: " + printStackTrace(e1) + "\n");
			} catch (InterruptedException e1) {
				writeToLog("Error: Could not execute script: " + printStackTrace(e1) + "\n");
			} catch (Exception e1) {
				writeToLog("Error: Could not execute script: " + printStackTrace(e1) + "\n");
			}
			writeToLog("----------");
		}
	}

	@Override
	public boolean isRegisteredForEvent(EventTypeIdentifier e) {
		return true;
	}
	
	private static String printStackTrace(Exception e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	
	private void writeToLog(String s) {
		try {
		    Date now = new Date( );
		    SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss,SSS");
		    s = df.format(now) + " " + s + "\n";
			FileOutputStream fstream = new FileOutputStream("/Library/JSS/Logs/JAMFEventNotificationScriptRunner.log", true);
			fstream.write(s.getBytes(Charset.forName("UTF-8")));
			fstream.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

}
