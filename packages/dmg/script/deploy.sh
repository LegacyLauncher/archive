#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

short_brand="@short_brand@"
dmg_path="LegacyLauncher_${short_brand}.dmg"

ssh tlauncher@tl-main mkdir -p brands/files/$short_brand/dmg
scp -v -r "$dmg_path" tlauncher@tl-main:brands/files/$short_brand/dmg/dmg.dmg
ssh tlauncher@tl-main bash brands/deploy.sh dmg $short_brand
