package br.edu.icev.aed.forense;
import java.io.*;
import java.util.*;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.Alerta;
public class AnaliseForense implements AnaliseForenseAvancada {

    public AnaliseForense() {
    }

    @Override
        public Set<String> encontrarSessoesInvalidas(String arquivo) throws IOException {
            // Implementar usando Map<String, Stack<String>>
        }

        @Override
        public List<String> reconstruirLinhaTempo(String arquivo, String sessionId) throws IOException {
            Queue<String> fila= new LinkedList<>();
                    List<String> resultado=new ArrayList<>();
                    try(BufferedReader br=new BufferedReader(new FileReader(arquivo))){
                        br.readLine();
                        String line=br.readLine();
                        while(line!= null) {
                            String[] filtro = line.split(",");
                            String actionType = filtro[3];
                            if (sessionId.equals(filtro[2])){
                                fila.add(actionType);
                        }
                            line=br.readLine();
                        }
                        while(!fila.isEmpty()){
                        resultado.add(fila.remove());
                        }
                        return resultado;
                    }catch(IOException e){
                        System.out.println(e.getMessage());
                        return Collections.emptyList();
                    }



        }

        @Override
        public List<Alerta> priorizarAlertas(String arquivo, int n) throws IOException {
            // Implementar usando PriorityQueue<Alerta>
        }

        @Override
        public Map<Long, Long> encontrarPicosTransferencia(String arquivo) throws IOException {
            // Implementar usando Stack (Next Greater Element)
        }

        @Override
        public Optional<List<String>> rastrearContaminacao(String arquivo, String origem, String destino) throws IOException {
            // Implementar usando BFS em grafo
        }
    }

