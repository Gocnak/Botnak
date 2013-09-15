Botnak
======

A Java-based IRC chat client with bot capabilities and a focus on Twitch.tv streams.

The latest (pre-compiled) build can be found here: https://www.dropbox.com/s/24jagzp0uyryqd0/Botnak.jar

A full example list of commands and such can be found at http://bit.ly/1366RwM.

TODO:
- Code the ability to join SRL race chats
- New-line detection for commands - perhaps typing a special character (like '[' or something) in the message text.
- Make the viewer branch - contemplating this -- just use a viewer account only?

Some useful pieces of advice:

BLAMETWITCH: If you cannot login (logging in freezes for a long time) on a recently-made account, try resetting your password for the account, or look at this help topic for the solution: http://help.twitch.tv/customer/portal/questions/1366327-can-t-connect-to-irc-with-new-account-

IN YO FACE: Botnak supports all Twitch faces, including Subscriber faces. He downloads them and puts them in the "Botnak/Face/" folder on a seperate thread. Faces will not work until he's done so, and he will print out "Done downloading faces." in the panel. See: http://puu.sh/40H6D.png

YOU NEED STANDARDS: Click the "Defaults Settings" button in the main GUI and set the default face and sound to a Dropbox directory, which is recommended so that you can invite other people to it and they can add faces/sounds while you stream.

SIZE MATTERS: Try to keep face images below 26 px in height, with size 18 font it fits well enough. Sound files should not be any longer than 5 seconds unless they're a special case.

AND YOUR FRIENDS, TOO: Botnak supports other channels. Though please note, the drop-down menu of the channels in the main interface is a filter, and will only display the current selected stream's messages.

KEEPIN' IT CLEAN: Botnak automatically deletes chat history (lines not on screen) after about 100 lines have been logged. As of right now, there is no method in place to save this text elsewhere, but it is certainly coming in the future.


Credits:

Chatterbot API for making Botnak come alive in chats - https://code.google.com/p/chatter-bot-api/

JSON Library for making Twitch parsing easier - https://github.com/douglascrockford/JSON-java

JTattoo for making Botnak look pretty - http://www.jtattoo.net/

Scalr API for Image Scaling - https://github.com/thebuzzmedia/imgscalr/

Pircbot API for giving me headaches - http://www.jibble.org/pircbot.php

Dr. Kegel from my Twitch chat for fixing them - http://www.twitch.tv/dr_kegel

