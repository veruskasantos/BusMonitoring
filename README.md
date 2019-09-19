---RODANDO LOCALMENTE--

1. Roda OndeBUS App

Para CADA CIDADE:

2. Abre um terminal para rodar BulmaStreaming da cidade desejada:

-> cd <path>/BusMonitoring/data/<cidade>/input/

(Se houver algum arquivo em <cidade>/input/, deleta tudo)

-> java -jar -Xmx1024M ../../../libs/BULMA_RT_<cidade>.jar shape_<cidade>.csv stopTimeOutput<cidade>.txt localhost 9998 ../output/ <num_particoes(ex:1)> <intervalo(ex:20)>

3. Abre outro terminal para rodar StreamSimulationFile (simulador de dados de GPS em tempo real, a partir de dados históricos):

-> cd <path>/BusMonitoring/data/<cidade>/input/

-> java -jar ../../../libs/StreamSimulationFile.jar 9998 GPS_<cidade>_2017-10-21.csv <janela_de_dados(ex:3)>


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
