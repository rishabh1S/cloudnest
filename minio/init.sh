#!/bin/sh
set -e

echo "⏳ Waiting for MinIO to start..."
sleep 5

echo "✅ Creating bucket: ${MINIO_BUCKET}"

# Add alias for local MinIO instance
mc alias set local http://localhost:9000 "${MINIO_ACCESS_KEY}" "${MINIO_SECRET_KEY}"

# Create bucket if not exists
mc mb --ignore-existing local/${MINIO_BUCKET}

# Create public-read policy JSON
cat > /tmp/public-policy.json <<EOF
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

# Apply public policy
mc anonymous set-json /tmp/public-policy.json local/${MINIO_BUCKET}

echo "🎉 Bucket ${MINIO_BUCKET} is now public for GET access."
