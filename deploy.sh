#!bash

source ./config.env

./gradlew buildFatJar

if [ $? -ne 0 ]; then
  echo "Сборка завершилась с ошибкой. Выход..."
  exit 1
fi

scp -i keys/shb-key build/libs/studhunter.jar $USER@$HOST:$DEPLOY_PATH

if [ $? -ne 0 ]; then
  echo "Не удалось скопировать JAR на сервер. Выход..."
  exit 1
fi

ssh -i keys/shb-key $USER@$HOST 'sudo systemctl restart studhunter.service'

if [ $? -ne 0 ]; then
  echo "Не удалось перезапустить сервис. Выход..."
  exit 1
fi

echo "Готово!"
