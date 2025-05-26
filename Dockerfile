ARG REGISTRY

FROM ${REGISTRY}/jdk:jdk21_spring-boot
ARG BUILD=./app/build/stageDocker
ARG APP=/opt/app
COPY ${BUILD}/org ${APP}/org
COPY ${BUILD}/META-INF ${APP}/META-INF
COPY ${BUILD}/BOOT-INF/layers.idx ${APP}/BOOT-INF/layers.idx
COPY ${BUILD}/BOOT-INF/classpath.idx ${APP}/BOOT-INF/classpath.idx
COPY ${BUILD}/BOOT-INF/lib ${APP}/BOOT-INF/lib
COPY ${BUILD}/BOOT-INF/classes ${APP}/BOOT-INF/classes
RUN ln -sf BOOT-INF/classes/db/migration migration
