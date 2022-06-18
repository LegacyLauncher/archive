#!/bin/bash
# source.png is expected to be 1080x1920

source_width=1080
source_height=1920

sizes=()
sizes[100]=164x314
sizes[125]=192x386
sizes[150]=246x459
sizes[175]=273x556
sizes[200]=328x604
sizes[225]=355x700
sizes[250]=410x797

for size in "${!sizes[@]}"
do
  IFS='x'
  read -a dimensions <<< "${sizes[$size]}"
  let desired_width="${dimensions[1]} * $source_width / $source_height"
  let offset="$desired_width - ${dimensions[0]}"
  echo "Creating $size.bmp of ${sizes[$size]}"
  convert -shave "${offset}x0" -resize "${sizes[$size]}!" -format BMP source.png "$size.bmp"
done

