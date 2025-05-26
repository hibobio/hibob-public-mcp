#!/bin/bash

url="$(git remote get-url origin)"
repo_name=$(echo "$url" | sed -E 's#.*/([^/.]+)(\.git)?#\1#')

echo -n "Service Name:
(ENTER to use the repository name '$repo_name')
"
read -r service_name

if [ -z "$service_name" ]; then
    service_name="$repo_name"
fi

#service_name usually service-name-kebab-case
service_name_snake_case="${service_name/-/_}"
serviceNameCamelCase=$(echo "$service_name" | awk -F '-' '{for (i=1;i<=NF;i++) printf (i>1)? toupper(substr($i,1,1))tolower(substr($i,2)) : $i}')
ServiceName="$(echo "${serviceNameCamelCase:0:1}" | tr '[:lower:]' '[:upper:]')${serviceNameCamelCase:1}"

echo
echo "kotlin-template -> $service_name"
echo "kotlin_template -> $service_name_snake_case"
echo "kotlintemplate  -> $serviceNameCamelCase"
echo "KotlinTemplate  -> $ServiceName"

source used_ports.sh

echo
printf "Allocating service port... "
while true; do
    port=$((9400 + RANDOM % 100))
    printf ' ...%s' "$port"
    if ! [[ ${used_ports[*]} =~ (^|[[:space:]])$port($|[[:space:]]) ]]; then
        printf '\n'
        break
    fi
done
echo "port=$port"
echo

sed -i '' -e "s/\${PORT\:0}/$port/g" app/src/main/resources/application-development.yml
sed -i '' -e "s/\PORT/$port/g" gradle.properties

for file in $(grep -I -l -r "kotlin_template\|kotlin-template\|kotlintemplate\|KotlinTemplate"); do
    if ! [[ $file =~ ^(\./)?(\.git.*|\.github.*|scripts|$0) ]]; then
        sed -i '' \
          -e "s/kotlin-template/$service_name/g" \
          -e "s/kotlin_template/$service_name_snake_case/g" \
          -e "s/kotlintemplate/$serviceNameCamelCase/g" \
          -e "s/KotlinTemplate/$ServiceName/g" \
          "$file"
    fi
done

declare -a folders=(app/src/main/kotlin/com/hibob app/src/test/kotlin/com/hibob lib/src/main/kotlin/com/hibob)
for folder in "${folders[@]}"; do
    pushd "$folder" > /dev/null || return
    git mv "kotlintemplate/" "$serviceNameCamelCase"
    popd > /dev/null || return
done

git mv ".run/KotlinTemplateApplication.run.xml" ".run/${ServiceName}App.run.xml"
git mv app/src/main/kotlin/com/hibob/KotlinTemplateApplication.kt "app/src/main/kotlin/com/hibob/${ServiceName}Application.kt"

rm .github/workflows/kotlinTemplatePR.yaml
rm .github/workflows/nameThisService.yaml
rm used_ports.sh

echo "Ready to commit all changes"
echo "see 'git diff'"
echo

echo "Self destructing!"
rm $0
