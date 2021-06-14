FROM node:16-alpine AS build
WORKDIR /workspace

ENV NODE_ENV production

RUN apk add --update --no-cache yarn build-base libtool autoconf automake zlib-dev pkgconfig nasm curl python3 && \
    curl -sfL https://install.goreleaser.com/github.com/tj/node-prune.sh | bash -s -- -b /usr/local/bin

COPY package.json yarn.lock ./
RUN yarn --frozen-lockfile --network-timeout 100000 && /usr/local/bin/node-prune

COPY . .

FROM node:16-alpine
WORKDIR /usr/src/app

ENV NODE_ENV production

COPY --from=build /workspace/node_modules ./node_modules
COPY --from=build /workspace/package.json .
COPY --from=build /workspace/index.js .

EXPOSE 8082
CMD ["node", "index.js"]
