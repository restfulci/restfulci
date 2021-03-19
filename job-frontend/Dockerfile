FROM node:15.11-alpine3.12

RUN mkdir -p /usr/src/nuxt-app
WORKDIR /usr/src/nuxt-app

COPY . /usr/src/nuxt-app/
RUN yarn install

RUN yarn build

ENV NUXT_HOST=0.0.0.0
ENV NUXT_PORT=5000

EXPOSE 5000

CMD ["yarn", "start"]
