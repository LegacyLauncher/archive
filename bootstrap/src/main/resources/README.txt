TL;DR: If you're reading this file please open TL with Java, not with archiver!

Hello! Thanks for installing TL!
If an archive with this file was opened instead of our launcher:
1. Click TL.jar with right mouse button
2. Select "properties"
3. Select "change..." in the "Application" row
4. Select Java in opened window
4.1. No Java? Install Java from java.com/download
4.2. You've installed Java, but there is no Java in applications list?
     Use JarFix program from https://johann.loefflmann.net/en/software/jarfix/index.html
5. Click "Ok" and close "properties" window. All done!

Using Linux? Search for Java 8 package (usually named like jre8-openjdk or openjdk-8-jre)
in your distro repos and install it using package manager (apt, apt-get, pacman, pamac, yum, dnf, portage...)

Using macOS? Install Java from java.com/download

Have troubles? Or questions? Need help? Here is some useful links:
  Our Facebook page: tlaun.ch/fb
  Our VK page: tlaun.ch/vk
  Our Discord server: tlaun.ch/discord/intl
  Our support e-mail: support@tln4.ru

TL download links:
  Stable:
    Exe (for Windows): tlaun.ch/dl/mcl/exe
    Jar (for Windows, Linux, macOS): tlaun.ch/dl/mcl/jar
  Beta (latest):
    Exe (for Windows): tlaun.ch/latest/exe
    Jar (for Windows, Linux, macOS): tlaun.ch/latest/jar




























===========================================
     TL features for experienced users
===========================================
* This arguments can be used in tl.bootargs file in same folder launcher is
* or as Java arguments (java THIS-ARGUMENTS -jar TL.jar)
* It's recommended to use OS-specific tl.bootargs file: tl-[OS].bootargs
* OS- and Arch-spefic tl.bootargs is also supported: tl-[OS]-[ARCH].bootargs
* [OS] is replaced with user's OS name (windows, linux, macos)
* [ARCH] is replaced with user's OS arch (x64 for 64-bit and x86 for 32-bit)

Points to launcher Jar file
Used for Portable client mode
-Dtlauncher.bootstrap.targetJar=<path/to/file.jar>

Points to launcher libraries folder
Used for Portable client mode
Can be used for moving launcher files to other hard drive
-Dtlauncher.bootstrap.targetLibFolder=<path/to/folder>

===========================================
* This arguments can be used in tl.args file in same folder launcher is
* or as TL arguments (java -jar TL.jar THIS-ARGUMENTS)

Overrides game folder path. Locks this setting in the launcher
Used for Portable client mode
--directory <path/to/folder>

Overrides Java arguments. Locks this setting in the launcher
--javaargs <arguments>

Overrides Minecraft arguments. Locks this setting in the launcher
--margs <arguments>

Overrides TL settings file
Used for Portable client mode
--settings <path/to/file>

Set default nickname selected on launcher startup
--usernane <nickname>

Set default version selected on launcher startup
--version <version>

Overrides launcher background. Locks this setting in the launcher
--background <path/to/file>

More: https://wiki.tlaun.ch/guide:portable-client (Sorry, russian only)
