
<!-- GETTING STARTED -->
## Getting Started

This is the plugin(s) for minecraft that is created to humiliate 

### Prerequisites

#### Mvn dependency plugin

Let's start with defining the plugin directory on the minecraft server

First: clone the repo and navigate to the pom.xml file 
In pom.xml: replace the 
```
${project.build.directory}/minecraft
```
with the actual path to the plugins dir. Example: 
```
D:/Games/ServerMinecraft/Plugins
```
So the pom xml properties will look like: 
```xml
 <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <minecraft.local.dir>D:/Games/ServerMinecraft/Plugins</minecraft.local.dir>
    </properties>
```
Run
```
mvn clean install
```

Enjoy!

### Installation

1. Clone the repo
2. Run ```mvn clean install```
3. go to target dir, copy the jar file
4. paste the jar file into the plugins dir on the server 
5. restart server 

OR

1. Clone repo
2. Do according to instructions in "Mvn dependency plugin" above
3. Restart the server

### Additional server configuration

1. Open the server.properties file in the minicraft server, set the next properties
   1. online-mode=false
   2. white-list=false

### Running the plugin 

1. Proceed with installation instructions
2. Run the minecraft server using start.bat file
3. Open minecraft launcher
   1. Multiplayer
   2. Server address 0.0.0.0
   3. in server cmd window type : op "${your user name}". E.G. if your username is Uranus, type "op Uranus"
   4. try using the plugin by typing in any of the commands
   
