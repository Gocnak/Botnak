Botnak
======

A Java-based IRC chat client with bot capabilities and a focus on Twitch.tv streams.

A full example list of commands and such can be found at http://bit.ly/1366RwM.

TODO:
- Code the ability to join SRL race chats
- New-line detection for commands - perhaps typing a special character (like '[' or something) in the message text.
- After a certain amount of lines (like, a LOT of lines), clear the chat (intelligently, keep the lines on screen) to prevent memory/lag issues
- Make the viewer branch Kappa

Some useful tips:

FACES: Botnak supports all Twitch faces. He downloads them and puts them in the "Face" folder on a seperate thread. Faces will not work until he's done so, and he will print out "Done downloading faces." in the panel. See: http://puu.sh/40H6D.png

SET THE DEFAULT DIRECTORIES: Click the "Defaults Settings" button in the main GUI and set the default face and sound to a Dropbox directory, which is recommended so that you can invite other people to it and they can add faces/sounds while you stream.

FILE SIZES/LENGTH: Try to keep faces below 26 px in height, with size 18 font it fits well enough. Sound files should not be any longer than 5 seconds unless a special case.

OTHER CHANNELS: Botnak supports other channels, but it's wise to only join a few at a time to prevent potential lag/memory issues.


Credits:

Chatterbot API for making Botnak come alive in chats - https://code.google.com/p/chatter-bot-api/

JSON Library for making Twitch parsing easier - https://github.com/douglascrockford/JSON-java

Scalr API for Image Scaling - https://github.com/thebuzzmedia/imgscalr/

Pircbot API for giving me headaches - http://www.jibble.org/pircbot.php

Dr. Kegel from my Twitch chat for fixing them - http://www.twitch.tv/dr_kegel

