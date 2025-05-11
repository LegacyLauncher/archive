#!/bin/sh

docker run \
    --rm \
    -v "$(pwd):/workspace" \
    -w /workspace \
    amake/innosetup:latest \
    main.iss
