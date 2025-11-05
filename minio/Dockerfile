FROM minio/minio:RELEASE.2025-09-07T16-13-09Z

# Copy initialization script
COPY init.sh /usr/local/bin/minio-init.sh
RUN chmod +x /usr/local/bin/minio-init.sh

# Install MinIO client (mc)
RUN curl -L https://dl.min.io/client/mc/release/linux-amd64/mc -o /usr/local/bin/mc \
    && chmod +x /usr/local/bin/mc

# Expose ports
EXPOSE 9000 9001

# Start MinIO and run init script in background
ENTRYPOINT ["/bin/sh", "-c", "minio server /data --console-address ':9001' & sleep 10 && /usr/local/bin/minio-init.sh && wait"]
