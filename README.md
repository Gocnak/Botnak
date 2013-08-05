Botnak
======

A Java-based IRC chat client with bot capabilities and a focus on Twitch.tv streams.

A full example list of commands and such can be found at http://bit.ly/1366RwM

TODO:
- Code the ability to join SRL race chats
- After a certain amount of lines (like, a LOT of lines), clear the chat (intelligently, keep the lines on screen) to prevent memory/lag issues
- Hardcode all of the Twitch faces (Sub emotes and the kind) -- will do this when I make the viewer branch.
- Make the viewer branch Kappa

Some useful tips:

SET THE DEFAULT DIRECTORIES: Click the "Defaults Settings" button in the main GUI and set the default face and sound to a Dropbox directory, which is recommended so that you can invite other people to it and they can add faces/sounds while you stream.

FILE SIZES/LENGTH: Try to keep faces below 26 px in height, with size 18 font it fits well enough. Sound files should not be any longer than 5 seconds unless a special case.

OTHER CHANNELS: Botnak supports other channels, but it's wise to only join a few at a time to prevent potential lag/memory issues.


Credits:  
Chatterbot API for making Botnak come alive in chats - https://code.google.com/p/chatter-bot-api/  
Pircbot API for giving me headaches - http://www.jibble.org/pircbot.php  
Dr. Kegel from my Twitch chat for fixing them - http://www.twitch.tv/dr_kegel
