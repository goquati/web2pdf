FROM gradle:8.10.0-jdk21 as builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

FROM alpine:3.19
RUN apk upgrade --no-cache --available \
    && apk add --no-cache \
      chromium-swiftshader \
      openjdk21-jre \
      ttf-freefont \
      font-noto-emoji \
      fontconfig \
    && fc-cache -f

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

RUN mkdir -p /usr/src/app \
    && adduser -D chrome \
    && chown -R chrome:chrome /usr/src/app

USER chrome
WORKDIR /usr/src/app

ENV CHROME_BIN=/usr/bin/chromium-browser \
    CHROME_PATH=/usr/lib/chromium/ \
    CHROMIUM_FLAGS="--disable-software-rasterizer --disable-dev-shm-usage"

EXPOSE 8080
COPY --chmod=0755 ./main.sh .
COPY --from=builder /app/build/libs/web2pdf.jar ./web2pdf.jar

ENTRYPOINT ["./main.sh"]
