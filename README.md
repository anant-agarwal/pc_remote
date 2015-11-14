# pc_remote
Control PC from your android phone

Some details: 
* Windows server was built in C# in visual studio.
* Server is using Bluetooth SPP hence the SPP GUID is hardcoded and should work out of the box for any android device.
* Server starts a background thread which will listen to instructions coming from android app
* Server is using AutoIt3 to send instructions to the app running in foreground 
* Android app shows all the controls available and you can just click on them
* The set of controls can easily be extended.

Currently the app can send in the following keystrokes/function: 
* Left
* Right
* F5 //used for entering presentation mode
* Esc
* ctrl Esc
* Sleep windows pc
* Hibernate windows pc
* Restart windows pc
* Shut Down windows pc
* Go to Desktop on windows pc
* Lock windows pc

Future work:
* Add more useful features
* Convert Mobile phone into game pad //this can be tricky as autoit is slow in executing key strokes, and hence continuous key press (e.g. accelerating in Need For Speed) doesnt work very well.
