# 🐎 Jogo Herdeiros de Khan

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow?style=for-the-badge)

Repositório dedicado ao desenvolvimento do jogo de tabuleiro **Herdeiros do Kan**, projeto final desenvolvido para a disciplina **INF1636 – Programação Orientada a Objetos** da PUC-Rio.

## 📜 Sobre o Jogo
"Herdeiros do Kan" é um jogo estratégico de tabuleiro para 2 a 5 jogadores ambientado no Império Mongol. Com o envelhecimento de Gengis Khan, os jogadores assumem o papel de herdeiros competindo por influência e votos no conselho (*Kurultai*) para se tornarem o próximo grande líder.

## 💻 Arquitetura e Tecnologias
O projeto foi modelado para aplicar os conceitos fundamentais de Orientação a Objetos, boas práticas de engenharia de software e padrões de projeto (Design Patterns).

- **Linguagem:** Java
- **Padrões de Projeto Aplicados:**
  - **Singleton:** `GerenciadorPartida` centraliza o acesso à partida ativa.
  - **Facade:** `InterfacePartida` oferece à interface gráfica operações e consultas sobre a partida.
  - **Observer:** as telas interessadas registram-se na partida e são notificadas quando seu estado muda.

## 👨‍💻 Autores
**Maria Eduarda de Menezes Queiroz**

**Matheus da Silva Oliveira**

**Saulo Canto**

## 💾 Saves de partidas

Os saves ficam na pasta `saves/` e usam JSON legível, com metadados versionados,
estado da partida, mapa, jogadores, tabuleiro, baralhos e províncias. É possível
duplicar e editar um arquivo para preparar cenários de teste; o carregamento
valida os campos e as referências entre eles.

O projeto Eclipse inclui Gson em `lib/gson-2.11.0.jar`, configurado no `.classpath`;
a licença Apache 2.0 está em `lib/LICENSE-GSON.txt`. Saves anteriores no formato
binário/Base64 continuam podendo ser carregados.
