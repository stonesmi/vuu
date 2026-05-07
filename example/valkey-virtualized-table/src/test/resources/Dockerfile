FROM valkey/valkey:8.0

# Install procps for daemonize support
RUN apt-get update && apt-get install -y bash procps && rm -rf /var/lib/apt/lists/*

# Pre-create data directories and set permissions
RUN mkdir -p /data/node7000 /data/node7001 /data/node7002 && \
    chown -R valkey:valkey /data

WORKDIR /valkey

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 7000 7001 7002 17000 17001 17002

# Run as the valkey user for security and file ownership
USER valkey

ENTRYPOINT ["/entrypoint.sh"]