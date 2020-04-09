@REM call mvnw clean kotlin:compile package -Dmaven.test.skip=true
call apidoc -i ./
call scp -r doc root@47.107.178.147:/usr/local/exam