#!/bin/sh
rm -rf dist/
npm ci
npm run build
rm ../backend/src/main/resources/static/ -rf
mkdir ../backend/src/main/resources/static/
touch ../backend/src/main/resources/static/.gitkeep
cp dist/* ../backend/src/main/resources/static/ -r
