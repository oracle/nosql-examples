#FROM fnproject/node:dev as build-stage
#WORKDIR /function
#ADD package-lock.json /function/
#ADD package.json /function/
#RUN npm install
#RUN node -v

FROM node:12-slim
WORKDIR /function
ADD . /function/
#COPY --from=build-stage /function/node_modules/ /function/node_modules/
RUN npm install
RUN ls -lrt /function/*
RUN node -v
ENTRYPOINT ["node", "func.js"]
