#!/bin/bash

# Usage - ./scripts/create_migration %MIGRATION_DESCRIPTION%
#
# * if MIGRATION_DESCRIPTION is not provided, it is prompted
#
# description white spaces are replaced with underscores following flyway naming conventions
#
# in case flyway migration folder contains folders, new file will be added under month/year folder
# * such folders will be created if missing
#
if [ $# -eq 0 ]; then
    echo -n "Migration description: "
    read -r migrationDescription
else
    migrationDescription="$*"
fi

flywayMigrationDescription="${migrationDescription// /_}"
pushd "$(dirname "$0")/../flyway/migration" >/dev/null || true

migration_file="V$(date +"%Y%m%d%H%M")__${flywayMigrationDescription}.sql"

if [[ $(find "." -mindepth 1 -maxdepth 1 -type d | wc -l) -gt 0 ]]; then
    migration_directory="$(date +"%Y%m")"
    mkdir -p "$migration_directory"
    migration_file="$migration_directory/$migration_file"
fi

touch "$migration_file"
git add "$migration_file"
echo "Created file://$(realpath "$migration_file")"

popd >/dev/null || true
