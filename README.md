## CS537-Group12 (Wheels,	Spectral Sensor,	Range Extender)
                This is rover game. there are two teams blue crop and Red corp. Each team has many rovers with specific functionalities and competes with each other to win the game.The stargery to win the game is to collect difficult types of minerals(diamond,cystal,emerald). Each equipment(driller,picker) and sensor(radio active sensor , chemaical senser) is used to detect and collect an particular minaral. The arena is a tile based area with many diffrent terrains (Sand , Rock ,Graval)
                
Our team worked on a scanning rover to supported the blue corp to win the game.Basically my team rover will use different type of path finding algorithms(combination of A* star, Random , Supervsied algorithms) to discover the details in each tile (about terrain and minerals) and push those details to the server so that other rovers in the blue corp can navigate based on the information from the server.


### Links
- <a href="http://csns.calstatela.edu/site/s16/cs537-1/item/5402135" target="_blank">Rover Requirements</a>
- <a href="http://bit.ly/1SB3qat" target="_blank">Helpful Time Complexity Chart, Java Library Data Structure</a>
- <a href="http://bit.ly/1QnSPYJ" target="_blank">Rover Specs</a>
- <a href="http://bit.ly/23FXbY5" target="_blank">Git Command (Google Doc)</a>
- <a href="http://bit.ly/1qPuqH0 " target="_blank">Class/ Meeting Recap notes (Google Doc)</a>

###Git Commands:
* Sync with SwermServerAndSampleRover repo
```
git pull swarmserver master
```
* Set Remote Repo
```
git remote set-url origin <git://new.url.here>
git remote add origin <git://new.url.here>
```


* Branch Deletion
```
git branch -d local_branch_name
git push origin --delete remote_branch_name // delete a remote branch 
```


* List Existing Branch
```
git branch // list only local branches
git branch -a // list all existing branches, including remote
```

* Create New Branch
```
git checkout -b branch 
```


* Push to Remote
```
git branch --set-upstream my_branch origin/my_branch // first time only
git push // second time or after
```

###Members:
* Kae Sawada
* Febi Elgiva
* Wael Alhamwi
* Nivethitha Subas

![Group 12 members](
https://github.com/ks1k1/cs537-ks1k1/blob/master/images/members.PNG)
