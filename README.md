Botnak
======

A Java-based IRC chat client with bot capabilities and a focus on Twitch.tv streams.

#Download:
The latest (pre-compiled) build can be found here: https://www.dropbox.com/s/24jagzp0uyryqd0/Botnak.jar

Just download that and double click it to run it. You need the latest Java version in order to run it.

A full example list of commands and such can be found at http://bit.ly/1366RwM.

TODO:
- Code the ability to join SRL race chats
- Tab/channel combination
- Subscriber detection/support

Some useful pieces of advice:

#BLAMETWITCH:
LOGGING IN REQUIRES YOU TO USE YOUR OAUTH KEY AS A PASSWORD NOW! You can find your account's OAuth key by going to http://twitch.tv/gocnak?chat_debug=true and finding it in the chat. USE THIS (the entire oauth key, ex: "oauth:blahblahblahblahbl344") FOR YOUR PASSWORD ON BOTNAK! For more information: http://help.twitch.tv/customer/portal/articles/1302780-twitch-irc

#IN YO FACE:
Botnak supports all Twitch faces, including Subscriber faces. He downloads them and puts them in the "Botnak/Face/" folder on a separate thread. Faces will not work until he's done so, and he will print out "Done downloading faces." in the panel. See: http://puu.sh/40H6D.png

#YOU NEED STANDARDS:
Click the "Settings" button in the main GUI and set the Default Face and Sound directories to a Dropbox directory, which is recommended so that you can invite other people to it and they can add faces/sounds while you stream.

#SIZE MATTERS:
Faces are automatically scaled to 26 pixels in height when downloaded, and also scaled based on font size. Sound files should not be any longer than 5 seconds unless they're a special case.

#AND YOUR FRIENDS, TOO:
Botnak supports other channels. Each channel has its own tab, and it is wise to only join a few channels at a time.

#KEEPIN' IT CLEAN:
Botnak is able to delete chat history (lines not on screen) after a specified number of lines have been logged. The minimum clear int is 40 lines, with no maximum. You can set this value in the Settings GUI.

#FOR THE RECORD...
Botnak also supports logging the session's chats to file. If selected, Botnak will print the chats to text files in Botnak/Logs/ under the folder session#-dd-mm-yy . If you also have the chat cleared option enabled, don't worry, as Botnak logs the cleared text to file before deleting it.

#UP TO DATE:
Botnak automatically checks to see if there's a new version available and lets you know!

Credits:

Chatterbot API for making Botnak come alive in chats - https://code.google.com/p/chatter-bot-api/

JSON Library for making Twitch parsing easier - https://github.com/douglascrockford/JSON-java

JTattoo for making Botnak look pretty - http://www.jtattoo.net/

Scalr API for Image Scaling - https://github.com/thebuzzmedia/imgscalr/

Pircbot API for giving me headaches - http://www.jibble.org/pircbot.php

Dr. Kegel from my Twitch chat for fixing them - http://www.twitch.tv/dr_kegel

