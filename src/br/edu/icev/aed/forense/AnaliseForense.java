package br.edu.icev.aed.forense;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.Alerta;
import java.io.*;
import java.util.*;
import java.util.Collections;

public class AnaliseForense implements AnaliseForenseAvancada {

    public AnaliseForense() {
    }

    @Override
    public Set<String> encontrarSessoesInvalidas(String arquivo) throws IOException {
        Map<String, Stack<String>> verificarInvalidas = new HashMap<>();
        Set<String> resultado = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {

            br.readLine();
            String line = br.readLine();

            while (line != null) {
                if (line.trim().isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] vect = line.split(",");

                verificarInvalidas.putIfAbsent(vect[1], new Stack<>());


                if (vect[3].equals("LOGIN")) {
                    if (!verificarInvalidas.get(vect[1]).isEmpty()) {
                        resultado.add(vect[2]);
                    }
                    verificarInvalidas.get(vect[1]).push(vect[2]);
                }


                if (vect[3].equals("LOGOUT")) {
                    if (verificarInvalidas.get(vect[1]).isEmpty() ||
                            !verificarInvalidas.get(vect[1]).peek().equals(vect[2])) {
                        resultado.add(vect[2]);
                    } else {
                        verificarInvalidas.get(vect[1]).pop();
                    }
                }

                line = br.readLine();
            }


            for (String usuario : verificarInvalidas.keySet()) {
                Stack<String> pilha = verificarInvalidas.get(usuario);
                while (!pilha.isEmpty()) {
                    resultado.add(pilha.pop());
                }
            }

            return resultado;

        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public List<String> reconstruirLinhaTempo(String arquivo, String sessionId) throws IOException {
        Queue<String> fila = new LinkedList<>();
        List<String> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                if (line.trim().isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] filtro = line.split(",");
                String actionType = filtro[3];
                if (sessionId.equals(filtro[2])) {
                    fila.add(actionType);
                }
                line = br.readLine();
            }
            while (!fila.isEmpty()) {
                resultado.add(fila.remove());
            }
            return resultado;
        } catch (IOException e) {

            return Collections.emptyList();
        }


    }

    @Override
    public List<Alerta> priorizarAlertas(String arquivo, int n) throws IOException {
        if (n == 0) {
            return Collections.emptyList();
        }

        Comparator<Alerta> comparadorDeSeveridade = (alerta1, alerta2) ->
                Integer.compare(alerta2.getSeverityLevel(), alerta1.getSeverityLevel());

        PriorityQueue<Alerta> filaDePrioridade = new PriorityQueue<>(comparadorDeSeveridade);

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] campos = line.split(",");
                long timestamp = Long.parseLong(campos[0]);
                String userId = campos[1];
                String sessionId = campos[2];
                String actionType = campos[3];
                String targetResource = campos[4];
                int severityLevel = Integer.parseInt(campos[5]);
                long bytesTransferred = Long.parseLong(campos[6]);
                Alerta alertaNovo = new Alerta(
                        timestamp,
                        userId,
                        sessionId,
                        actionType,
                        targetResource,
                        severityLevel,
                        bytesTransferred
                );
                filaDePrioridade.add(alertaNovo);
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        List<Alerta> resultado = new ArrayList<>();
        for (int i = 0; i < n && !filaDePrioridade.isEmpty(); i++) {
            resultado.add(filaDePrioridade.poll());
        }
        return resultado;
    }

    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String arquivo) throws IOException {
        List<Long> timestamps = new ArrayList<>();
        List<Long> bytes = new ArrayList<>();
        Map<Long, Long> resultado = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {


            br.readLine();
            String line = br.readLine();

            while (line != null) {
                if (line.trim().isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] vect = line.split(",");

                long timestamp = Long.parseLong(vect[0].trim());
                long bytesTransferidos = Long.parseLong(vect[6].trim());

                timestamps.add(timestamp);
                bytes.add(bytesTransferidos);

                line = br.readLine();
            }

            Stack<Integer> pilha = new Stack<>();

            for (int i = 0; i < bytes.size(); i++) {

                while (!pilha.isEmpty() && bytes.get(i) > bytes.get(pilha.peek())) {

                    int idxAnterior = pilha.pop();
                    long tsAnterior = timestamps.get(idxAnterior);

                    resultado.put(tsAnterior, timestamps.get(i));
                }

                pilha.push(i);
            }

            return resultado;

        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }


    @Override
    public Optional<List<String>> rastrearContaminacao(String arquivo, String origem, String destino) throws IOException {
        Map<String, List<String>> listaAdj = new HashMap<>();
        Map<String, String> sessionTracker = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                if (line.trim().isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] vect = line.split(",");

                if (sessionTracker.containsKey(vect[2])) {
                    String recursoAnt = sessionTracker.get(vect[2]);

                    listaAdj.putIfAbsent(recursoAnt, new ArrayList<>());
                    listaAdj.get(recursoAnt).add(vect[4]);
                }
                sessionTracker.put(vect[2], vect[4]);
                line = br.readLine();
            }
            Queue<String> fila = new LinkedList<>();
            Map<String, String> predecessor = new HashMap<>();

            fila.add(origem);
            predecessor.put(origem, null);
            while (!fila.isEmpty()) {
                String atual = fila.remove();

                if (listaAdj.containsKey(atual)) {
                    for (String v : listaAdj.get(atual)) {
                        if (!predecessor.containsKey(v)) {
                            predecessor.put(v, atual);
                            fila.add(v);
                        }

                        if (v.equals(destino)) {
                            List<String> caminhoSucesso = new LinkedList<>();
                            String recursoAtual = destino;
                            while (recursoAtual != null) {
                                caminhoSucesso.addFirst(recursoAtual);
                                recursoAtual = predecessor.get(recursoAtual);
                            }
                            return Optional.of(caminhoSucesso);
                        }
                    }
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();

    }
}



