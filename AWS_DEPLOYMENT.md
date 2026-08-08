# AWS Deployment Guide for FlowSync

This guide walks you through deploying the FlowSync unified architecture (Spring Boot Backend + React Frontend + MySQL) onto AWS infrastructure using an EC2 + RDS + S3 strategy.

## Architecture Overview

*   **Database**: AWS RDS (MySQL 8)
*   **Backend**: AWS EC2 (Amazon Linux 2023 or Ubuntu 24.04), running the Spring Boot `.jar` via Systemd or Docker.
*   **Frontend**: AWS S3 (Static Website Hosting) combined with Amazon CloudFront (CDN) for fast, global delivery.

---

## 1. Setup the Database (AWS RDS)

1. Go to the AWS RDS Console and click **Create database**.
2. Select **MySQL** (Version 8.0.x).
3. Choose the **Free tier** or **Dev/Test** template depending on your budget.
4. **Settings:**
    *   **DB instance identifier:** `flowsync-db`
    *   **Master username:** `admin` (or your preference)
    *   **Master password:** (choose a secure password)
5. **Connectivity:**
    *   Allow Public Access: **No** (best practice). It should only be accessible from your EC2 instance's security group.
    *   Create a new VPC security group or use default, making sure port `3306` allows inbound traffic from your EC2 instance.
6. Click **Create database**. 
7. *Note down the endpoint URL once the DB is available (e.g., `flowsync-db.xxxx.us-east-1.rds.amazonaws.com`).*

---

## 2. Deploy the Backend (AWS EC2)

1. Go to the EC2 Console and click **Launch Instance**.
2. **OS Image:** Amazon Linux 2023 or Ubuntu.
3. **Instance Type:** `t2.micro` (Free tier) or `t3.small`.
4. **Key Pair:** Create a new RSA key pair `.pem` file to SSH into the instance.
5. **Network Settings:**
    *   Allow SSH traffic from Anywhere (or your IP).
    *   Allow HTTP (port 80) and HTTPS (port 443).
    *   *Custom TCP:* Allow port `8080` (where Spring Boot will run).
6. **Launch Instance**.

### Connect and Install Java
Connect to your EC2 instance via SSH:
```bash
ssh -i "your-key.pem" ec2-user@<your-ec2-public-ip>
```

Install Java 21:
```bash
# For Amazon Linux
sudo dnf install java-21-amazon-corretto -y
```

### Build and Transfer the Application
On your local machine, build the Spring Boot `.jar`:
```bash
cd flowsync-backend
./mvnw clean package -DskipTests
```
Transfer the built jar (`target/flowsync-backend-1.0.0.jar`) to EC2 using SCP:
```bash
scp -i "your-key.pem" target/flowsync-backend-1.0.0.jar ec2-user@<your-ec2-public-ip>:/home/ec2-user/
```

### Run the Backend
On the EC2 instance, set the environment variables (matching what we put in `application.properties`) and start the app:
```bash
export DB_URL=jdbc:mysql://<your-rds-endpoint>:3306/flowsync_db?createDatabaseIfNotExist=true
export DB_USERNAME=admin
export DB_PASSWORD=your_rds_password

# Run in the background using nohup
nohup java -jar flowsync-backend-1.0.0.jar > app.log 2>&1 &
```
*Tip: For a robust production setup, consider writing a `systemd` service file so the app restarts automatically if the server reboots.*

---

## 3. Deploy the Frontend (AWS S3 + CloudFront)

### Update API Base URL
In your React code, update `src/services/api.ts`. Change the `baseURL` to point to your EC2 instance's public IP or domain name.
```typescript
const api = axios.create({
  baseURL: 'http://<your-ec2-public-ip>:8080', // In production, map a domain name here
  headers: { 'Content-Type': 'application/json' },
});
```

### Build the React App
On your local machine:
```bash
cd flowsync-frontend
npm run build
```
This generates a `dist` folder.

### Create an S3 Bucket
1. Go to AWS S3 and **Create bucket**.
2. Name: `flowsync-frontend-ui` (must be globally unique).
3. Uncheck **Block all public access** (acknowledge the warning).
4. Click **Create bucket**.

### Enable Static Website Hosting
1. Click into your bucket -> **Properties**.
2. Scroll to **Static website hosting** -> Edit -> Enable.
3. Index document: `index.html`.
4. Error document: `index.html` (critical for React Router to handle 404s).
5. Save.

### Upload Files and Set Permissions
1. Go to **Objects** -> **Upload**, and upload the *contents* of your `dist` folder.
2. Go to **Permissions** -> **Bucket Policy** and add a policy to make the bucket public:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::flowsync-frontend-ui/*"
        }
    ]
}
```

You can now access your frontend via the S3 Bucket Website Endpoint!

*(Optional but Recommended: Create an AWS CloudFront distribution pointing to this S3 bucket to provide free HTTPS and global caching).*

---

## 4. Final Security Checklist
* [ ] Do not expose port 3306 on RDS to the public internet. Only the EC2 security group should have access.
* [ ] Use AWS Certificate Manager (ACM) to attach an SSL certificate to a Load Balancer (for EC2) and CloudFront (for S3) to ensure all traffic is HTTPS.
* [ ] Change the `flowsync.jwt.secret` property in `application.properties` to a highly secure, randomized 256-bit key injected via environment variables.
