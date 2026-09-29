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
    
    class Inimigo {
        int x, y, startX, startY, eixo, dirPatrulha = 1;
        boolean vivo = true;
        Inimigo(int x, int y, int eixo) { 
            this.x = x; this.y = y; this.startX = x; this.startY = y; this.eixo = eixo; 
        }
        void resetarPosicao() { x = startX; y = startY; vivo = true; }
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
        
        for(int i=0; i<GRID_TAM; i++) for(int j=0; j<GRID_TAM; j++) mapa[i][j] = 0; 
        int barreiras = 10 + random.nextInt(5);
        for(int i=0; i<barreiras; i++) {
            int bx = random.nextInt(GRID_TAM), by = random.nextInt(GRID_TAM);
            if ((bx > 1 || by > 1) && (bx < GRID_TAM-2 || by < GRID_TAM-2)) mapa[by][bx] = 1; 
        }
        mapa[GRID_TAM-1][GRID_TAM-1] = 2; // Saída
        
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
    
    private void resetarFase() {
        roboX = startRoboX; roboY = startRoboY; direcao = startRoboDir;
        for (Inimigo in : inimigos) in.resetarPosicao();
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
        
        // Cria cópias idênticas dos inimigos para prever movimentos
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
                for(Inimigo f : inimigosFantasma) if (f.vivo && f.x == tx && f.y == ty) f.vivo = false;
            }
            else if (cmd == Comando.ANDAR) {
                int nx = fantasmaX, ny = fantasmaY;
                if (fantasmaDir == 0) ny--; if (fantasmaDir == 1) nx++; if (fantasmaDir == 2) ny++; if (fantasmaDir == 3) nx--;
                if (nx >= 0 && nx < GRID_TAM && ny >= 0 && ny < GRID_TAM && mapa[ny][nx] != 1) {
                    fantasmaX = nx; fantasmaY = ny;
                }
            }

            // 2. Move os Inimigos Fantasma (Pois eles se movem a cada bloco!)
            for(Inimigo f : inimigosFantasma) {
                if(!f.vivo) continue;
                int nx = f.x + (f.eixo == 0 ? f.dirPatrulha : 0);
                int ny = f.y + (f.eixo == 1 ? f.dirPatrulha : 0);
                if (nx < 0 || nx >= GRID_TAM || ny < 0 || ny >= GRID_TAM || mapa[ny][nx] == 1 || mapa[ny][nx] == 2) {
                    f.dirPatrulha *= -1;
                } else {
                    f.x = nx; f.y = ny;
                }
            }

            // 3. Checa colisão no futuro!
            for(Inimigo f : inimigosFantasma) {
                if(f.vivo && f.x == fantasmaX && f.y == fantasmaY) {
                    colidiuNaSimulacao = true;
                }
            }
            
            caminhoFantasma.add(new Point(fantasmaX, fantasmaY));
            if (colidiuNaSimulacao) break; // Para o desenho do fantasma se ele for morrer
        }
        repaint();
    }
    
    public void executarComandos(List<Comando> comandosBrutos, Runnable callbackFim) {
        List<Comando> codigoFinal = compilarCodigo(comandosBrutos);
        Timer timer = new Timer(400, null);
        var iterador = codigoFinal.iterator();
        
        timer.addActionListener(e -> {
            if (iterador.hasNext()) {
                processarComando(iterador.next());
                moverInimigos(); 
                verificarColisoes();
                
                if (mapa[roboY][roboX] == 2) {
                    timer.stop();
                    nivelAtual++;
                    gerarMapaProcedural(); 
                    callbackFim.run();
                }
                repaint();
            } else {
                timer.stop();
                callbackFim.run();
            }
        });
        timer.start();
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
    
    private void moverInimigos() {
        for (Inimigo in : inimigos) {
            if (!in.vivo) continue;
            int nx = in.x + (in.eixo == 0 ? in.dirPatrulha : 0);
            int ny = in.y + (in.eixo == 1 ? in.dirPatrulha : 0);
            if (nx < 0 || nx >= GRID_TAM || ny < 0 || ny >= GRID_TAM || mapa[ny][nx] == 1 || mapa[ny][nx] == 2) {
                in.dirPatrulha *= -1; 
            } else {
                in.x = nx; in.y = ny;
            }
        }
    }
    
    private void verificarColisoes() {
        for (Inimigo in : inimigos) if (in.vivo && in.x == roboX && in.y == roboY) resetarFase();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int offX = (getWidth() - (GRID_TAM * TAM_CELULA)) / 2;
        int offY = (getHeight() - (GRID_TAM * TAM_CELULA)) / 2;
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Nível: " + nivelAtual, 10, 25);
        
        for(int i=0; i<GRID_TAM; i++) {
            for(int j=0; j<GRID_TAM; j++) {
                int px = offX + j * TAM_CELULA, py = offY + i * TAM_CELULA;
                if(mapa[i][j] == 1) g.setColor(new Color(15, 15, 15)); 
                else if(mapa[i][j] == 2) g.setColor(new Color(0, 200, 0)); 
                else g.setColor(new Color(80, 80, 80)); 
                
                g.fillRect(px, py, TAM_CELULA, TAM_CELULA);
                g.setColor(Color.BLACK);
                g.drawRect(px, py, TAM_CELULA, TAM_CELULA);
            }
        }
        
        // DESENHA INIMIGOS "FANTASMA" (O Futuro deles)
        for (Inimigo f : inimigosFantasma) {
            if (f.vivo) {
                g.setColor(new Color(255, 100, 150, 150)); // Rosa choque transparente
                g.fillOval(offX + f.x * TAM_CELULA + 5, offY + f.y * TAM_CELULA + 5, 30, 30);
                g.setColor(Color.WHITE);
                g.drawString("?", offX + f.x * TAM_CELULA + 15, offY + f.y * TAM_CELULA + 25); // Marcação visual clara
            }
        }
        
        // FANTASMA ROBÔ (Rastro)
        if (caminhoFantasma.size() > 1) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(colidiuNaSimulacao ? new Color(255, 0, 0, 150) : new Color(0, 255, 255, 100)); 
            for(int i = 0; i < caminhoFantasma.size() - 1; i++) {
                Point p1 = caminhoFantasma.get(i);
                Point p2 = caminhoFantasma.get(i+1);
                g2.drawLine(offX + p1.x * TAM_CELULA + 20, offY + p1.y * TAM_CELULA + 20, 
                            offX + p2.x * TAM_CELULA + 20, offY + p2.y * TAM_CELULA + 20);
            }
            g.setColor(colidiuNaSimulacao ? new Color(255, 0, 0, 120) : new Color(0, 255, 255, 120));
            g.fillOval(offX + fantasmaX * TAM_CELULA + 10, offY + fantasmaY * TAM_CELULA + 10, 20, 20);
        }
        
        // INIMIGOS REAIS
        for (Inimigo in : inimigos) {
            if (in.vivo) {
                int px = offX + in.x * TAM_CELULA;
                int py = offY + in.y * TAM_CELULA;
                g.setColor(Color.RED);
                g.fillOval(px + 5, py + 5, 30, 30);
                
                g.setColor(Color.WHITE);
                if (in.eixo == 0) {
                    if (in.dirPatrulha == 1) g.fillPolygon(new int[]{px+25, px+30, px+25}, new int[]{py+15, py+20, py+25}, 3); 
                    else g.fillPolygon(new int[]{px+15, px+10, px+15}, new int[]{py+15, py+20, py+25}, 3); 
                } else {
                    if (in.dirPatrulha == 1) g.fillPolygon(new int[]{px+15, px+20, px+25}, new int[]{py+25, py+30, py+25}, 3); 
                    else g.fillPolygon(new int[]{px+15, px+20, px+25}, new int[]{py+15, py+10, py+15}, 3); 
                }
            }
        }
        
        // ROBÔ REAL
        int rx = offX + roboX * TAM_CELULA, ry = offY + roboY * TAM_CELULA;
        g.setColor(new Color(50, 100, 200));
        g.fillRoundRect(rx + 5, ry + 5, 30, 30, 10, 10);
        g.setColor(Color.BLACK);
        if (direcao == 0 || direcao == 2) {
            g.fillRect(rx + 2, ry + 5, 5, 30); g.fillRect(rx + 33, ry + 5, 5, 30);
        } else {
            g.fillRect(rx + 5, ry + 2, 30, 5); g.fillRect(rx + 5, ry + 33, 30, 5);
        }
        g.setColor(Color.YELLOW);
        if (direcao == 0) g.fillRect(rx + 17, ry, 6, 15);
        if (direcao == 1) g.fillRect(rx + 25, ry + 17, 15, 6);
        if (direcao == 2) g.fillRect(rx + 17, ry + 25, 6, 15);
        if (direcao == 3) g.fillRect(rx, ry + 17, 15, 6);
    }
}