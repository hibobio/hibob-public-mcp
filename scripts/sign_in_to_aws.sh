#!/usr/bin/env bash

set -e

if [ -z "${DEFAULT_AWS_PROFILE}" ]; then
    DEFAULT_AWS_PROFILE=dev-generic
fi

echo "testing AWS credentials"
echo "aws sts get-caller-identity"
printf " ::: "
if ! aws sts get-caller-identity --no-cli-pager | grep "${DEFAULT_AWS_PROFILE}"; then
    aws sso login --profile "${DEFAULT_AWS_PROFILE}" && export AWS_PROFILE=${DEFAULT_AWS_PROFILE}

    if [ -z "${AWS_SHARED_CREDENTIALS_FILE}" ]; then
        export AWS_SHARED_CREDENTIALS_FILE=~/.aws/credentials
    fi

    if [ -f "${AWS_SHARED_CREDENTIALS_FILE}" ]; then
        rm "${AWS_SHARED_CREDENTIALS_FILE}_"* 2>/dev/null || true
        cp "${AWS_SHARED_CREDENTIALS_FILE}" "${AWS_SHARED_CREDENTIALS_FILE}_$(date +'%Y%m%d_%H%M%S')"
    fi
    aws configure export-credentials --profile "${DEFAULT_AWS_PROFILE}" --format env-no-export | awk -F= '{
        if ($1 == "AWS_ACCESS_KEY_ID") {
            access_key = $2
        } else if ($1 == "AWS_SECRET_ACCESS_KEY") {
            secret_key = $2
        } else if ($1 == "AWS_SESSION_TOKEN") {
            session_token = $2
        } else if ($1 == "AWS_CREDENTIAL_EXPIRATION") {
            expiration = $2
        }
    }
    END {
        printf("[default]\n")
        printf("aws_access_key_id        = %s\n", access_key)
        printf("aws_secret_access_key    = %s\n", secret_key)
        printf("aws_session_token        = %s\n", session_token)
        printf("aws_security_token       = %s\n", session_token) # Assuming AWS_SECURITY_TOKEN is the same as AWS_SESSION_TOKEN
        printf("x_security_token_expires = %s\n", expiration)
    }' > "${AWS_SHARED_CREDENTIALS_FILE}"
fi

echo "
docker login --username AWS"
aws --profile "${DEFAULT_AWS_PROFILE}" ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 581316237707.dkr.ecr.eu-west-1.amazonaws.com
