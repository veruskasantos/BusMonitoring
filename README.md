---RODANDO LOCALMENTE--

Executar o ./ligar_hadoop_spark.sh no servidor para ligar o hadoop e permitir o acesso ao HDFS

1. Roda OndeBUS App


Para os dados de **Campina Grande**:

2. Abre um terminal para rodar BulmaStreaming em CampinaGrande/input:

-> cd Desktop/OndeBUS/BULMA_RT/CampinaGrande/input/

-> java -jar -Xmx1024M BULMA_RT_CG.jar shape_CampinaGrande.csv stopTimeOutput.txt localhost 9998 /BULMA_RT/CampinaGrande/output/ 1 20

3. Abre um terminal para rodar StreamSimulationFile (simulador de dados de GPS em tempo real, a partir de dados históricos) em CampinaGrande/input

-> cd Desktop/OndeBUS/BULMA_RT/CampinaGrande/input/

-> java -jar StreamSimulationFile.jar 9998 CampinaGrande_2017-10-21.csv 3



Para os dados de **Curitiba**:

2. Abre um terminal para rodar BulmaStreaming em Curitiba/input

cd Desktop/fetech/BULMA_RT/Curitiba/input/

java -jar -Xmx1024M BULMA_RT_CURITIBA.jar shape_Curitiba.csv stopTimeOutput.txt localhost  9997 /BULMA_RT/Curitiba/output/ 1 20

3. Abre um terminal para rodar StreamSimulationFile em Curitiba/input

cd Desktop/OndeBUS/BULMA_RT/Curitiba/input/

java -jar StreamSimulationFile.jar 9997 Curitiba_2017-10-21.csv 3


--------------------

OndeBUS App: é uma aplicação para monitoramento de frotas de ônibus, a partir de dados de GPS
- entrada: dados de GPS e GTFS dos ônibus da cidade(s) monitorada(s)
- saída: exibição da localização e situação dos ônibus no browser

BulmaStreaming: é um posprocessamento dos dados de GPS, lidos de uma porta
- entrada: dados de GPS em tempo real a partir de uma porta
- saída: dados de GPS rotulados por rota, direção e viagem

 StreamSimulationFile: é um simulador de dados de GPS em tempo real, a partir de dados históricos
- entrada: dados de GPS históricos a partir de um arquivo
- saída: dados de GPS da hora corrente para uma porta
