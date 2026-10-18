#!/bin/bash

# =============================================================================
# Production VM Setup Script (Ubuntu / Debian / Oracle Cloud)
# Run this script ONCE on the VM with sudo to prepare the host environment:
# sudo ./setup-vm.sh
# =============================================================================

set -e

APP_NAME="app-template"
APP_DIR="/opt/${APP_NAME}"

echo "============================================================"
echo " Starting Host VM Setup for: ${APP_NAME}"
echo "============================================================"

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
  echo "Error: Please run as root (sudo ./setup-vm.sh)"
  exit 1
fi

# 1. System Update
echo ""
echo " Updating system packages..."
apt update && apt upgrade -y

# 2. Install Docker
echo ""
echo " Installing Docker Engine..."
if ! command -v docker &> /dev/null; then
  curl -fsSL https://get.docker.com -o get-docker.sh
  sh get-docker.sh
  rm get-docker.sh
  echo " Docker installed"
else
  echo " Docker already installed"
fi

# Add current user to docker group
TARGET_USER=${SUDO_USER:-ubuntu}
usermod -aG docker "$TARGET_USER"

# 3. Install Docker Compose
echo ""
echo " Installing Docker Compose plugin..."
if ! docker compose version &> /dev/null; then
  apt install -y docker-compose-plugin
  echo " Docker Compose installed"
else
  echo " Docker Compose already installed"
fi

# 4. Install Git & Utilities
echo ""
echo " Installing Git, Curl, UFW..."
apt install -y git curl ufw

# 5. Configure Swap Space (4GB for smooth microservice builds)
echo ""
echo " Configuring 4GB swap space..."
if [ ! -f /swapfile ]; then
  fallocate -l 4G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' | tee -a /etc/fstab
  echo " Swap space configured"
else
  echo " Swap space already present"
fi

# 6. Create Application Directory
echo ""
echo " Creating application directory at ${APP_DIR}..."
mkdir -p "${APP_DIR}"
chown -R "${TARGET_USER}:${TARGET_USER}" "${APP_DIR}"

# 7. Configure Firewall
echo ""
echo " Configuring UFW Firewall..."
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 8080/tcp
ufw --force enable
echo " Firewall configured"

echo ""
echo "============================================================"
echo " VM Setup Complete!"
echo "============================================================"
echo "Next steps:"
echo "1. Clone your project into ${APP_DIR}"
echo "2. Copy .env.example to .env.production and populate secrets"
echo "3. Run: docker compose -f Other/Deployment/docker-compose.prod.yml up -d --build"
echo "Host IP: $(curl -s ifconfig.me)"
