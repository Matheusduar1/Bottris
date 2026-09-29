# Bottris
BotTris é um jogo tático e de sobrevivência desenvolvido em Java que funde a ação rápida e clássica do Tetris com a lógica estratégica de programação em blocos. O objetivo não é apenas evitar o acúmulo de peças, mas usar as linhas limpas para farmar comandos e programar um robô para escapar de labirintos procedurais cada vez mais difíceis.

🎮 Como Funciona o Jogo
O ciclo de gameplay é dividido em duas fases que se complementam:

Fase de Ação (Tetris): Você joga uma partida clássica de Tetris. Cada linha limpa é convertida em blocos de programação (estoque). A cada 5 linhas limpas, o jogo entra automaticamente no modo de programação.

Fase de Estratégia (Programação): Um temporizador de 60 segundos é ativado. Usando os blocos conquistados, você deve montar uma sequência de ações para guiar o robô até a saída verde do labirinto.

Execução: O robô e os inimigos agem por turnos com base no código que você montou. Se o robô chegar à saída, o nível avança. Se colidir com um inimigo, a tentativa é resetada. Se o Tetris der Game Over, você tem uma última chance de sobreviver com os blocos que restaram no estoque.

🧩 Os Blocos de Comando (Tetrominós)
Cada formato de peça do Tetris representa um comando lógico específico no estoque do robô:

🟩 Andar (I / Ciano): O robô avança 1 bloco na direção em que está virado. Custa 1 turno.

🟧 Direita (L / Laranja): O robô gira 90º para a direita no próprio eixo. Custa 1 turno.

🟦 Esquerda (J / Azul): O robô gira 90º para a esquerda no próprio eixo. Custa 1 turno.

🟨 Multiplicar (O / Amarelo): Não gasta turno por si só. Multiplica a próxima ação por 2 (Ex: Multi + Andar = Anda 2 espaços gastando menos peças).

🟥 Loop (Z / Vermelho): Repete toda a sequência que foi colocada antes dele. Excelente para criar padrões de movimento complexos com poucas peças.

🟩 Esperar (S / Verde): O robô fica parado por 1 turno. Essencial para manipular o tempo e deixar inimigos passarem direto.

🟪 Atacar (T / Magenta): Dispara à frente. Pode destruir paredes de terra ou eliminar inimigos adjacentes. Custa 1 turno.

✨ Principais Funcionalidades
Simulador "Fantasma": Enquanto você arrasta os blocos na fase de programação, o jogo desenha um rastro ciano no labirinto prevendo o caminho exato do robô e o futuro das patrulhas inimigas.

Física Avançada de Tetris: Implementação de Soft Drop, rotação matricial, Wall Kick (empurra a peça se bater na parede ao girar) e Lock Delay.

Geração Procedural: O mapa 8x8, a posição das paredes quebra-cabeças e o spawn dos inimigos são gerados aleatoriamente a cada novo nível.

Inteligência Inimiga: Inimigos possuem rotas de patrulha indicadas visualmente e só se movem a cada linha de código executada pelo jogador.

Interface Gráfica (Swing): Painel de próxima peça, pontuação com multiplicador de combo e miniaturas geométricas dos blocos interagíveis.

🛠️ Tecnologias Utilizadas
Linguagem: Java (Orientação a Objetos).

Interface e Gráficos: Java Swing (JFrame, JPanel, Graphics2D, Timer).

Arquitetura: Separação em painéis isolados comunicando-se através do Frame principal.

🚀 Como Executar o Projeto
Certifique-se de ter o Java Development Kit (JDK 8 ou superior) instalado na sua máquina.

Clone o repositório ou baixe os arquivos fonte.

Importe o projeto em sua IDE favorita (NetBeans, Eclipse ou IntelliJ).

Localize o arquivo JogoFrame.java (ou Main.java dependendo de como instanciou).

Compile e execute a classe principal para abrir o Menu do BotTris.

Controles no Tetris: Setinhas ou W, A, S, D para mover, girar e descer a peça. O mouse é utilizado no painel de programação.

Criado e Desenvolvido por: Matheus Duarte
