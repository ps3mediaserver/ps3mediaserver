README
------

This directory contains the renderer configuration profiles for all devices that
PS3 Media Server supports. Every configuration profile serves two purposes:

  - Allow PMS to recognize a specific renderer when it tries to connect
  - Define the possibilities of that renderer

For a detailed description of all available options in a configuration file,
examine "PS3.conf".


MAKING PMS SUPPORT YOUR RENDERER
--------------------------------

Sometimes PMS does not recognize your renderer, or it wrongly identifies it.
In this case you can try creating your own renderer configuration file.

The steps below explain how to create a basic .conf file.

1) Shut down PMS.

2) Create a directory named "backup" and copy all .conf files to it.

3) Remove all .conf files from the "renderers" directory. 

4) Choose the .conf file that matches your device most. For example, if your
   Samsung TV is not recognized, "Samsung.conf" might be a good place to
   start from. If you do not know which one to choose, pick "PS3.conf" as
   starting point.

5) Copy the chosen .conf file to the "renderers" directory and rename it as
   your device.

You now have created a setup where PMS cannot be distracted by settings in
other configuration files, ideal for experimentation.

Now it is time to figure out how PMS can recognize your renderer. When your
device tries to connect to PMS or tries to play a file, it sends identifying
information to PMS. You need to know this information and use it in your
.conf file. To intercept the information you need to dumb down PMS and crank
up its logging information temporarily:

6) In your .conf file, look for the line that define "UserAgentSearch" and
   change it to:

	UserAgentSearch = This should not match anything

7) Look for "UserAgentAdditionalHeader" and "UserAgentAdditionalHeaderSearch"
   as well, make sure they are empty:

	UserAgentAdditionalHeader =
	UserAgentAdditionalHeaderSearch = 

8) In the PMS main directory, edit the file "logback.xml". Change for the
   appender with name "debug.log" the level to "TRACE":

	<appender name="debug.log" class="ch.qos.logback.core.FileAppender">
	  <!-- only log event DEBUG and higher to the debug.log. -->
	  <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
	    <level>TRACE</level>
	  </filter>

PMS has now been prepared to not recognize your device at all and it will
log all trace information to the "debug.log" logfile.

9) Start up PMS.

10) Connect your device to PMS. If possible, try to browse PMS and play
    some media as well. Chances are it does not work at all. This is fine,
    since we are only after logging information.

11) Edit the "debug.log". If you do not know where it lives on your file
    system, there is a button "debug.log" on the "Traces" tab in PMS that
    will open an editor.

12) Shut down PMS.

Look for lines containing "User-Agent" in your "debug.log" and examine the
"Received on socket" lines, for example:

	[New I/O server worker #1-1] TRACE 11:05:50.702 Received on socket: Date: Sun, 02 Oct 2011 09:12:22 GMT
	[New I/O server worker #1-1] TRACE 11:05:50.702 Received on socket: Host: 192.168.0.16:5001
	[New I/O server worker #1-1] TRACE 11:05:50.703 Received on socket: User-Agent: UPnP/1.0
	[New I/O server worker #1-1] TRACE 11:05:50.703 Received on socket: X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";

The first two lines are not relevant, but the last two are interesting. They
identify the device to PMS. These lines were captured from a PlayStation 3
trying to connect to PMS and play some media files.

When you search the "debug.log", you might find that the identifying
information is not always the same for each request.

For example (edited for readability):

	Received on socket: User-Agent: PLAYSTATION 3
	...
	Received on socket: User-Agent: UPnP/1.0
	Received on socket: X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";
	...    
	Received on socket: User-Agent: UPnP/1.0 DLNADOC/1.50
	Received on socket: X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";

As you can see, the device does not always send the same "User-Agent"
information. Now you need to integrate this knowledge into your .conf file.

13) Extract all different "User-Agent" snippets from the "debug.log" and copy
    them into your .conf file as a mental note. For example:

	# ============================================================================
	# PlayStation 3 uses the following strings:
	#
	# User-Agent: PLAYSTATION 3
	# ---
	# User-Agent: UPnP/1.0
	# X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";
	# ---
	# User-Agent: UPnP/1.0 DLNADOC/1.50
	# X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";
	# ============================================================================

14) Edit the "UserAgentSearch" so it matches the headers that you discovered.
    If there are extra headers that can help with identification, use the
    "UserAgentAdditionalHeader" and "UserAgendAdditionalHeaderSearch" settings
    as well. For the PS3, this results in:

	UserAgentSearch = PLAYSTATION
	UserAgentAdditionalHeader = X-AV-Client-Info
	UserAgentAdditionalHeaderSearch = PLAYSTATION

    These lines should be interpreted as "if the 'User-Agent' header contains
    'PLAYSTATION' or if the 'X-AV-Client-Info' header contains 'PLAYSTATION'
    we have a definite match with the PS3".
    
    Note that we did not try to match "UPnP/1.0". That string is too generic;
    another device might use the same string and PMS would wrongly identify
    it as a PS3. Hence the match for 'PLAYSTION', which is very specific for
    the PS3. Be sure to look for a specific match for your device as well.

Now PMS will be able to positively match your device to your .conf file. From
now on, it will use your .conf file to determine what the device is capable of.
Try to determine what formats your device supports, using its manual or Google.

15) Configure the .conf file for your device. Refer to "PS3.conf" for a detailed
    description of each option. At the very least, make sure you configure these
    settings:

	Video
	Audio
	Image
	MediaInfo
	TranscodeVideo
	TranscodeAudio

    You could uncomment other settings if you are not sure they would work for
    your device.

    Tip: If you define "MediaInfo = true" and do not define any "Supported"
    lines, PMS is forced to transcode everything. This is the best way to
    find out the correct values for "TranscodeVideo" and "TranscodeAudio" for
    your device.

16) Start PMS and connect your device to it. PMS should recognize it now. If
    your device is not recognized, return to step 14).

17) Try to open media. If it does not work, try different settings for the
    "TranscodeVideo" and "TranscodeAudio" options in your .conf file. See
    "PS3.conf" for a detailed description of the options.

From there on, you can tune your .conf file by adding "Supported" lines and
configuring more options. Use the backup .conf files of similar devices for
inspiration.

Restart PMS every time you want to view the results.

Be sure to share your working results on the Alternative Media Renderers forum:
http://www.ps3mediaserver.org/forum/

