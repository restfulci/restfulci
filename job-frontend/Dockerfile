FROM node:14.6.0-alpine3.12

RUN mkdir -p /usr/src/nuxt-app
WORKDIR /usr/src/nuxt-app

COPY . /usr/src/nuxt-app/
RUN yarn install

# TODO:
# Can't be passing in through docker-compose env, since it needs value
# at `yarn build` step. This makes the image to be different for dev/stage/prod.
#
# Also, this needs to be the API path to be seen from the browser, rather
# than the API path seen from the docker container (more specifically,
# in docker-compose http://master-api-server:8080/ will not work).
# It is because this path is in `_nuxt/app.***.js` executed in inside of
# the browser.
#
# Don't know if there's a good workaround for this?
ENV API_URL=http://localhost:8881/

RUN yarn build

ENV NUXT_HOST=0.0.0.0
ENV NUXT_PORT=5000

EXPOSE 5000

CMD ["yarn", "start"]
