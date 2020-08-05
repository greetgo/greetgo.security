#!/bin/sh

cd "$(dirname "$0")"

echo "%%%"
echo "%%% Останавливаем докеровские образы : docker-compose down"
echo "%%%"

docker-compose down

echo "%%%"
echo "%%% Удаляем вольюмы : sudo rm -rf ./volumes/"
echo "%%%"

sudo rm -rf ./volumes/

echo "%%%"
echo "%%% Запускаем докеровские образы : docker-compose up -d"
echo "%%%"

docker-compose up -d
