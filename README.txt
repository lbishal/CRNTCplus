CRN Toolbox Center 1.0
Copyright 2010-2011 Jakob Weigert, University of Passau
weigert@fim.uni-passau.de
===================================================================


Description of purpose
-------------------------------------------------------------------

The CRN Toolbox Center is an Android application which contains
the Context Recognition Network Toolbox (CRN Toolbox, developed by
David Bannach, University of Passau) as a native library.

The purpose of the CRN Toolbox is to receive sensor data of various
types, process, combine, filter and classify it as wished and
transmit it to various types of receivers (e.g. via TCP or UDP).
The CRN Toolbox therefore is started with a JSON config file that
describes which tasks (sensor receivers and output writers) are to
create and which connections there should be made between them.

The purpose of the CRN Toolbox Center is to make the functionality
of the CRN Toolbox available on the Android system.



Features
-------------------------------------------------------------------

The CRN Toolbox Center includes the following features
regarding the CRN Toolbox:

- Display information about the native Toolbox library (version, ...)

- Start the CRN Toolbox with a JSON configuration file

- Start DirectInput which delivers data of typical Android sensors
  to the toolbox (the DirectInput IDs are pre-defined, e.g. "AccInput")

- Start the CRN Toolbox with a control port

- Request and display the current Toolbox log messages



Installation
-------------------------------------------------------------------

- This application requires Android version 2.1 or higher.

- On your Android device: Activate the accepting of unknown sources 
  (Settings -> Applications).

- Copy the file "CRN Toolbox Center.apk" from the "bin" directory
  to your Android device. Install it by tapping on it.

||| If there is a problem during installation, please report it to
||| weigert@fim.uni-passau.de - thanks!

- There are sample config files in the folder "sample_config_files".
  - Adjust the IPs (and ports) in the config files to match to your
    current network configuration and note them.
  - Copy the folder to your android device.



Using the CRN Toolbox Center
-------------------------------------------------------------------

- Enable WiFi if you wish to receive sensor data via your network.

- Enable GPS if you wish to use it as input for the Toolbox.

- Start the application by tapping on the "CRN Toolbox Center" icon
  in "all applications".

||| If there is a problem when starting the application, please report it to
||| weigert@fim.uni-passau.de - thanks!

- Try to start the Toolbox with one of the sample config files:
	- Touch the tab "ConfigFile"
	- Touch the "Browse..." button
	- Navigate to the sample_config_file folder you copied
	  (path to SD card: /mnt/sdcard/)
	- Choose the config file you want to use
	- Touch the "Start Toolbox..." button
	
- Try to receive the data from the Toolbox
	- For TCP you can just use telnet (or a telnet app on Android)
	- For UDP, use a UDP Server

- Just try it out ;)



Available Toolbox features
-------------------------------------------------------------------

The included Toolbox library has compiled-in the following classes:

- All standard tasks (/std-tasks)
- All classes in /devel/esl

You can build-in additional features into the Toolbox library:
Have a look at "HOWTO build the library yourself.txt".



Known bugs
-------------------------------------------------------------------

None at the moment.

||| Please send any questions, comments and bugs to
||| weigert@fim.uni-passau.de - thanks!



