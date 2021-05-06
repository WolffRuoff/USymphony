# BergBot

Welcome to BergBot! BergBot is a comprehensive portfolio manager that can be controlled in Symphony. Watch this video for a complete walkthrough:

[![Link to Video Walkthrough](http://img.youtube.com/vi/qE9LzU_Czsk/0.jpg)](http://www.youtube.com/watch?v=qE9LzU_Czsk "BergBot Walkthrough")

## Requirements
* JDK 8+
* Maven 3
* Symphony Pod 1.55.3 or later
* Symphony API Agent 2.55.9 or later

## Setup
Fill up `src/main/resources/config.json` with the appropriate values for pod information, bot username, 
and service account details 
```json5
{
    "sessionAuthHost": "[pod].symphony.com",
    "sessionAuthPort": 443,
    "keyAuthHost": "[pod].symphony.com",
    "keyAuthPort": 443,
    "podHost": "[pod].symphony.com",
    "podPort": 443,
    "agentHost": "[pod].symphony.com",
    "agentPort": 443,
    "botUsername": "[bot-name]",
    "botEmailAddress": "[bot-name]@bots.symphony.com",
    "botPrivateKeyPath": "rsa/",
    "botPrivateKeyName": "private-key.pem"
}
```

Create a folder in root called `rsa` and place your private and public keys in. Make sure your private key name is `private-key.pem`

Lastly navigate to `src/main/java/com/muhlenberg/bot/listeners/BergBotListener.java` and replace `Bergbot` on line 32 to the username of your bot. Line 32 should then follow the below format with `[bot-name]` being the username of your bot. 
``` java
if(!msgParts[0].equals("@[bot-name]") || msgParts[1].charAt(0) != '/'){
```
If your bot's username includes spaces, then add the following code before line 32:
``` java
int botLength = [number of words in the bot username];
for(int j = 1; j < botLength; j++){
  msgParts[0] += " " + msgParts[1];
  for (int i = 1; i < msgParts.length - 1; i++) {
    msgParts[i] = msgParts[i + 1];
  }
}
```
