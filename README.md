
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
2. update/define the <mark>minecraft.local.dir</mark> property : 
  - go to src/main/resources 
    - create the **dev.properties file** (or just copy **example.of.dev.properties** with renaming)
    - inside the file define minecraft.local.dir property that contains the path to minecraft server dir
    - E.G. `minecraft.local.dir=C:/ProgramFiles/minecraft-server`
      -  Ensure that you are using slashes like in example, and not a backslash. Even on Windows
3. Run `mvn clean install`
4. Navigate to _${minecraft.server.location}/plugins_ on your local machine, and verify that the plugin is there
5. Run/Rerun the minecraft server

---

### FAQ

### Troubleshooting

1. `You must set a minecraft.local.dir property. Read the README.md file for instructions. The parameter should point ot the minecraft server dir` Exception
   1. Read this doc, especially the "installation" part
   2. goto src/main/resources, verify that dev.properties is created, verify that `minecraft.local.dir` is created like in example above
   3. Run `mvn clean install` again
   4. If exception persist:
      1. Define minecraft.local.dir in the properties directly in pom.xml like so: ``<minecraft.local.dir>D:\soft\minecraft-server\plugins</minecraft.local.dir>``

### Running the plugin 

1. Proceed with installation instructions
2. Run the minecraft server using start.bat file
3. Open minecraft launcher
   1. Multiplayer
   2. Server address 0.0.0.0
   3. in server cmd window type : op "${your user name}". E.G. if your username is Uranus, type "op Uranus"
   4. try using the plugin by typing in any of the commands
   
