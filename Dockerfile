FROM gradle:8.10.0-jdk21 as builder
ARG GIT_TAG_VERSION
ENV GIT_TAG_VERSION=${GIT_TAG_VERSION}
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

FROM alpine:3.19 as chrome-base
RUN apk upgrade --no-cache --available \
    && apk add --no-cache \
      chromium-swiftshader \
      ttf-freefont \
      font-noto-emoji \
      fontconfig \
    && fc-cache -f \
    && mkdir -p /usr/src/app \
    && adduser -D chrome
RUN chown -R chrome:chrome /usr/src/app

# additional fonts
# RUN apk add --no-cache \
#      font-terminus \
#      font-inconsolata \
#      font-dejavu \
#      font-awesome \
#      font-noto \
#      font-noto-cjk \
#      font-noto-extra \
#    && fc-cache -f

WORKDIR /usr/src/app

ENV CHROME_BIN=/usr/bin/chromium-browser \
    CHROME_PATH=/usr/lib/chromium/ \
    CHROMIUM_FLAGS="--disable-software-rasterizer --disable-dev-shm-usage"

FROM chrome-base as chrome
USER chrome
EXPOSE 9222
ENTRYPOINT ["chromium-browser", "--headless", "--no-sandbox", "--remote-debugging-address=0.0.0.0", "--remote-debugging-port=9222", "--enable-features=ConversionMeasurement,AttributionReportingCrossAppWeb", "about:blank"]

FROM chrome-base
RUN apk add --no-cache openjdk21-jre
USER chrome
EXPOSE 8080
COPY --chmod=0755 ./main.sh .
COPY --from=builder /app/build/libs/web2pdf.jar ./web2pdf.jar
ENTRYPOINT ["./main.sh"]
