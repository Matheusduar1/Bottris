package bottris;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class LabirintoPanel extends JPanel {
    private final int TAM_CELULA = 40, GRID_TAM = 8;
    private int[][] mapa = new int[GRID_TAM][GRID_TAM]; 
    
    private int roboX = 0, roboY = 0, direcao = 1; 
    private int startRoboX = 0, startRoboY = 0, startRoboDir = 1;
    private int nivelAtual = 1;
    
    private List<Inimigo> inimigos = new ArrayList<>();
    private Random random = new Random();
    
    // Variáveis da Simulação "Fantasma"
    private List<Point> caminhoFantasma = new ArrayList<>();
    private List<Inimigo> inimigosFantasma = new ArrayList<>();
    private int fantasmaX = -1, fantasmaY = -1, fantasmaDir = -1;
    private boolean colidiuNaSimulacao = false;
    private Timer timerExecucao;
    private int[][] mapaInicial = new int[GRID_TAM][GRID_TAM];
    
    class Inimigo {
        int x, y, startX, startY, eixo, dirPatrulha = 1, startDirPatrulha = 1;
        boolean vivo = true;
        Inimigo(int x, int y, int eixo) { 
            this.x = x; this.y = y; this.startX = x; this.startY = y; this.eixo = eixo; 
        }
        void resetarPosicao() { x = startX; y = startY; dirPatrulha = startDirPatrulha; vivo = true; }
    }
    
    public LabirintoPanel() {
        setBackground(new Color(30, 30, 35));
        gerarMapaProcedural();
    }
    
    private void gerarMapaProcedural() {
        inimigos.clear();
        caminhoFantasma.clear();
        inimigosFantasma.clear();
        colidiuNaSimulacao = false;
        
        do {
            for(int i=0; i<GRID_TAM; i++) for(int j=0; j<GRID_TAM; j++) mapa[i][j] = 0; 
            int barreiras = 10 + random.nextInt(5);
            for(int i=0; i<barreiras; i++) {
                int bx = random.nextInt(GRID_TAM), by = random.nextInt(GRID_TAM);
                if ((bx > 1 || by > 1) && (bx < GRID_TAM-2 || by < GRID_TAM-2)) mapa[by][bx] = 1; 
            }
            mapa[GRID_TAM-1][GRID_TAM-1] = 2; // Saída
        } while (!existeCaminhoLivre());
        
        for(int i=0; i<GRID_TAM; i++) System.arraycopy(mapa[i], 0, mapaInicial[i], 0, GRID_TAM);
        
        roboX = 0; roboY = 0; direcao = 1;
        startRoboX = 0; startRoboY = 0; startRoboDir = 1;
        
        int qtdInimigos = Math.min(2 + (nivelAtual / 2), 4);
        for(int i=0; i<qtdInimigos; i++) {
            int ex, ey;
            do { ex = random.nextInt(GRID_TAM); ey = random.nextInt(GRID_TAM);
            } while(mapa[ey][ex] != 0 || (ex < 3 && ey < 3)); 
            inimigos.add(new Inimigo(ex, ey, random.nextInt(2)));
        }
    }
    
    private boolean existeCaminhoLivre() {
        boolean[][] visitado = new boolean[GRID_TAM][GRID_TAM];
        java.util.Queue<Point> fila = new java.util.LinkedList<>();
        fila.add(new Point(0, 0));
        visitado[0][0] = true;
        int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
        while (!fila.isEmpty()) {
            Point p = fila.poll();
            if (p.x == GRID_TAM - 1 && p.y == GRID_TAM - 1) return true;
            for (int d = 0; d < 4; d++) {
                int nx = p.x + dx[d], ny = p.y + dy[d];
                if (nx >= 0 && nx < GRID_TAM && ny >= 0 && ny < GRID_TAM && !visitado[ny][nx] && mapa[ny][nx] != 1) {
                    visitado[ny][nx] = true;
                    fila.add(new Point(nx, ny));
                }
            }
        }
        return false;
    }
    
    private void resetarFase() {
        roboX = startRoboX; roboY = startRoboY; direcao = startRoboDir;
        for(int i=0; i<GRID_TAM; i++) System.arraycopy(mapaInicial[i], 0, mapa[i], 0, GRID_TAM);
        for (Inimigo in : inimigos) in.resetarPosicao();
    }

    public void pararTimers() {
        if (timerExecucao != null && timerExecucao.isRunning()) timerExecucao.stop();
    }
    
    public void limparFantasma() {
        caminhoFantasma.clear();
        inimigosFantasma.clear();
        fantasmaX = -1;
        colidiuNaSimulacao = false;
        repaint();
    }
    
    // --- LÓGICA MASSIVA DO FANTASMA PREVENDO O FUTURO DOS INIMIGOS ---
    public void simularCaminho(List<Comando> brutos) {
        limparFantasma();
        fantasmaX = roboX; fantasmaY = roboY; fantasmaDir = direcao;
        caminhoFantasma.add(new Point(fantasmaX, fantasmaY));
        
        // Cria cópias idênticas do mapa e dos inimigos para prever movimentos e paredes quebradas
        int[][] mapaSim = new int[GRID_TAM][GRID_TAM];
        for(int i=0; i<GRID_TAM; i++) System.arraycopy(mapa[i], 0, mapaSim[i], 0, GRID_TAM);
        
        for(Inimigo in : inimigos) {
            if(in.vivo) {
                Inimigo f = new Inimigo(in.x, in.y, in.eixo);
                f.dirPatrulha = in.dirPatrulha;
                inimigosFantasma.add(f);
            }
        }
        
        List<Comando> compilado = compilarCodigo(brutos);
        for (Comando cmd : compilado) {
            // 1. Move o Robô Fantasma
            if (cmd == Comando.VIRAR_DIR) fantasmaDir = (fantasmaDir + 1) % 4;
            else if (cmd == Comando.VIRAR_ESQ) fantasmaDir = (fantasmaDir + 3) % 4;
            else if (cmd == Comando.ATACAR) {
                int tx = fantasmaX, ty = fantasmaY;
                if (fantasmaDir == 0) ty--; if (fantasmaDir == 1) tx++; if (fantasmaDir == 2) ty++; if (fantasmaDir == 3) tx--;
                if (tx >= 0 && tx < GRID_TAM && ty >= 0 && ty < GRID_TAM && mapaSim[ty][tx] == 1) mapaSim[ty][tx] = 0;
                for(Inimigo f : inimigosFantasma) if (f.vivo && f.x == tx && f.y == ty) f.vivo = false;
            }
            else if (cmd == Comando.ANDAR) {
                int nx = fantasmaX, ny = fantasmaY;
                if (fantasmaDir == 0) ny--; if (fantasmaDir == 1) nx++; if (fantasmaDir == 2) ny++; if (fantasmaDir == 3) nx--;
                if (nx >= 0 && nx < GRID_TAM && ny >= 0 && ny < GRID_TAM && mapaSim[ny][nx] != 1) {
                    fantasmaX = nx; fantasmaY = ny;
                }
            }

            int oldRoboX = caminhoFantasma.get(caminhoFantasma.size() - 1).x;
            int oldRoboY = caminhoFantasma.get(caminhoFantasma.size() - 1).y;

            // 2. Move os Inimigos Fantasma (Pois eles se movem a cada bloco!)
            for(Inimigo f : inimigosFantasma) {
                if(!f.vivo) continue;
                int oldInX = f.x, oldInY = f.y;
                int nx = f.x + (f.eixo == 0 ? f.dirPatrulha : 0);
                int ny = f.y + (f.eixo == 1 ? f.dirPatrulha : 0);
                if (nx < 0 || nx >= GRID_TAM || ny < 0 || ny >= GRID_TAM || mapaSim[ny][nx] == 1 || mapaSim[ny][nx] == 2) {
                    f.dirPatrulha *= -1;
                } else {
                    f.x = nx; f.y = ny;
                }
                // 3. Checa colisão no futuro (mesma célula ou cruzamento)
                if ((f.x == fantasmaX && f.y == fantasmaY) || (f.x == oldRoboX && f.y == oldRoboY && oldInX == fantasmaX && oldInY == fantasmaY)) {
                    colidiuNaSimulacao = true;
                }
            }
            
            caminhoFantasma.add(new Point(fantasmaX, fantasmaY));
            if (colidiuNaSimulacao || mapaSim[fantasmaY][fantasmaX] == 2) break; // Para o desenho do fantasma se morrer ou chegar na saída
        }
        repaint();
    }
    
    public void executarComandos(List<Comando> comandosBrutos, Runnable callbackFim) {
        List<Comando> codigoFinal = compilarCodigo(comandosBrutos);
        if (timerExecucao != null && timerExecucao.isRunning()) timerExecucao.stop();
        timerExecucao = new Timer(400, null);
        var iterador = codigoFinal.iterator();
        
        timerExecucao.addActionListener(e -> {
            if (iterador.hasNext()) {
                int antRoboX = roboX, antRoboY = roboY;
                processarComando(iterador.next());
                boolean colidiu = moverInimigosEVerificarColisao(antRoboX, antRoboY);
                
                if (colidiu) {
                    timerExecucao.stop();
                    resetarFase();
                    repaint();
                    callbackFim.run();
                    return;
                }
                
                if (mapa[roboY][roboX] == 2) {
                    timerExecucao.stop();
                    nivelAtual++;
                    gerarMapaProcedural(); 
                    repaint();
                    callbackFim.run();
                    return;
                }
                repaint();
            } else {
                timerExecucao.stop();
                callbackFim.run();
            }
        });
        timerExecucao.start();
    }
    
    private List<Comando> compilarCodigo(List<Comando> brutos) {
        List<Comando> compilado = new ArrayList<>();
        boolean aplicarMulti = false;
        for (Comando cmd : brutos) {
            if (cmd == Comando.MULTIPLICAR) { aplicarMulti = true; continue; }
            if (cmd == Comando.LOOP) { 
                List<Comando> copia = new ArrayList<>(compilado);
                compilado.addAll(copia);
                continue; 
            }
            int vezes = aplicarMulti ? 2 : 1;
            aplicarMulti = false;
            for(int i=0; i<vezes; i++) compilado.add(cmd);
        }
        return compilado;
    }
    
    private void processarComando(Comando cmd) {
        if (cmd == Comando.VIRAR_DIR) direcao = (direcao + 1) % 4;
        else if (cmd == Comando.VIRAR_ESQ) direcao = (direcao + 3) % 4;
        else if (cmd == Comando.ATACAR) {
            int tx = roboX, ty = roboY;
            if (direcao == 0) ty--; if (direcao == 1) tx++; if (direcao == 2) ty++; if (direcao == 3) tx--;
            if(tx >=0 && tx < GRID_TAM && ty >=0 && ty < GRID_TAM) {
                if (mapa[ty][tx] == 1) mapa[ty][tx] = 0; 
                for (Inimigo in : inimigos) if (in.vivo && in.x == tx && in.y == ty) in.vivo = false;
            }
        }
        else if (cmd == Comando.ANDAR) {
            int nx = roboX, ny = roboY;
            if (direcao == 0) ny--; if (direcao == 1) nx++; if (direcao == 2) ny++; if (direcao == 3) nx--;
            if (nx >= 0 && nx < GRID_TAM && ny >= 0 && ny < GRID_TAM && mapa[ny][nx] != 1) {
                roboX = nx; roboY = ny;
            }
        }
    }
    
    private boolean moverInimigosEVerificarColisao(int antRoboX, int antRoboY) {
        for (Inimigo in : inimigos) {
            if (!in.vivo) continue;
            int antInX = in.x, antInY = in.y;
            int nx = in.x + (in.eixo == 0 ? in.dirPatrulha : 0);
            int ny = in.y + (in.eixo == 1 ? in.dirPatrulha : 0);
            if (nx < 0 || nx >= GRID_TAM || ny < 0 || ny >= GRID_TAM || mapa[ny][nx] == 1 || mapa[ny][nx] == 2) {
                in.dirPatrulha *= -1; 
            } else {
                in.x = nx; in.y = ny;
            }
            if ((in.x == roboX && in.y == roboY) || (in.x == antRoboX && in.y == antRoboY && antInX == roboX && antInY == roboY)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int offX = (getWidth() - (GRID_TAM * TAM_CELULA)) / 2;
        int offY = (getHeight() - (GRID_TAM * TAM_CELULA)) / 2;
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Nível: " + nivelAtual, 15, 30);
        
        for(int i=0; i<GRID_TAM; i++) {
            for(int j=0; j<GRID_TAM; j++) {
                int px = offX + j * TAM_CELULA, py = offY + i * TAM_CELULA;
                if (mapa[i][j] == 1) {
                    // Parede / Bunker destrutível (estilo tijolo/bloco blindado)
                    g2.setColor(new Color(45, 35, 30));
                    g2.fillRect(px, py, TAM_CELULA, TAM_CELULA);
                    g2.setColor(new Color(75, 55, 45));
                    g2.fillRect(px + 3, py + 3, 16, 15);
                    g2.fillRect(px + 21, py + 3, 16, 15);
                    g2.fillRect(px + 3, py + 21, 10, 15);
                    g2.fillRect(px + 15, py + 21, 22, 15);
                    g2.setColor(new Color(20, 15, 12));
                    g2.drawRect(px, py, TAM_CELULA, TAM_CELULA);
                } else if (mapa[i][j] == 2) {
                    // Portal / Base de Extração Militar
                    g2.setColor(new Color(25, 95, 45));
                    g2.fillRect(px, py, TAM_CELULA, TAM_CELULA);
                    g2.setColor(new Color(50, 220, 90));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(px + 4, py + 4, TAM_CELULA - 8, TAM_CELULA - 8);
                    g2.drawOval(px + 9, py + 9, 22, 22);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                    g2.drawString("H", px + 16, py + 25);
                    g2.setStroke(new BasicStroke(1));
                    g2.setColor(Color.BLACK);
                    g2.drawRect(px, py, TAM_CELULA, TAM_CELULA);
                } else {
                    // Piso metálico tático
                    g2.setColor(new Color(68, 72, 78));
                    g2.fillRect(px, py, TAM_CELULA, TAM_CELULA);
                    g2.setColor(new Color(82, 87, 94));
                    g2.fillRect(px + 3, py + 3, 2, 2);
                    g2.fillRect(px + TAM_CELULA - 5, py + 3, 2, 2);
                    g2.fillRect(px + 3, py + TAM_CELULA - 5, 2, 2);
                    g2.fillRect(px + TAM_CELULA - 5, py + TAM_CELULA - 5, 2, 2);
                    g2.setColor(new Color(35, 38, 42));
                    g2.drawRect(px, py, TAM_CELULA, TAM_CELULA);
                }
            }
        }
        
        // DESENHA INIMIGOS "FANTASMA" (O Futuro deles)
        for (Inimigo f : inimigosFantasma) {
            if (f.vivo) {
                desenharInimigo(g2, offX + f.x * TAM_CELULA, offY + f.y * TAM_CELULA, f.eixo, f.dirPatrulha, true);
            }
        }
        
        // FANTASMA TANK (Rastro + Silhueta Final)
        if (caminhoFantasma.size() > 1) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 6}, 0));
            g2.setColor(colidiuNaSimulacao ? new Color(255, 60, 60, 190) : new Color(0, 255, 255, 160)); 
            for(int i = 0; i < caminhoFantasma.size() - 1; i++) {
                Point p1 = caminhoFantasma.get(i);
                Point p2 = caminhoFantasma.get(i+1);
                g2.drawLine(offX + p1.x * TAM_CELULA + 20, offY + p1.y * TAM_CELULA + 20, 
                            offX + p2.x * TAM_CELULA + 20, offY + p2.y * TAM_CELULA + 20);
            }
            g2.setStroke(oldStroke);
            desenharTank(g2, offX + fantasmaX * TAM_CELULA, offY + fantasmaY * TAM_CELULA, fantasmaDir, true, colidiuNaSimulacao);
        }
        
        // INIMIGOS REAIS (Mecha-Aranha / Drone de Patrulha)
        for (Inimigo in : inimigos) {
            if (in.vivo) {
                desenharInimigo(g2, offX + in.x * TAM_CELULA, offY + in.y * TAM_CELULA, in.eixo, in.dirPatrulha, false);
            }
        }
        
        // TANK DE GUERRA REAL
        desenharTank(g2, offX + roboX * TAM_CELULA, offY + roboY * TAM_CELULA, direcao, false, false);
    }

    private void desenharTank(Graphics2D g2, int rx, int ry, int dir, boolean fantasma, boolean colidiu) {
        Graphics2D g = (Graphics2D) g2.create();
        if (fantasma) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.48f));
        }
        // Rotaciona o canvas em torno do centro da célula (dir 0 = Cima, 1 = Direita, 2 = Baixo, 3 = Esquerda)
        double angulo = dir * (Math.PI / 2.0);
        g.rotate(angulo, rx + 20, ry + 20);

        Color corLagarta = fantasma ? (colidiu ? new Color(120, 20, 20) : new Color(0, 90, 100)) : new Color(35, 38, 40);
        Color corGomo = fantasma ? (colidiu ? new Color(180, 50, 50) : new Color(0, 150, 160)) : new Color(75, 80, 85);
        Color corChassi = fantasma ? (colidiu ? new Color(220, 40, 40) : new Color(0, 210, 230)) : new Color(75, 115, 55);
        Color corChassiBorda = fantasma ? (colidiu ? new Color(255, 100, 100) : new Color(120, 255, 255)) : new Color(45, 75, 30);
        Color corTorreta = fantasma ? (colidiu ? new Color(240, 70, 70) : new Color(50, 235, 250)) : new Color(95, 140, 70);
        Color corCanhao = fantasma ? (colidiu ? new Color(200, 50, 50) : new Color(0, 190, 210)) : new Color(55, 65, 50);

        // 1. Lagartas laterais (Tracks)
        g.setColor(corLagarta);
        g.fillRoundRect(rx + 4, ry + 5, 7, 30, 4, 4);
        g.fillRoundRect(rx + 29, ry + 5, 7, 30, 4, 4);
        g.setColor(corGomo);
        for (int ty = ry + 7; ty <= ry + 31; ty += 4) {
            g.fillRect(rx + 5, ty, 5, 2);
            g.fillRect(rx + 30, ty, 5, 2);
        }
        g.setColor(Color.BLACK);
        g.drawRoundRect(rx + 4, ry + 5, 7, 30, 4, 4);
        g.drawRoundRect(rx + 29, ry + 5, 7, 30, 4, 4);

        // 2. Chassi Blindado (Hull)
        g.setColor(corChassi);
        g.fillRoundRect(rx + 9, ry + 7, 22, 26, 6, 6);
        g.setColor(corChassiBorda);
        g.drawRoundRect(rx + 9, ry + 7, 22, 26, 6, 6);
        // Detalhes traseiros de exaustão
        g.setColor(new Color(30, 35, 25));
        g.fillRect(rx + 12, ry + 29, 6, 3);
        g.fillRect(rx + 22, ry + 29, 6, 3);

        // 3. Canhão Principal + Quebra-chamas (Muzzle Brake) apontando para o Topo (Norte)
        g.setColor(corCanhao);
        g.fillRect(rx + 18, ry + 1, 4, 15);
        g.setColor(Color.BLACK);
        g.drawRect(rx + 18, ry + 1, 4, 15);
        // Ponta do canhão (Muzzle)
        g.setColor(corLagarta);
        g.fillRect(rx + 17, ry, 6, 4);
        g.setColor(Color.BLACK);
        g.drawRect(rx + 17, ry, 6, 4);

        // 4. Torreta Central Giratória (Turret)
        g.setColor(corTorreta);
        g.fillRoundRect(rx + 12, ry + 12, 16, 15, 6, 6);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rx + 12, ry + 12, 16, 15, 6, 6);

        // 5. Escotilha e Visor Luminoso
        g.setColor(corChassiBorda);
        g.fillOval(rx + 16, ry + 16, 8, 8);
        g.setColor(fantasma ? Color.WHITE : new Color(255, 210, 50));
        g.fillRect(rx + 16, ry + 13, 8, 2); // Visor frontal amarelo/dourado

        g.dispose();
    }

    private void desenharInimigo(Graphics2D g2, int px, int py, int eixo, int dirPatrulha, boolean fantasma) {
        Graphics2D g = (Graphics2D) g2.create();
        if (fantasma) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        }

        // Rotaciona de acordo com a direção da patrulha (eixo 0: Leste=1/Oeste=-1, eixo 1: Sul=1/Norte=-1)
        int dir = (eixo == 0) ? (dirPatrulha == 1 ? 1 : 3) : (dirPatrulha == 1 ? 2 : 0);
        g.rotate(dir * (Math.PI / 2.0), px + 20, py + 20);

        Color corCorpo = fantasma ? new Color(255, 80, 150) : new Color(180, 30, 35);
        Color corArmadura = fantasma ? new Color(255, 130, 190) : new Color(90, 20, 25);
        Color corOlho = fantasma ? Color.WHITE : new Color(255, 230, 0);

        // 1. Garras / Lâminas Mecânicas laterais (estilo Drone Caçador / Mecha-Aranha)
        g.setColor(new Color(45, 45, 50));
        g.setStroke(new BasicStroke(2.5f));
        // Patas esquerdas e direitas
        g.drawLine(px + 10, py + 14, px + 3, py + 9);
        g.drawLine(px + 10, py + 20, px + 2, py + 20);
        g.drawLine(px + 10, py + 26, px + 3, py + 31);
        g.drawLine(px + 30, py + 14, px + 37, py + 9);
        g.drawLine(px + 30, py + 20, px + 38, py + 20);
        g.drawLine(px + 30, py + 26, px + 37, py + 31);
        g.setStroke(new BasicStroke(1f));

        // 2. Pinças frontais (apontando para a direção de movimento)
        g.setColor(new Color(200, 200, 210));
        g.fillPolygon(new int[]{px + 13, px + 16, px + 11}, new int[]{py + 3, py + 10, py + 10}, 3);
        g.fillPolygon(new int[]{px + 27, px + 24, px + 29}, new int[]{py + 3, py + 10, py + 10}, 3);

        // 3. Carapaça Blindada Octogonal/Oval
        g.setColor(corArmadura);
        g.fillOval(px + 7, py + 7, 26, 26);
        g.setColor(corCorpo);
        g.fillOval(px + 10, py + 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(px + 7, py + 7, 26, 26);

        // 4. Núcleo / Visor Cyber-Olho Frontal + Indicador de Direção
        g.setColor(Color.BLACK);
        g.fillRoundRect(px + 13, py + 10, 14, 7, 4, 4);
        g.setColor(corOlho);
        g.fillOval(px + 15, py + 11, 4, 4);
        g.fillOval(px + 21, py + 11, 4, 4);

        // Seta tática no centro da carapaça indicando sentido de patrulha
        g.setColor(Color.WHITE);
        if (fantasma) {
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("?", px + 17, py + 26);
        } else {
            g.fillPolygon(new int[]{px + 20, px + 15, px + 25}, new int[]{py + 18, py + 26, py + 26}, 3);
        }

        g.dispose();
    }
}