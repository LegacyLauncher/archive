#!/bin/bash

bundle_name="@bundle_name@"
app_dir="$bundle_name.app"

codesign --timestamp -fvvv -s "Apple Development: apple@tlaun.ch (QVKZ8GF583)" "$app_dir"

# errSecInternalComponent? try: security unlock-keychain login.keychain
