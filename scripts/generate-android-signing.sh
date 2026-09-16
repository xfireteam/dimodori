#!/usr/bin/env bash
set -euo pipefail

output_directory=".signing"
keystore_path="$output_directory/dimodori-upload.keystore"
secrets_path="$output_directory/codemagic-secrets.txt"
fingerprint_path="$output_directory/certificate-sha256.txt"

for command_name in keytool openssl base64 git; do
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "Required command not found: $command_name" >&2
    exit 1
  fi
done

if ! git check-ignore -q "$secrets_path"; then
  echo "$output_directory must be ignored by Git before creating signing material." >&2
  exit 1
fi

if [[ -e "$keystore_path" || -e "$secrets_path" ]]; then
  echo "Signing files already exist in $output_directory." >&2
  echo "Move or back them up before generating a different signing identity." >&2
  exit 1
fi

umask 077
mkdir -p "$output_directory"

read -r -p "Key alias [dimodori-upload]: " key_alias
key_alias="${key_alias:-dimodori-upload}"
if [[ ! "$key_alias" =~ ^[A-Za-z0-9._-]+$ ]]; then
  echo "The alias may contain only letters, numbers, dots, underscores, and hyphens." >&2
  exit 1
fi

read -r -s -p "Keystore password (Enter to generate one): " store_password
printf '\n'
if [[ -z "$store_password" ]]; then
  store_password="$(openssl rand -base64 36 | tr -d '\r\n')"
else
  read -r -s -p "Confirm keystore password: " store_password_confirmation
  printf '\n'
  if [[ "$store_password" != "$store_password_confirmation" ]]; then
    echo "Keystore passwords do not match." >&2
    exit 1
  fi
fi

read -r -s -p "Key password (Enter to generate one): " key_password
printf '\n'
if [[ -z "$key_password" ]]; then
  key_password="$(openssl rand -base64 36 | tr -d '\r\n')"
else
  read -r -s -p "Confirm key password: " key_password_confirmation
  printf '\n'
  if [[ "$key_password" != "$key_password_confirmation" ]]; then
    echo "Key passwords do not match." >&2
    exit 1
  fi
fi

if (( ${#store_password} < 8 || ${#key_password} < 8 )); then
  echo "Signing passwords must contain at least 8 characters." >&2
  exit 1
fi

export DIMODORI_GENERATED_STORE_PASSWORD="$store_password"
export DIMODORI_GENERATED_KEY_PASSWORD="$key_password"

keytool -genkeypair \
  -keystore "$keystore_path" \
  -storetype JKS \
  -storepass:env DIMODORI_GENERATED_STORE_PASSWORD \
  -keypass:env DIMODORI_GENERATED_KEY_PASSWORD \
  -alias "$key_alias" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -dname "CN=DIMODORI Android Upload"

if base64 --help 2>&1 | grep -q -- '-w'; then
  keystore_base64="$(base64 -w 0 "$keystore_path")"
else
  keystore_base64="$(base64 < "$keystore_path" | tr -d '\r\n')"
fi

certificate_sha256="$(
  keytool -list -v \
    -keystore "$keystore_path" \
    -storepass:env DIMODORI_GENERATED_STORE_PASSWORD \
    -alias "$key_alias" \
    | awk '/SHA256:/{print $2; exit}'
)"

cat > "$secrets_path" <<EOF
DIMODORI_KEYSTORE_BASE64=$keystore_base64
DIMODORI_STORE_PASSWORD=$store_password
DIMODORI_KEY_ALIAS=$key_alias
DIMODORI_KEY_PASSWORD=$key_password
EOF

printf '%s\n' "$certificate_sha256" > "$fingerprint_path"
chmod 600 "$keystore_path" "$secrets_path" "$fingerprint_path"

unset DIMODORI_GENERATED_STORE_PASSWORD DIMODORI_GENERATED_KEY_PASSWORD
unset store_password store_password_confirmation key_password key_password_confirmation
unset keystore_base64

cat <<EOF
Android signing identity created.

Keep these files private and back them up securely:
  Keystore: $keystore_path
  Codemagic values: $secrets_path

Public SHA-256 certificate fingerprint:
  $fingerprint_path

Copy the four lines from codemagic-secrets.txt into the
dimodori_android_release group in Codemagic. Mark every variable Secret.
Never commit or share the .signing directory.
EOF