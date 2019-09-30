Instruções de como preparar o ambiente para executar o OndeBus em qualquer máquina

- Instala Java 1.8 e Tomcat 8: https://devops.ionos.com/tutorials/how-to-install-and-configure-tomcat-8-on-ubuntu-1604/

- Inicia Tomcat:
sudo systemctl restart tomcat
sudo systemctl status tomcat
login: tomcat Senha: admin

- Faz deploy da aplicação e se não der certo o deploy do war aumentar a capacidade do max-file-size e max-request-size nesse arquivo:
sudo nano webapps/manager/WEB-INF/web.xml

- Entra em /Documents/BusMonitoring/data/CIDADE/input/

- Roda BULMA:
java -jar -Xmx1024M ../../../libs/BULMA_RT_CG.jar shape_CampinaGrande.csv stopTimeOutput.txt localhost 9998 ../output/ 1 20
java -jar -Xmx1024M ../../../libs/BULMA_RT_CURITIBA.jar shape_Curitiba.csv stopTimeOutput.txt localhost 9999 ../output/ 1 20

- Roda StreamSimulation:

java -jar ../../../libs/StreamSimulationFile.jar 9998 GPS_CampinaGrande_2017-10-21.csv 3
java -jar ../../../libs/StreamSimulationFile.jar 9999 GPS_Curitiba_2017-10-21.csv 3