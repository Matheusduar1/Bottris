# 🤖 BotTris — Action Puzzle & Logic Programming Game

![Java](https://img.shields.io/badge/Java-JDK%2011%2B%20%7C%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing/AWT](https://img.shields.io/badge/GUI-Java%20Swing%20%26%20AWT-007396?style=for-the-badge)
![Genre](https://img.shields.io/badge/Genre-Puzzle%20%2B%20Tactical%20Coding-8A2BE2?style=for-the-badge)

**BotTris** é um jogo híbrido tático e de sobrevivência desenvolvido em **Java puro (Swing/AWT)** que funde a velocidade clássica do **Tetris** com a lógica estratégica de **programação em blocos**. O objetivo não é apenas evitar o empilhamento das peças, mas utilizar as linhas eliminadas para acumular comandos e programar um robô autônomo para escapar de labirintos procedurais repletos de patrulhas inimigas.

---

## 🎮 Ciclo de Gameplay

A partida é dividida em três telas simultâneas que interagem em tempo real:

1. **⚡ Fase de Ação (Tetris):**
   * Jogue uma partida dinâmica de Tetris com visualização da próxima peça e *Ghost Piece* (sombra de queda).
   * Cada linha limpa converte-se automaticamente em blocos de comando no estoque do robô (combos de 2 a 4 linhas concedem blocos especiais de *Multiplicador* e *Loop*).
   * A cada **5 linhas limpas**, o Tetris pausa e libera o terminal de programação.
2. **🧠 Fase de Estratégia (Programação em Blocos):**
   * Um cronômetro de **60 segundos** é iniciado.
   * Utilizando os blocos conquistados, monte um algoritmo sequencial para guiar o robô até a saída verde `(7,7)` do labirinto.
   * Enquanto você adiciona ou remove blocos, o **Simulador Fantasma** projeta em tempo real na tela do labirinto o caminho exato que o robô percorrerá, quais paredes serão quebradas e onde cada inimigo estará no futuro!
3. **🤖 Fase de Execução (Labirinto Tático):**
   * O robô e os inimigos agem simultaneamente passo a passo (`400ms` por instrução).
   * Se o robô alcançar a saída, o nível avança e um novo labirinto mais desafiador é gerado.
   * Se colidir com um inimigo (ou cruzar de frente com ele), a execução é interrompida e o nível atual é reiniciado.
   * **Modo Última Chance:** Caso o Tetris chegue ao *Game Over*, o cronômetro torna-se infinito para que você tente avançar o máximo possível utilizando apenas o estoque restante de comandos.

---

## 🧩 Blocos de Comando (Tetrominós)

Cada formato clássico de peça do Tetris representa uma instrução específica no compilador do robô:

| Tetrominó | Cor | Comando | Custo de Turno | Descrição e Efeito |
| :---: | :---: | :--- | :---: | :--- |
| **I** | 🟦 Ciano | `Andar` | 1 Turno | Avança 1 célula na direção em que o canhão do robô está apontando. |
| **L** | 🟧 Laranja | `Dir` | 1 Turno | Gira o robô `90°` no sentido horário no próprio eixo. |
| **J** | 🟦 Azul | `Esq` | 1 Turno | Gira o robô `90°` no sentido anti-horário no próprio eixo. |
| **O** | 🟨 Amarelo | `Multi` | 0 Turnos | Compilador: Duplica a execução do próximo comando da fila *(ganho ao limpar 2+ linhas)*. |
| **Z** | 🟥 Vermelho | `Loop` | 0 Turnos | Compilador: Repete toda a sequência de comandos programada antes dele *(ganho no Tetris de 4 linhas)*. |
| **S** | 🟩 Verde | `Espera` | 1 Turno | O robô permanece imóvel por 1 turno (essencial para sincronizar a passagem de patrulhas). |
| **T** | 🟪 Magenta | `Atacar` | 1 Turno | Dispara à frente, destruindo paredes bloqueadoras ou eliminando inimigos adjacentes. |

---

## 🕹️ Controles

### Painel de Tetris (Teclado)
| Tecla | Ação |
| :---: | :--- |
| `⬅️` / `A` | Mover peça para a esquerda |
| `➡️` / `D` | Mover peça para a direita |
| `⬆️` / `W` | Girar peça (`90°` com *Wall Kick* automático) |
| `⬇️` / `S` | *Soft Drop* (desce mais rápido e soma `+1 ponto` por célula) |
| `Espaço` | *Hard Drop* (queda instantânea na sombra com bônus de `+2 pontos` por célula) |

### Painel de Programação (Mouse)
* **Clique nos blocos em "Seus Blocos":** Insere o comando na fila de execução.
* **Clique em um item da "Sequência de Execução":** Remove aquele comando específico e devolve ao estoque.
* **Botão `LIMPAR TUDO`:** Devolve toda a sequência atual para o estoque de uma só vez.
* **Botão `EXECUTAR >>`:** Compila e executa imediatamente o algoritmo no labirinto.

---

## ✨ Destaques Técnicos

* **🔮 Simulador Preditivo ("Fantasma"):** Ao montar o código, o motor simula toda a execução em memória (incluindo expansão de `Multi`/`Loop`, destruição de paredes com `Atacar` e movimentação futura das patrulhas inimigas), desenhando o rastro em **ciano** (caminho seguro) ou **vermelho** (alerta de colisão).
* **🗺️ Geração Procedural Validada (BFS):** O labirinto `8x8` gera barreiras e patrulhas aleatórias a cada nível, utilizando um algoritmo de **Busca em Largura (BFS)** para garantir que todas as fases geradas tenham solução válida.
* **🧱 Física Clássica de Tetris:** Implementação de *Ghost Piece* translúcida, *Hard Drop*, *Soft Drop*, *Lock Delay* (tempo de ajuste antes de fixar a peça) e *Wall Kick* (ajuste lateral ao girar próximo às bordas).

---

## 🏗️ Estrutura do Projeto

```text
Bottris/
└── src/
    └── bottris/
        ├── Main.java               # Ponto de entrada da aplicação (SwingUtilities)
        ├── JogoFrame.java          # Janela principal (CardLayout alternando Menu e Jogo)
        ├── Comando.java            # Enum dos 7 comandos lógicos, cores e matrizes de formato
        ├── TetrisPanel.java        # Motor do Tetris, controles de teclado, Ghost Piece e recompensas
        ├── ProgramacaoPanel.java   # IDE visual em blocos, ícones customizados, timer e fila de código
        └── LabirintoPanel.java     # Grid 8x8 procedural (BFS), IA de patrulha, compilador e Simulador Fantasma
```

---

## 🚀 Como Executar

1. Certifique-se de possuir o **Java JDK 11+** instalado.
2. Abra o projeto no **Apache NetBeans** (ou outra IDE Java como IntelliJ IDEA / Eclipse), ou compile via terminal:
   ```bash
   javac -d build/classes src/bottris/*.java
   java -cp build/classes bottris.Main
   ```
3. Execute a classe principal [`bottris.Main`](src/bottris/Main.java).

---

## 👤 Autor

Desenvolvido por **Matheus Duarte**.
