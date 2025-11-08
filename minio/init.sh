#!/bin/sh
set -e

echo "⏳ Waiting for MinIO to start..."
sleep 5

echo "🔗 Connecting to MinIO..."
mc alias set local http://localhost:9000 "${MINIO_ACCESS_KEY}" "${MINIO_SECRET_KEY}" >/dev/null 2>&1

# Check if bucket exists
if mc stat local/${MINIO_BUCKET} >/dev/null 2>&1; then
  echo "✅ Bucket '${MINIO_BUCKET}' already exists. Skipping creation."
else
  echo "📦 Creating new bucket: ${MINIO_BUCKET}"
  mc mb local/${MINIO_BUCKET}
fi

# Create a temporary policy file
POLICY_FILE="/tmp/public-policy.json"
cat > "$POLICY_FILE" <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": ["s3:GetObject"],
      "Effect": "Allow",
      "Principal": {"AWS": ["*"]},
      "Resource": ["arn:aws:s3:::${MINIO_BUCKET}/*"]
    }
  ]
}
EOF

# Apply policy
echo "🔓 Applying public-read policy to '${MINIO_BUCKET}'..."
mc anonymous set-json "$POLICY_FILE" local/${MINIO_BUCKET}
echo "✅ Public policy applied successfully."
rm -f "$POLICY_FILE"

echo "🎉 Bucket setup completed: ${MINIO_BUCKET}"
