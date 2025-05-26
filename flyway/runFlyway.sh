#!/usr/bin/env bash

flyway="$(dirname "$0")"
pushd "$flyway" &>/dev/null || true

helpFlag="--help"
helpFileName="runFlyway_help.txt"
forceParam="-Pforce"

last_modified_file="flyway_last_modified.txt"
debug_file="flyway_debug.txt"
zero_date=946684800  # (2000-01-01 00:00:00 UTC)

if [[ -z "$dockerImage" ]]; then
  dockerRegistry=581316237707.dkr.ecr.eu-west-1.amazonaws.com
  dockerImage="${dockerRegistry}/hibobio/backend-platform:flyway-gradle-jdk21"

  if ! grep -q "$dockerRegistry" ~/.docker/config.json \
    || ! docker pull ${dockerImage} 2>/dev/null; then
      ./../scripts/sign_in_to_aws.sh
      docker pull ${dockerImage} 2>/dev/null
      docker run --rm -it "${dockerImage}" sh -c "cat /home/gradle/flyway/bin/${helpFileName}" > "${helpFileName}"
  fi
fi

if [[ " $* " =~ \ ${helpFlag}|tasks\  ]]; then
  cat "${helpFileName}"
  exit 0

elif [[ " $* " =~ \ ${forceParam}\  ]]; then

  rm "$last_modified_file" "$debug_file" 2>/dev/null
fi

task=flywayMigrate
if [[ "$1" =~ flyway* ]]; then
  task=""
fi

if [[ " ${task} $* " =~ \ flywayMigrate\  ]]; then

  declare -a folders=( migration code_migration/src gradlePlugins gradleScripts gradleSettings )

  convert_timestamp_to_date() {
      local timestamp=$1
      if date --version >/dev/null 2>&1; then
          # Linux
          date -d "@$timestamp" +"%Y-%m-%d %H:%M:%S"
      else
          # macOS
          date -r "$timestamp" +"%Y-%m-%d %H:%M:%S"
      fi
  }

  get_latest_timestamp() {

      pwd > $debug_file
      timestamp_date=$(convert_timestamp_to_date "$stored_timestamp")
      echo "previous_timestamp = $stored_timestamp - '$timestamp_date'" >> $debug_file
      if stat --version >/dev/null 2>&1; then
          # GNU stat (Linux)
          stat_cmd='stat -c "%Y %W" "$1"'
      else
          # BSD stat (macOS)
          stat_cmd='stat -f "%m %B" "$1"'
      fi

      echo "
stat_cmd = $stat_cmd
" >> $debug_file

      latest_timestamp=$stored_timestamp
      for folder in "${folders[@]}"; do
          if [[ -d "$folder" || -L "$folder" ]]; then

              find_cmd="find -L \"$folder\" -path \"migration/code_migration\" -prune -o -type f -newermt \"$timestamp_date\""
              echo "$find_cmd" >> $debug_file

              folder_latest=$(find -L "$folder" \
                              -path "migration/code_migration" -prune -o \
                              -path "*/.DS_Store" -prune -o \
                              -type f \
                              -newermt "$timestamp_date" \
                              -exec sh -c "$stat_cmd" shell {} \;  | tr ' ' '\n' | sort -n | tail -1)

              folder_latest=${folder_latest:-0}
              echo "$folder $folder_latest - '$(convert_timestamp_to_date "$folder_latest")'" >> $debug_file
              if [[ "$folder_latest" -gt "$latest_timestamp" ]]; then
                  latest_timestamp=$folder_latest
              fi
          fi
      done
      echo "$latest_timestamp"
  }

  if [[ -f "$last_modified_file" ]]; then
      stored_timestamp=$(<"$last_modified_file")
  else
      stored_timestamp=${zero_date}
  fi

  cat <<EOF

checking files last modified since timestamp $stored_timestamp - '$(convert_timestamp_to_date "$stored_timestamp")'
EOF
  latest_timestamp=$(get_latest_timestamp)

  echo "stored_timestamp = $stored_timestamp - '$(convert_timestamp_to_date "$stored_timestamp")'" >> $debug_file
  echo "latest_timestamp = $latest_timestamp - '$(convert_timestamp_to_date "$latest_timestamp")'" >> $debug_file

  if [[ "$latest_timestamp" != "$stored_timestamp" ]]; then
      echo "$latest_timestamp" > "$last_modified_file"
  cat <<EOF

flyway changes detected.
updated $last_modified_file with timestamp $latest_timestamp - '$(convert_timestamp_to_date "$latest_timestamp")'
Running flyway...

EOF
  else
  cat <<EOF

 > Task :${task} UP-TO-DATE

      * ${task} can be force executed by adding '${forceParam}'"
EOF
      exit 0
  fi

fi

DB_URL_ENV=""
if [[ -n "${DB_URL}" ]]; then
  DB_URL_ENV="-e DB_URL=${DB_URL}"
fi

dockerVolumes="
 -v ./gradle.properties:/home/gradle/flyway/gradle.properties
 -v $(readlink -f ./migration)/:/home/gradle/flyway/migration/"

declare -a extensionFolders=( callbacks code_migration gradlePlugins gradleScripts gradleSettings )

for extensionFolder in "${extensionFolders[@]}"; do
  if [ -d "${extensionFolder}" ]; then
    dockerVolumes="${dockerVolumes}
    -v ./${extensionFolder}/:/home/gradle/flyway/${extensionFolder}/"
  fi
done

 # shellcheck disable=SC2086
docker run --rm \
 --network=host \
 ${DB_URL_ENV} \
 -e _JAVA_OPTIONS="${_JAVA_OPTIONS}" \
 ${dockerVolumes} \
 "${dockerImage}" \
 gradle -PskipFlywayCallbacks=true ${task} "$@"
