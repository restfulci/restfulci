## Build Setup

```bash
# install dependencies
$ yarn install

# serve with hot reload at localhost:3000
$ API_SERVER_URI=http://localhost:8881 AUTH_SERVER_URI=http://localhost:8880 FRONTEND_URI=http://localhost:3000 yarn dev

# build for production and launch server
$ yarn build
$ yarn start

# generate static project
$ yarn generate
```

Auto linter fix

```
node_modules/.bin/eslint --ext .js,.vue . --fix
```
