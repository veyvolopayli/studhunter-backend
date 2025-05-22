#!bash

source ./config.env

./gradlew buildFatJar

if [ $? -ne 0 ]; then
  echo "Сборка завершилась с ошибкой. Выход..."
  exit 1
fi

scp -i keys/deploy-key build/libs/studhunter.jar $SH_SERVER_USER@$SH_SERVER_ADDRESS:$DEPLOY_PATH

if [ $? -ne 0 ]; then
  echo "Не удалось скопировать JAR на сервер. Выход..."
  exit 1
fi

ssh -i keys/deploy-key $SH_SERVER_USER@$SH_SERVER_ADDRESS 'sudo systemctl daemon-reload && sudo systemctl restart studhunter.service'

if [ $? -ne 0 ]; then
  echo "Не удалось перезапустить сервис. Выход..."
  exit 1
fi

echo "Готово!"
