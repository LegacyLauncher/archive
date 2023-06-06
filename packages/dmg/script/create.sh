#!/bin/bash

bundle_name="@bundle_name@"
short_brand="@short_brand@"
full_brand="@full_brand@"

app_dir="$bundle_name.app"
dmg_path="LegacyLauncher_${short_brand}.dmg"

# Remove quarantine flag that causes the app to be "damaged"
sudo xattr -r -d com.apple.quarantine "$app_dir"

test -f "$dmg_path" && rm "$dmg_path"
create-dmg \
  --volname "$bundle_name" \
  --volicon "TL.icns" \
  --window-size 800 450 \
  --icon-size 128 \
  --icon "$app_dir" 200 215 \
  --app-drop-link 600 215 \
  --background "background.tiff" \
  "$dmg_path" \
  "$app_dir"
