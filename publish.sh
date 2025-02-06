#!/usr/bin/env sh

dest=$(tr [:lower:] [:upper:] <<< "${1:0:1}")${1:1}

function publish() {
  echo "./gradlew :$1:publish$dest"
  ./gradlew ":$1:publish$dest"
}

publish tv-lite
publish tv-super
publish tv-binding
publish tv-marquee
publish tv-counting
publish tv-expandable
publish tv-readmore
