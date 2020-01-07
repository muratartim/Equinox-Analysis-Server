# EquinoxAnalysisServer
[![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![HitCount](http://hits.dwyl.io/muratartim/EquinoxAnalysisServer.svg)](http://hits.dwyl.io/muratartim/EquinoxAnalysisServer)
[![Java Version](https://img.shields.io/badge/java-8-orange.svg)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![DockerHub](https://img.shields.io/badge/dockerhub-muratartim%2Fequinox__analysis__server-yellow.svg)](https://hub.docker.com/r/muratartim/equinox_analysis_server)

The analysis server is a TCP/IP server for the fatigue-digital-twin platform which performs structural fatigue analyses. Fatigue-digital-twin platform aims at creating a digital fatigue representation of the engineering structure. You can access the platform website at http://www.equinox-digital-twin.com This project contains the prototype analysis server application of the platform, named as EquinoxAnalysisServer. Overall system & use-case architectures can be seen in the following figure.

![architecture](https://user-images.githubusercontent.com/13915745/40925193-6eb76f0e-6819-11e8-96f4-2f7f6e2f98f9.gif)

## Architecture
EquinoxAnalysisServer utilizes a multi-threaded client-server architecture and acts as a middle tier between the structural analysis engines and client applications. It uses TCP network protocol to send & receive serialized Java objects over the network via Kryonet library.

## How to run the project
You would need to specify the following VM arguments in order to run the application in the development environment.

### Main class
```
equinox.analysisServer.EntryPoint
```

### VM arguments
```
-XX:+UseStringDeduplication -Xverify:none -server -XX:+UseParallelGC
```

## How to build & run the project in a Docker container
Equinox analysis server is also available as a [container image in Docker Hub](https://hub.docker.com/r/muratartim/equinox_analysis_server "Equinox Analysis Server"). The container can be run as follows:

1. Pull the container image from Docker Hub:
```
docker pull muratartim/equinox_analysis_server
```
2. Create & run the container:
```
docker container run -p 1236:1236 -d --name equinox_analysis_server --network equinox_network muratartim/equinox_analysis_server
```

## How to run the server-side platform using Docker Compose
1. Download sample data for SFTP server: [sample data](https://drive.google.com/uc?export=download&id=1Ldr7vujbLYqOiaes0DtSNiEDNBN8yHa6)
2. Create directory 'backups' and copy the downloaded 'sftp_data.tar' file into the directory.
3. Create a named volume by executing the following command outside of 'backups' directory:
```docker
docker container run --rm -v equinox_sftp_data:/dbdata -v $(pwd)/backups:/backup ubuntu bash -c "cd /dbdata && tar xvf /backup/sftp_data.tar --strip 1"
```
4. Download sample data for MySQL server: [sample data](https://drive.google.com/uc?export=download&id=1lvzTEUpvwJw7Om-xPynxSe2d3do5f6oz)
5. Create directory 'backups' and copy the downloaded 'mysql_data.tar' file into the directory.
6. Create a named volume by executing the following command outside of 'backups' directory:
```docker
docker container run --rm -v equinox_mysql_data:/dbdata -v $(pwd)/backups:/backup ubuntu bash -c "cd /dbdata && tar xvf /backup/mysql_data.tar --strip 1"
```
7. Create & start the whole platform from where the docker-compose.yml file is located:
```docker
docker-compose up
```
8. Stop & remove the platform from where the docker-compose.yml file is located:
```docker
docker-compose down
```

## How to deploy the server-side platform on AWS with single EC2 instance
1. Download the Deployment Template File [One-Instance-Arch-CloudFormation.yml](https://github.com/muratartim/Equinox/blob/master/docs/aws/cloud-formation/One-Instance-Arch-CloudFormation.yml).
2. Validate the template by running the following command (from where the Deployment Template File is located):
```
aws cloudformation validate-template --template-body file://./One-Instance-Arch-CloudFormation.yml
```
3. Create an AWS CloudFormation Stack using the following command (from where the Deployment Template File is located). Note that, this command will deploy the platform using the default parameters (which are valid for the 'eu-central-1' AWS Region).
```
aws cloudformation create-stack --stack-name equinox --template-body file://./One-Instance-Arch-CloudFormation.yml
```
4. Delete & rollback the stack as follows:
```
aws cloudformation delete-stack --stack-name equinox
```

This will deploy the platform on AWS utilizing the following architecture:

<img width="1018" alt="Equinox_One_Instance_AWS_Architecture" src="https://user-images.githubusercontent.com/13915745/70265112-0684f280-179a-11ea-8562-5996c8235707.png">

## Full stack server-side deployment on AWS
1. Download the Deployment Template File [Full-Stack-High-Availability-Arch-CloudFormation.yml](https://github.com/muratartim/Equinox/blob/master/docs/aws/cloud-formation/Full-Stack-High-Availability-Arch-CloudFormation.yml). 
2. Validate the template by running the following command (from where the Deployment Template File is located):
```
aws cloudformation validate-template --template-body file://./Full-Stack-High-Availability-Arch-CloudFormation.yml
```
3. Create an AWS CloudFormation Stack using the following command (from where the Deployment Template File is located). Note that, this command will deploy the platform using the default parameters (which are valid for the 'eu-central-1' AWS Region).
```
aws cloudformation create-stack --stack-name equinox-digital-twin --template-body file://./Full-Stack-High-Availability-Arch-CloudFormation.yml
```
4. Delete & rollback the stack as follows:
```
aws cloudformation delete-stack --stack-name equinox-digital-twin
```

This will deploy the platform on AWS utilizing the following architecture:

<img width="1018" alt="Equinox_Full_Stack_AWS_Architecture" src="https://user-images.githubusercontent.com/13915745/71382538-ad5df100-25d8-11ea-9cac-b568d073c469.png">