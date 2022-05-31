#!/bin/bash

# Remove quarantine flag that causes the app to be "damaged"
sudo xattr -r -d com.apple.quarantine TL.app

test -f TL.dmg && rm TL.dmg
create-dmg \
  --volname "TL" \
  --volicon "TL.icns" \
  --window-size 800 450 \
  --icon-size 128 \
  --icon TL.app 200 215 \
  --app-drop-link 600 215 \
  --background "background.tiff" \
  "TL.dmg" \
  "TL.app/"
