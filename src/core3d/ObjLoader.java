package core3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjLoader {

    public static List<Triangulo3D> carrega(String nomeArquivo) throws IOException {
        ArrayList<Ponto3D> vertices = new ArrayList<>();
        ArrayList<Triangulo3D> triangulos = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            int numeroLinha = 0;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                linha = linha.trim();

                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }

                String[] partes = linha.split("\\s+");
                if ("v".equals(partes[0])) {
                    if (partes.length < 4) {
                        throw new IOException("Vertice invalido na linha " + numeroLinha);
                    }

                    float x = Float.parseFloat(partes[1]);
                    float y = Float.parseFloat(partes[2]);
                    float z = Float.parseFloat(partes[3]);
                    vertices.add(new Ponto3D(x, y, z));
                } else if ("f".equals(partes[0])) {
                    if (partes.length < 4) {
                        throw new IOException("Face invalida na linha " + numeroLinha);
                    }

                    int primeiro = indiceVertice(partes[1], vertices.size());
                    for (int i = 2; i < partes.length - 1; i++) {
                        int segundo = indiceVertice(partes[i], vertices.size());
                        int terceiro = indiceVertice(partes[i + 1], vertices.size());

                        triangulos.add(new Triangulo3D(
                                vertices.get(primeiro),
                                vertices.get(segundo),
                                vertices.get(terceiro)));
                    }
                }
            }
        } catch (NumberFormatException erro) {
            throw new IOException("Numero invalido no arquivo OBJ " + nomeArquivo, erro);
        }

        return triangulos;
    }

    public static List<Triangulo3D> carregaPasta(String nomePasta) {
        ArrayList<Triangulo3D> triangulos = new ArrayList<>();
        File pasta = new File(nomePasta);

        if (!pasta.isDirectory()) {
            System.out.println("Pasta de OBJ nao encontrada: " + pasta.getPath());
            return triangulos;
        }

        File[] arquivos = pasta.listFiles();
        if (arquivos == null) {
            return triangulos;
        }

        Arrays.sort(arquivos, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        int numeroObjeto = 0;

        for (File arquivo : arquivos) {
            if (!arquivo.isFile() || !arquivo.getName().toLowerCase().endsWith(".obj")) {
                continue;
            }

            try {
                List<Triangulo3D> objeto = carrega(arquivo.getPath());
                preparaParaTela(objeto, arquivo.getName(), numeroObjeto++);
                triangulos.addAll(objeto);
                System.out.println("OBJ carregado: " + arquivo.getName()
                        + " (" + objeto.size() + " triangulos)");
            } catch (IOException erro) {
                System.err.println("Nao foi possivel carregar " + arquivo.getPath()
                        + ": " + erro.getMessage());
            }
        }

        return triangulos;
    }

        private static void preparaParaTela(
            List<Triangulo3D> triangulos, String nomeArquivo, int numeroObjeto) {
        if (triangulos.isEmpty()) {
            return;
        }

        float minimoX = Float.MAX_VALUE;
        float minimoY = Float.MAX_VALUE;
        float minimoZ = Float.MAX_VALUE;
        float maximoX = -Float.MAX_VALUE;
        float maximoY = -Float.MAX_VALUE;
        float maximoZ = -Float.MAX_VALUE;

        for (Triangulo3D triangulo : triangulos) {
            Ponto3D[] pontos = { triangulo.pa, triangulo.pb, triangulo.pc };
            for (Ponto3D ponto : pontos) {
                minimoX = Math.min(minimoX, ponto.x);
                minimoY = Math.min(minimoY, ponto.y);
                minimoZ = Math.min(minimoZ, ponto.z);
                maximoX = Math.max(maximoX, ponto.x);
                maximoY = Math.max(maximoY, ponto.y);
                maximoZ = Math.max(maximoZ, ponto.z);
            }
        }

        float largura = maximoX - minimoX;
        float altura = maximoY - minimoY;
        float profundidade = maximoZ - minimoZ;
        float maiorDimensao = Math.max(largura, Math.max(altura, profundidade));

        if (maiorDimensao <= 0) {
            return;
        }

        ConfiguracaoObjeto configuracao = configuracaoInicial(nomeArquivo, numeroObjeto);
        float escala = configuracao.tamanhoPixels / maiorDimensao;
        float centroModeloX = (minimoX + maximoX) / 2.0f;
        float centroModeloZ = (minimoZ + maximoZ) / 2.0f;
        float radianos = configuracao.rotacaoY * 0.017453f;
        float seno = (float) Math.sin(radianos);
        float cosseno = (float) Math.cos(radianos);

        for (Triangulo3D triangulo : triangulos) {
            Ponto3D[] pontos = { triangulo.pa, triangulo.pb, triangulo.pc };
            for (Ponto3D ponto : pontos) {
            float xCentralizado = ponto.x - centroModeloX;
            float zCentralizado = ponto.z - centroModeloZ;
            float xRotacionado = xCentralizado * cosseno
                - zCentralizado * seno;
            float zRotacionado = xCentralizado * seno
                + zCentralizado * cosseno;

            ponto.x = xRotacionado * escala + configuracao.x;
                ponto.y = configuracao.y - (ponto.y - minimoY) * escala;
            ponto.z = zRotacionado * escala + configuracao.z;
            }
        }
    }

    private static ConfiguracaoObjeto configuracaoInicial(
            String nomeArquivo, int numeroObjeto) {
        String nome = nomeArquivo.toLowerCase();

        if (nome.equals("tank.obj")) {
            return new ConfiguracaoObjeto(420, 390, -400, 180, 90);
        }
        if (nome.equals("soldado.obj")) {
            return new ConfiguracaoObjeto(270, 390, 0, 100, 0);
        }
        if (nome.equals("medieval house.obj")) {
            return new ConfiguracaoObjeto(420, 390, 250, 220, 0);
        }
        if (nome.equals("arvore.obj")) {
            return new ConfiguracaoObjeto(570, 390, -120, 130, 0);
        }


        float x = 110.0f + (numeroObjeto % 3) * 210.0f;
        float y = 380.0f - (numeroObjeto / 3) * 180.0f;
        return new ConfiguracaoObjeto(x, y, 0, 140, 0);
    }

    private static class ConfiguracaoObjeto {
        float x;
        float y;
        float z;
        float tamanhoPixels;
        float rotacaoY;

        ConfiguracaoObjeto(
                float x, float y, float z, float tamanhoPixels, float rotacaoY) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.tamanhoPixels = tamanhoPixels;
            this.rotacaoY = rotacaoY;
        }
    }

    private static int indiceVertice(String texto, int quantidadeVertices) throws IOException {
        try {
            int indice = Integer.parseInt(texto.split("/")[0]);
            int indiceZeroBased = indice > 0 ? indice - 1 : quantidadeVertices + indice;

            if (indiceZeroBased < 0 || indiceZeroBased >= quantidadeVertices) {
                throw new IOException("Indice de vertice fora do intervalo: " + texto);
            }

            return indiceZeroBased;
        } catch (NumberFormatException erro) {
            throw new IOException("Indice de vertice invalido: " + texto, erro);
        }
    }
}