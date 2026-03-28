#!/usr/bin/env bash

echo $SIGNING_KEY | base64 --decode > signing.key
gpg --import signing.key
shred signing.key

PUBLISH_VERSION=$TRAVIS_TAG ./mill -i --ticker false \
            mill.scalalib.SonatypeCentralPublishModule/publishAll \
            --gpgArgs --passphrase="$GPG_PW",--batch,--yes,-a,-b,--pinentry-mode,loopback \
            --username "$SONATYPE_NAME" --password "$SONATYPE_PW" \
            --shouldRelease true