#!/usr/bin/env bash
set -Eeuo pipefail

app_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
backup_dir="${app_dir}/backups"
env_file="${app_dir}/config/.env"
timestamp="$(date +%F_%H-%M-%S)"
temporary_file="${backup_dir}/.promeditor_${timestamp}.sql.gz.tmp"
backup_file="${backup_dir}/promeditor_${timestamp}.sql.gz"

set -a
# shellcheck disable=SC1090
source "${env_file}"
set +a

mkdir -p "${backup_dir}"

docker compose \
  --project-directory "${app_dir}" \
  --env-file "${env_file}" \
  -f "${app_dir}/compose.yaml" \
  exec -T postgres pg_dump -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
  | gzip > "${temporary_file}"

mv "${temporary_file}" "${backup_file}"

# Retain the most recent 14 days of backups.
find "${backup_dir}" -type f -name 'promeditor_*.sql.gz' -mtime +14 -delete

echo "Backup created: ${backup_file}"
