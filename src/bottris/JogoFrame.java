package bottris;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class JogoFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel painelPrincipal;
    
    private TetrisPanel tetrisPanel;
    private ProgramacaoPanel programacaoPanel;
    private LabirintoPanel labirintoPanel;
    private JPanel containerJogo;

    public JogoFrame() {
        setTitle("BotTris"); 
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);
        painelPrincipal.add(criarMenu(), "MENU");
        add(painelPrincipal);
    }

    private JPanel criarMenu() {
        JPanel menu = new JPanel(null);
        menu.setBackground(Color.DARK_GRAY);

        JLabel titulo = new JLabel("BOTTRIS"); 
        titulo.setFont(new Font("Monospaced", Font.BOLD, 60));
        titulo.setForeground(Color.CYAN);
        titulo.setBounds(400, 100, 400, 100);
        menu.add(titulo);

        JButton btnIniciar = new JButton("INICIAR JOGO");
        btnIniciar.setBounds(425, 300, 250, 60);
        btnIniciar.addActionListener(e -> iniciarNovoJogo());
        menu.add(btnIniciar);
        
        JButton btnSair = new JButton("SAIR");
        btnSair.setBounds(425, 380, 250, 60);
        btnSair.addActionListener(e -> System.exit(0));
        menu.add(btnSair);

        return menu;
    }

    public void iniciarNovoJogo() {
        if (containerJogo != null) painelPrincipal.remove(containerJogo);
        
        containerJogo = new JPanel(new GridLayout(1, 3));
        labirintoPanel = new LabirintoPanel();
        programacaoPanel = new ProgramacaoPanel(this);
        tetrisPanel = new TetrisPanel(this);
        
        containerJogo.add(tetrisPanel);
        containerJogo.add(programacaoPanel);
        containerJogo.add(labirintoPanel);
        
        painelPrincipal.add(containerJogo, "JOGO");
        cardLayout.show(painelPrincipal, "JOGO");
        
        tetrisPanel.requestFocusInWindow();
        tetrisPanel.iniciarJogo();
    }
    
    public void voltarParaMenu() {
        cardLayout.show(painelPrincipal, "MENU");
    }

    public void pausarTetrisELiberarProgramacao(boolean gameOver) {
        programacaoPanel.ativarPainel(gameOver);
    }

    // ALTERAÇÃO AQUI: Aciona a contagem regressiva em vez de retomar instantaneamente
    public void executarCodigoNoLabirinto(java.util.List<Comando> codigo, boolean gameOver) {
        labirintoPanel.executarComandos(codigo, () -> {
            if (!gameOver) {
                tetrisPanel.iniciarContagemRegressiva(); 
            } else {
                programacaoPanel.ativarPainel(true);
            }
        });
    }

    public ProgramacaoPanel getProgramacaoPanel() { return programacaoPanel; }
    public LabirintoPanel getLabirintoPanel() { return labirintoPanel; }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JogoFrame().setVisible(true));
    }
}