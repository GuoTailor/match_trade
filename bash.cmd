call mvn kotlin:compile package -Dmaven.test.skip=true

call copy mt-user\target\mt-user-0.0.1-SNAPSHOT.jar docker
call copy mt-gateway\target\mt-gateway-0.0.1-SNAPSHOT.jar docker
call copy mt-socket\target\mt-socket-0.0.1-SNAPSHOT.jar docker
call copy mt-engine\target\mt-engine-0.0.1-SNAPSHOT.jar docker
