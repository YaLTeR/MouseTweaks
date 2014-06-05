#How to build
1. Setup your workspace by following [this video](http://youtu.be/tgjAiaUgTu8).
2. Create a **Mouse Tweaks** folder inside the *eclipse* folder in the *mcp* folder.
3. Clone this repository into that folder.
4. Import the module into Idea, add it into the dependencies of the Run module. You're ready to go.
5. To build the jars, execute the provided ANT build file.

#The Forge loader
The Forge loader is built with the **forgeSrc.jar** file that is located in the *forge* directory. To obtain this file you need to execute the *setupDecompWorkspace* gradle task from the desired Forge Src. The jar will be located in *<Your user folder>/.gradle/caches/minecraft/net/minecraftforge/forge/<Forge version>/* and named *forgeSrc-<forge version>.jar*. To use it in the build copy it into the *forge* folder inside the *Mouse Tweaks* folder and rename into *forgeSrc.jar*.
