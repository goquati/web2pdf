#!/bin/sh

set -e

chromium-browser --headless --no-sandbox \
  --remote-debugging-address=0.0.0.0 \
  --remote-debugging-port=9222 \
  --enable-features=ConversionMeasurement,AttributionReportingCrossAppWeb \
  about:blank > /dev/null 2>&1 &

java -jar ./web2pdf.jar
