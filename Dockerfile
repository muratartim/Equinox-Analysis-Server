# latest oracle openjdk is the basis
FROM openjdk:oracle

# set maintainer
MAINTAINER Murat Artim "muratartim@gmail.com"

# copy jar
COPY target/EquinoxAnalysisServer-*.jar analysisServer/analysisServer.jar

# copy libraries
COPY target/libs analysisServer/libs

# copy resources
COPY resources/config.properties analysisServer/resources/config.properties
COPY resources/isamiConfigFile.txt analysisServer/resources/isamiConfigFile.txt
COPY resources/isamiRunScriptForFatigue.py analysisServer/resources/isamiRunScriptForFatigue.py
COPY resources/isamiRunScriptForPropagation.py analysisServer/resources/isamiRunScriptForPropagation.py

# expose server port
EXPOSE 1236

# change to server directory
WORKDIR /analysisServer

# start server
CMD ["java", "-jar", "analysisServer.jar", "-XX:+UseStringDeduplication", "-Xverify:none", "-server", "-XX:+UseParallelGC"]

# To build the docker image: (from where this file is located)
# docker image build -t muratartim/equinox_analysis_server:latest .

# To create & run the container:
# docker container run -e sftp.hostname=equinox_sftp_server 
#                      -e sftp.port=22 
#                      -e sftp.username=aurora 
#                      -e sftp.password=17891917 
#                      -e sftp.rootPath=filerRoot/ 
#                      -p 1236:1236 -d --name equinox_analysis_server --network equinox_network muratartim/equinox_analysis_server

# To start an existing container
# docker container start equinox_analysis_server