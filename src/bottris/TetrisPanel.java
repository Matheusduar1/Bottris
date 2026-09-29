package bottris;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class TetrisPanel extends JPanel {
    private Timer timerQueda;
    private JogoFrame parent;
    private int linhasLimpasTotal = 0;
    private int score = 0; 
    private final int LINHAS_PARA_PROGRAMAR = 5;
    
    private final int LINHAS = 20, COLUNAS = 10, TAM_BLOCO = 25;
    private int[][] grade = new int[LINHAS][COLUNAS];
    private Color[] cores = {Color.BLACK, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.BLUE, Color.ORANGE, Color.GREEN, Color.RED};
    
    private final int[][][] PECAS = {
        {{1,1,1,1}}, {{2,2},{2,2}}, {{0,3,0},{3,3,3}}, {{4,0,0},{4,4,4}}, {{0,0,5},{5,5,5}}, {{0,6,6},{6,6,0}}, {{7,7,0},{0,7,7}}
    };
    
    private int[][] pecaAtual, proximaPeca;
    private int pecaX, pecaY, corAtual, proximaCor;
    private Random random = new Random();
    private boolean gameOver = false;
    private int lockDelayTimer = 0;
    private JPanel painelGameOver;
    
    // Variáveis da Contagem Regressiva
    private Timer timerContagem;
    private int tempoRetorno = 0;
    
    public TetrisPanel(JogoFrame parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(new Color(35, 35, 40)); 
        configurarControles();
        sortearProximaPeca();
        gerarNovaPeca();
        criarPainelGameOver();
        
        timerQueda = new Timer(400, e -> tickTetris());
        
        timerContagem = new Timer(1000, e -> {
            tempoRetorno--;
            if (tempoRetorno <= 0) {
                timerContagem.stop();
                retomarJogo();
            }
            repaint();
        });
    }
    
    public void iniciarContagemRegressiva() {
        tempoRetorno = 3;
        timerContagem.start();
        repaint();
    }
    
    private void criarPainelGameOver() {
        painelGameOver = new JPanel(new GridLayout(3, 1, 10, 10));
        painelGameOver.setBounds(50, 200, 250, 150);
        painelGameOver.setOpaque(false);
        painelGameOver.setVisible(false);
        
        JButton btnReiniciar = new JButton("Reiniciar");
        JButton btnMenu = new JButton("Voltar ao Menu");
        JButton btnSair = new JButton("Sair do Jogo");
        
        btnReiniciar.addActionListener(e -> parent.iniciarNovoJogo());
        btnMenu.addActionListener(e -> parent.voltarParaMenu());
        btnSair.addActionListener(e -> System.exit(0));
        
        painelGameOver.add(btnReiniciar);
        painelGameOver.add(btnMenu);
        painelGameOver.add(btnSair);
        add(painelGameOver);
    }
    
    private void configurarControles() {
        mapearTecla("ESQ", KeyEvent.VK_LEFT, -1, 0);
        mapearTecla("DIR", KeyEvent.VK_RIGHT, 1, 0);
        mapearTecla("BAIXO", KeyEvent.VK_DOWN, 0, 1);
        
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "GIRAR");
        getActionMap().put("GIRAR", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (!gameOver && timerQueda.isRunning()) girarPeca(); }
        });
    }
    
    private void mapearTecla(String nome, int keyCode, int dx, int dy) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), nome);
        getActionMap().put(nome, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!gameOver && timerQueda.isRunning() && podeMover(pecaAtual, pecaX + dx, pecaY + dy)) {
                    pecaX += dx; pecaY += dy; 
                    if(dy > 0) { lockDelayTimer = 0; score++; } 
                    repaint();
                }
            }
        });
    }
    
    private void sortearProximaPeca() {
        int tipo = random.nextInt(7);
        proximaPeca = PECAS[tipo];
        proximaCor = tipo + 1;
    }
    
    private void gerarNovaPeca() {
        pecaAtual = proximaPeca;
        corAtual = proximaCor;
        pecaX = COLUNAS / 2 - pecaAtual[0].length / 2;
        pecaY = 0;
        lockDelayTimer = 0;
        sortearProximaPeca();
        if (!podeMover(pecaAtual, pecaX, pecaY)) dispararGameOver();
    }
    
    private void dispararGameOver() {
        timerQueda.stop();
        gameOver = true;
        painelGameOver.setVisible(true);
        parent.pausarTetrisELiberarProgramacao(true);
        repaint();
    }
    
    private void girarPeca() {
        int l = pecaAtual.length, c = pecaAtual[0].length;
        int[][] rot = new int[c][l];
        for (int y = 0; y < l; y++) for (int x = 0; x < c; x++) rot[x][l - 1 - y] = pecaAtual[y][x];
        
        if (podeMover(rot, pecaX, pecaY)) { pecaAtual = rot; lockDelayTimer = 0; }
        else if (podeMover(rot, pecaX - 1, pecaY)) { pecaAtual = rot; pecaX--; lockDelayTimer = 0; }
        else if (podeMover(rot, pecaX + 1, pecaY)) { pecaAtual = rot; pecaX++; lockDelayTimer = 0; }
        repaint();
    }
    
    private boolean podeMover(int[][] peca, int nx, int ny) {
        for (int y = 0; y < peca.length; y++) {
            for (int x = 0; x < peca[y].length; x++) {
                if (peca[y][x] != 0) {
                    int ax = nx + x, ay = ny + y;
                    if (ax < 0 || ax >= COLUNAS || ay >= LINHAS) return false;
                    if (ay >= 0 && grade[ay][ax] != 0) return false;
                }
            }
        }
        return true;
    }
    
    private void tickTetris() {
        if (podeMover(pecaAtual, pecaX, pecaY + 1)) {
            pecaY++;
            lockDelayTimer = 0;
        } else {
            lockDelayTimer++;
            if (lockDelayTimer > 1) {
                fixarPeca();
                verificarLinhas();
                gerarNovaPeca();
            }
        }
        repaint();
    }
    
    private void fixarPeca() {
        for (int y = 0; y < pecaAtual.length; y++) {
            for (int x = 0; x < pecaAtual[y].length; x++) {
                if (pecaAtual[y][x] != 0) grade[pecaY + y][pecaX + x] = corAtual;
            }
        }
    }
    
    private void verificarLinhas() {
        int linhas = 0;
        for (int y = LINHAS - 1; y >= 0; y--) {
            boolean cheia = true;
            for (int x = 0; x < COLUNAS; x++) if (grade[y][x] == 0) { cheia = false; break; }
            if (cheia) {
                linhas++;
                for (int ty = y; ty > 0; ty--) System.arraycopy(grade[ty - 1], 0, grade[ty], 0, COLUNAS);
                y++; 
            }
        }
        if (linhas > 0) {
            score += (linhas * 100) * linhas; 
            linhasLimpasTotal += linhas;
            distribuirRecompensas(linhas);
            if (linhasLimpasTotal >= LINHAS_PARA_PROGRAMAR) {
                timerQueda.stop();
                linhasLimpasTotal -= LINHAS_PARA_PROGRAMAR;
                parent.pausarTetrisELiberarProgramacao(false);
            }
        }
    }
    
    private void distribuirRecompensas(int l) {
        parent.getProgramacaoPanel().adicionarBloco(Comando.ANDAR, l * 2);
        parent.getProgramacaoPanel().adicionarBloco(Comando.VIRAR_DIR, l);
        parent.getProgramacaoPanel().adicionarBloco(Comando.VIRAR_ESQ, l);
        parent.getProgramacaoPanel().adicionarBloco(Comando.ATACAR, l);
        parent.getProgramacaoPanel().adicionarBloco(Comando.ESPERAR, l);
        if (l >= 2) parent.getProgramacaoPanel().adicionarBloco(Comando.MULTIPLICAR, 1);
        if (l >= 4) parent.getProgramacaoPanel().adicionarBloco(Comando.LOOP, 1);
    }
    
    public void iniciarJogo() { timerQueda.start(); }
    public void retomarJogo() { if(!gameOver) timerQueda.start(); }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int offX = 20, offY = 20;
        
        g.setColor(new Color(20, 20, 25));
        g.fillRect(offX, offY, COLUNAS * TAM_BLOCO, LINHAS * TAM_BLOCO);
        g.setColor(new Color(60, 60, 65));
        for (int i = 0; i <= COLUNAS; i++) g.drawLine(offX + i*TAM_BLOCO, offY, offX + i*TAM_BLOCO, offY + LINHAS*TAM_BLOCO);
        for (int i = 0; i <= LINHAS; i++) g.drawLine(offX, offY + i*TAM_BLOCO, offX + COLUNAS*TAM_BLOCO, offY + i*TAM_BLOCO);
        
        for (int y = 0; y < LINHAS; y++) {
            for (int x = 0; x < COLUNAS; x++) {
                if (grade[y][x] != 0) desenharBloco(g, offX + x * TAM_BLOCO, offY + y * TAM_BLOCO, cores[grade[y][x]], TAM_BLOCO);
            }
        }
        
        if (pecaAtual != null) {
            for (int y = 0; y < pecaAtual.length; y++) {
                for (int x = 0; x < pecaAtual[y].length; x++) {
                    if (pecaAtual[y][x] != 0) desenharBloco(g, offX + (pecaX + x) * TAM_BLOCO, offY + (pecaY + y) * TAM_BLOCO, cores[corAtual], TAM_BLOCO);
                }
            }
        }
        
        // UI (Score, Próxima)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("SCORE: " + score, offX + COLUNAS * TAM_BLOCO + 10, offY + 15);
        g.drawString("PRÓXIMA:", offX + COLUNAS * TAM_BLOCO + 10, offY + 50);
        g.drawRect(offX + COLUNAS * TAM_BLOCO + 10, offY + 60, 80, 80);
        
        if (proximaPeca != null) {
            int miniTam = 15;
            int pxOff = (80 - (proximaPeca[0].length * miniTam)) / 2;
            int pyOff = (80 - (proximaPeca.length * miniTam)) / 2;
            for (int y = 0; y < proximaPeca.length; y++) {
                for (int x = 0; x < proximaPeca[y].length; x++) {
                    if (proximaPeca[y][x] != 0) {
                        desenharBloco(g, offX + COLUNAS * TAM_BLOCO + 10 + pxOff + x * miniTam, offY + 60 + pyOff + y * miniTam, cores[proximaCor], miniTam);
                    }
                }
            }
        }
        
        // CORREÇÃO: MOSTRADOR DE LINHAS RESTANTES
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 11)); // Fonte ajustada para caber no limite lateral
        int faltam = Math.max(0, LINHAS_PARA_PROGRAMAR - linhasLimpasTotal);
        g.drawString("FALTAM: " + faltam, offX + COLUNAS * TAM_BLOCO + 10, offY + 165);
        g.drawString("LINHAS P/", offX + COLUNAS * TAM_BLOCO + 10, offY + 180);
        g.drawString("PROGRAMAR", offX + COLUNAS * TAM_BLOCO + 10, offY + 195);
        
        // TELA DE CONTAGEM REGRESSIVA (Prepare-se)
        if (tempoRetorno > 0) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PREPARE-SE!", 20, 200);
            g.setFont(new Font("Arial", Font.BOLD, 80));
            g.drawString(String.valueOf(tempoRetorno), 130, 300);
        }
        
        // TELA DE GAME OVER
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 40, 150);
        }
    }
    
    private void desenharBloco(Graphics g, int x, int y, Color c, int tamanho) {
        g.setColor(c);
        g.fillRect(x, y, tamanho, tamanho);
        g.setColor(c.darker());
        g.drawRect(x, y, tamanho, tamanho);
    }
}