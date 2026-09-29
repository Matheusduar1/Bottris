package bottris;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class ProgramacaoPanel extends JPanel {
    private Map<Comando, Integer> estoque = new HashMap<>();
    private List<Comando> sequenciaAtual = new ArrayList<>();
    private JogoFrame parent;
    private JButton btnExecutar;
    private JPanel painelEstoque, painelSequencia;
    private JLabel lblTimer;
    private Timer timerProgramacao;
    private int tempoRestante = 60;
    private boolean isGameOverMode = false;
    
    public ProgramacaoPanel(JogoFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        
        for (Comando cmd : Comando.values()) estoque.put(cmd, 2); 
        
        // PAINEL SUPERIOR (Estoque + Timer)
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBackground(new Color(30, 30, 30));
        
        painelEstoque = new JPanel(new GridLayout(4, 2, 5, 5));
        painelEstoque.setBackground(new Color(30, 30, 30));
        painelEstoque.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "Seus Blocos", 0, 0, null, Color.LIGHT_GRAY));
        
        lblTimer = new JLabel("Tempo: 60s", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 20));
        lblTimer.setForeground(Color.RED);
        lblTimer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        painelTopo.add(painelEstoque, BorderLayout.CENTER);
        painelTopo.add(lblTimer, BorderLayout.SOUTH); 
        add(painelTopo, BorderLayout.NORTH);
        
        // ÁREA DE SEQUÊNCIA
        painelSequencia = new JPanel();
        painelSequencia.setLayout(new BoxLayout(painelSequencia, BoxLayout.Y_AXIS));
        painelSequencia.setBackground(new Color(20, 20, 20)); 
        
        JScrollPane scroll = new JScrollPane(painelSequencia);
        
        // CORREÇÃO DA IMAGEM: Escurecendo o fundo do ScrollPane e alterando a cor da linha da borda para não ficar branco ofuscante.
        scroll.setBackground(new Color(30, 30, 30)); 
        scroll.getViewport().setBackground(new Color(20, 20, 20)); 
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "Sequência de Execução (Clique para Cancelar)", 0, 0, null, Color.LIGHT_GRAY));
        
        add(scroll, BorderLayout.CENTER);
        
        // BOTÃO EXECUTAR
        btnExecutar = new JButton("EXECUTAR >>");
        btnExecutar.setEnabled(false);
        btnExecutar.setBackground(new Color(50, 150, 50));
        btnExecutar.setForeground(Color.WHITE);
        btnExecutar.addActionListener(e -> forcarExecucao());
        add(btnExecutar, BorderLayout.SOUTH);
        
        // TIMER LOGIC
        timerProgramacao = new Timer(1000, e -> {
            tempoRestante--;
            lblTimer.setText("Tempo: " + tempoRestante + "s");
            if(tempoRestante <= 0) {
                forcarExecucao();
            }
        });
        
        atualizarUI();
    }
    
    private void forcarExecucao() {
        if(timerProgramacao.isRunning()) timerProgramacao.stop();
        lblTimer.setText("Executando...");
        parent.executarCodigoNoLabirinto(new ArrayList<>(sequenciaAtual), isGameOverMode);
        sequenciaAtual.clear();
        btnExecutar.setEnabled(false);
        parent.getLabirintoPanel().limparFantasma();
        atualizarUI();
    }
    
    public void adicionarBloco(Comando cmd, int qtd) {
        estoque.put(cmd, estoque.get(cmd) + qtd);
        atualizarUI();
    }
    
    public void ativarPainel(boolean gameOver) {
        this.isGameOverMode = gameOver;
        tempoRestante = gameOver ? 999 : 60; 
        lblTimer.setText(gameOver ? "Tempo: Infinito" : "Tempo: 60s");
        btnExecutar.setEnabled(true);
        if(!gameOver) timerProgramacao.start();
        atualizarUI();
    }
    
    private void atualizarUI() {
        painelEstoque.removeAll();
        for (Comando cmd : Comando.values()) {
            JButton btn = new JButton(cmd.nome + " (x" + estoque.get(cmd) + ")");
            btn.setIcon(new BlocoIcon(cmd)); 
            btn.setBackground(Color.DARK_GRAY);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false); // Remove a bordinha azul feia ao clicar
            btn.addActionListener(e -> {
                if (estoque.get(cmd) > 0 && btnExecutar.isEnabled()) {
                    estoque.put(cmd, estoque.get(cmd) - 1);
                    sequenciaAtual.add(cmd);
                    atualizarUI();
                    parent.getLabirintoPanel().simularCaminho(sequenciaAtual);
                }
            });
            painelEstoque.add(btn);
        }
        
        painelSequencia.removeAll();
        for (int i = 0; i < sequenciaAtual.size(); i++) {
            Comando c = sequenciaAtual.get(i);
            int index = i; 
            JButton btnCancel = new JButton((i+1) + ". " + c.nome, new BlocoIcon(c));
            btnCancel.setBackground(new Color(60, 40, 40));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFocusPainted(false);
            btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCancel.setToolTipText("Clique para remover");
            
            btnCancel.addActionListener(e -> {
                if (btnExecutar.isEnabled()) {
                    sequenciaAtual.remove(index);
                    estoque.put(c, estoque.get(c) + 1);
                    atualizarUI();
                    parent.getLabirintoPanel().simularCaminho(sequenciaAtual); 
                }
            });
            
            painelSequencia.add(btnCancel);
            painelSequencia.add(Box.createRigidArea(new Dimension(0, 2)));
        }
        
        revalidate();
        repaint();
    }
    
    class BlocoIcon implements Icon {
        private Comando cmd;
        private final int size = 8; 
        public BlocoIcon(Comando cmd) { this.cmd = cmd; }
        @Override public int getIconWidth() { return cmd.shape[0].length * size; }
        @Override public int getIconHeight() { return cmd.shape.length * size; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            for(int i=0; i < cmd.shape.length; i++) {
                for(int j=0; j < cmd.shape[i].length; j++) {
                    if(cmd.shape[i][j] != 0) {
                        // CORREÇÃO DOS ÍCONES PRETOS: Restaurando a cor original a cada repetição.
                        g.setColor(cmd.cor); 
                        g.fillRect(x + j*size, y + i*size, size, size);
                        g.setColor(Color.BLACK);
                        g.drawRect(x + j*size, y + i*size, size, size);
                    }
                }
            }
        }
    }
}