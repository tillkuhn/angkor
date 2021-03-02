#!/usr/bin/env bash
echo "Launch sonar-scanner in $(pwd)"

if [ -z $SONAR_TOKEN ]; then
  echo "SONAR_TOKEN not set"; exit 1
fi
if [ -z $RELEASE_VERSION ]; then
  echo "RELEASE_VERSION not set"; exit 1
fi

for module in imagine polly healthbells remindabot; do
    echo "Enter module $module"
    cd $module
    if [ -f sonar-project.properties ]; then
        echo "Sonar props found int $module"
        ../sonar/node_modules/.bin/sonar-scanner \
            -Dsonar.login=$SONAR_TOKEN \
            -Dsonar.host.url=https://sonarcloud.io \
            -Dsonar.projectVersion=$RELEASE_VERSION \
            -Dsonar.organization=tillkuhn
        sonar_exit=$?
        if [ $sonar_exit -ne 0 ]; then
            echo "error sonar-scanner returned with exit code $sonar_exit"
            exit $sonar_exit
        fi
    else 
        echo "WARNING sonar props not found, skip module $module"
    fi
    cd ..
done
