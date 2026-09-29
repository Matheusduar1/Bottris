package bottris;
import java.awt.Color;

public enum Comando {
    ANDAR(Color.CYAN, "Andar", new int[][]{{1,1,1,1}}),
    VIRAR_DIR(Color.ORANGE, "Dir", new int[][]{{0,0,1},{1,1,1}}),
    VIRAR_ESQ(Color.BLUE, "Esq", new int[][]{{1,0,0},{1,1,1}}),
    MULTIPLICAR(Color.YELLOW, "Multi", new int[][]{{1,1},{1,1}}),
    LOOP(Color.RED, "Loop", new int[][]{{1,1,0},{0,1,1}}),
    ESPERAR(Color.GREEN, "Espera", new int[][]{{0,1,1},{1,1,0}}),
    ATACAR(Color.MAGENTA, "Atacar", new int[][]{{0,1,0},{1,1,1}});

    public final Color cor;
    public final String nome;
    public final int[][] shape;

    Comando(Color cor, String nome, int[][] shape) {
        this.cor = cor;
        this.nome = nome;
        this.shape = shape;
    }
}