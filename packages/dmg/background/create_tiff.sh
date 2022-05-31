#!/bin/bash

# See
# https://stackoverflow.com/questions/11199926/create-dmg-with-retina-background-support#comment85743863_1120476

tiffutil -cathidpicheck background.png background@2x.png -out background.tiff

