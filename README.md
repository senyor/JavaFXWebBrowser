# JavaFXWebBrowser
A simple web browser based on JavaFX 8 technologies.

# Dependencies/Resources
Due to license compatibility reasons you need to download the FontAwesome font yourself (http://fortawesome.github.io/Font-Awesome) and put the *.ttf file in the `src/main/resources` directory into the same package as the main app (`src/main/resources/de/lwerner/javafxwebbrowser`).

# Target group
This project is supposed to be used by students, JavaFX newbies and those who want to explore the amazing features, JavaFX 8 involves.

# How to build
To build this project, you need to `git clone https://github.com/lukaswerner/JavaFXWebBrowser.git` this GitHub repository on your local machine. Once you've done that, make sure you have a current version of https://maven.apache.org installed and made the mvn (in UNIX) or the mvn.bat (in Windows) file executable from everywhere (PATH/Link). Then simply type `mvn clean jfx:jar` to create a runnable jar or `mvn clean install` to just compile the classes and run them manually via `java de.lwerner.javafxwebbrowser.MainApp`. You can also run this application with the command `mvn jfx:run`.

### Change Log
All notable changes to this project will be documented in this file.

#### [0.1] - 2015-06-30
##### Added
- Initially created this application and made it runnable
