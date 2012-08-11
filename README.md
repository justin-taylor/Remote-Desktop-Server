# Remote Desktop Server  v1.5

Copyright 2010 Justin Taylor
This software can be distributed under the terms of the
GNU General Public License. 

Receives messages via UDP socket alowing a remote device to control any
computer's peripherals.

The server has a UI that allows the user the specify a port to listen on,
then waits for a connection from the Andoird device. There are two classes
to the server.

## Deliverables
What's that? You just want to use the app!?! You don't want to mess with the
hassle of eclipse? In that case just download
http://central.tayloredapps.org/server.jar and run it on any desktop that has
JRE installed. Then mosy on over and download
http://central.tayloredapps.org/Android-remote-client.apk to your android
device.

Now that you have everything you need, open the server on the Desktop you are
trying to connect to and click connect. Then open up the app on your mobile
deivce. Make sure the IP address field matches the IP listed on your desktop
server. Then click connect. Now, you are ready to control your dekstop.

1. ServerWindow
This class contains both the GUI and the server. Messages from
the Android device is received on the server thread then passed
to and AutoBot object (See section 1.b below).

1. AutoBot
Receives messages from the server thread and translates them to
I/O interactions (Move Mouse, Keyboard key stroke). The messages constant file
defines what messages are recognized.
