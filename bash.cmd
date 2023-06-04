call mvnd clean kotlin:compile package -Dmaven.test.skip=true -pl %1 -am

@rem call copy mt-user\target\mt-user-0.0.1-SNAPSHOT.jar docker
@rem call copy mt-gateway\target\mt-gateway-0.0.1-SNAPSHOT.jar docker
@rem call copy mt-socket\target\mt-socket-0.0.1-SNAPSHOT.jar docker
@rem call copy mt-engine\target\mt-engine-0.0.1-SNAPSHOT.jar docker
@echo mt-engine
call scp mt-engine\target\mt-engine-0.0.1-SNAPSHOT.jar root@182.92.6.183:/www
@echo mt-gateway
call scp mt-gateway\target\mt-gateway-0.0.1-SNAPSHOT.jar root@182.92.6.183:/www
@echo mt-socket
call scp mt-socket\target\mt-socket-0.0.1-SNAPSHOT.jar root@182.92.6.183:/www
@echo mt-user
call scp mt-user\target\mt-user-0.0.1-SNAPSHOT.jar root@182.92.6.183:/www
