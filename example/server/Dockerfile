FROM node:16-alpine
WORKDIR /usr/src/app

ENV NODE_ENV production

COPY package.json yarn.lock ./
RUN yarn --frozen-lockfile --network-timeout 100000

COPY . .

EXPOSE 8082
CMD ["node", "index.js"]
