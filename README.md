*AssassinMinigame is a plugin that integrates a minigame called Assassin into your survival server.*

###How to play Assassin###
All of the players playing assassin are teleported to the game coordinator. They are given 30 seconds to spread out. After 30 seconds, one player is chosen to be the Assassin, and he must kill all of the other players in the minigame to win. The assassin must kill secretly, so the other players don't know who is the assassin.

However, one player is chosen to be the archer, and this player must find the assassin and kill him so he and the rest of the players (the civilians) can win. If the assassin kills the archer, another player must retrieve the dropped infinity bow and arrow and then that player becomes the archer.

Every 15 seconds, two items spawn around the players. It is either a bow or an arrow, and the assassin cannot pick up these items; only civilians can, so if the assassin takes too long to kill all the players, the civilians have a chance to kill him if the archer doesn't.

You can view a video of gameplay here:

https://www.youtube.com/watch?v=JCCRBA_L9uA

###Getting in the Game###
This plugin was built so that Assassin could be played on a single-world survival server. To play the game, players must type "/assassin join". The server will announce that this player is playing Assassin, and "(In Minigame)" will be appended to his username on the tab list. Once there are at least three players playing Assassin, a player must type "/assassin start" to start the game.

###Scores###
Players are rewarded for winning Assassin. Civilians who kill other civilians with their bows will be punished. The civilian who kills the assassin will be rewarded 10 points, and every other civilian will be rewarded 5. If the assassin wins, he will be rewarded 5 points, plus 2 points for each players he kills. If a civilian shoots another civilian with a bow, the killer will lose 10 points.

The top five scores plus your own can be viewed by typing "/assassin leaderboards"

###Map###
You can use a single map with this plugin. Just type "/assassin spawn" to set the spawn point of the map. When players join or start the game, they will be teleported to the spawn location. When they leave, they will be teleported to their bed spawn location.

###The Game Coordinator###
If there are no players in the minigame, the first one who does becomes the game coordinator. This player currently has no permissions. The game coordinator will be able to do much more than this in the future, such as kick a player from the minigame, give the game coordinator status to another player, choose the map, etc. However, these features have not been added yet.

###Dependencies###
* [TitleAPI](https://www.spigotmc.org/resources/titleapi-1-8-1-9.1325/) This is required if you wish to display titles during the game. The titles will tell each player at the beginning of the game whether they are a civilian, the archer, or the assassin. The plugin will still work without this dependency, but there won't be titles.

###Additional Info###
* This plugin creates the team "Assassin" from the server's main scoreboard, plus a new objective called "assassinScore". If you are currently using these on your server, using this plugin is not recommended.
