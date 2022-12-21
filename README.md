
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
