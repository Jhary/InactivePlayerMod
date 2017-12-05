### Description
What this mod does is essentially a lookup in the database for which players are inactive for the minimum amount of days configured 
and then dumping the informationinto a formatted textfile in the server root folder. 
 
It will never, at any point, write anything to the database. It just performs a safe lookup through the wurmplayers.db and wurmzones.db
 

#### The collected informations are:
- Name of the player
- Total days played (if 0 the player has just played less than 24 hours)
- Total days inactive
- Name of the deed the player is the mayor of


#### Usage:
1. Download the mod
2. Unzip the contents of the zip file into your server mods folder
3. Make sure its active in the properties file and set the inactiveDays property to a value of your liking
4. Run the server
5. ....
6. Profit :P
