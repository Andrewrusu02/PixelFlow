# PixelFlow

<p align="center">
  <img src="src/main/resources/com/unical/pixelflow/assets/logo/logocompletoPixelFlow.svg" alt="PixelFlow logo" width="420">
</p>

<p align="center">
  Visual node-based image editing for organizing creative projects.
</p>

<p align="center">
  <a href="https://github.com/Andrewrusu02/PixelFlow/actions/workflows/main.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Andrewrusu02/PixelFlow/main.yml?branch=main&label=CI" alt="CI status">
  </a>
  <img src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/JavaFX-23-4285F4" alt="JavaFX 23">
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/SQLite-3-003B57?logo=sqlite&logoColor=white" alt="SQLite">
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-green.svg" alt="MIT License">
  </a>
</p>

> **Stato del progetto:** in sviluppo  
> PixelFlow è un progetto universitario in evoluzione. Feedback, issue e contributi
> sono benvenuti.

## Panoramica

PixelFlow è un'applicazione desktop Java per costruire e gestire progetti di
elaborazione delle immagini tramite un'interfaccia visuale a nodi. L'applicazione
permette di organizzare i progetti, collegare operazioni in un workflow e
personalizzare l'esperienza con supporto ai temi e alla localizzazione.

## Demo

<!-- Sostituire il placeholder con uno screenshot o una GIF ottimizzata. -->
![Anteprima di PixelFlow](docs/assets/pixelflow-preview.gif)

<!--
Se il file non è ancora disponibile, usare temporaneamente:
![Screenshot di PixelFlow](https://placehold.co/1200x675?text=PixelFlow+preview)
-->

## Funzionalità principali

- Editor visuale basato su nodi e connessioni.
- Creazione, modifica, ricerca e organizzazione dei progetti.
- Cronologia delle operazioni con supporto undo/redo.
- Autenticazione e gestione del profilo locale.
- Temi chiaro e scuro.
- Interfaccia localizzata in italiano e inglese.
- Persistenza locale tramite SQLite.

## Architettura

Il progetto segue una separazione modulare tra interfaccia, logica applicativa e
persistenza:

```text
src/
├── main/java/com/unical/pixelflow/
│   ├── Main.java                 # Entry point JavaFX
│   ├── application/             # Stato applicativo e comandi undo/redo
│   ├── components/              # Componenti JavaFX riutilizzabili
│   ├── controller/              # Controller delle viste e dei workflow
│   ├── model/                   # Modelli di dominio
│   └── utils/                   # Utility condivise
└── main/resources/com/unical/pixelflow/
    ├── fxml/                    # Layout JavaFX
    ├── components/              # Layout dei componenti
    ├── css/                    # Temi e stili
    ├── lang/                   # File di localizzazione
    └── databases/              # Template del database SQLite
```

L'interfaccia è costruita con JavaFX e FXML. Maven gestisce il build e le
dipendenze; SQLite fornisce la persistenza locale senza richiedere un server
esterno.

## Requisiti

- Java Development Kit (JDK) 25.
- Maven 3.9 o superiore, oppure Maven Wrapper incluso (`mvnw`/`mvnw.cmd`).
- Git.

## Avvio locale

### 1. Clona il repository

```bash
git clone https://github.com/Andrewrusu02/PixelFlow.git
cd PixelFlow
```

### 2. Verifica Java

```bash
java --version
```

L'output deve indicare Java 25. Se il sistema usa più versioni di Java, configura
`JAVA_HOME` in modo che punti al JDK corretto.

### 3. Compila il progetto

Su Windows:

```powershell
.\mvnw.cmd clean compile
```

Su macOS/Linux:

```bash
./mvnw clean compile
```

### 4. Esegui i test

Su Windows:

```powershell
.\mvnw.cmd test
```

Su macOS/Linux:

```bash
./mvnw test
```

### 5. Avvia PixelFlow

Su Windows:

```powershell
.\mvnw.cmd javafx:run
```

Su macOS/Linux:

```bash
./mvnw javafx:run
```

Al primo avvio, l'applicazione utilizza i template SQLite inclusi in
`src/main/resources`.

## Contribuire

Prima di aprire una issue o una Pull Request, consulta
[CONTRIBUTING.md](CONTRIBUTING.md). Per segnalazioni di bug e proposte di
funzionalità sono disponibili i template GitHub nella directory
`.github/ISSUE_TEMPLATE/`.

## Licenza

Distribuito con licenza MIT. Consulta [LICENSE](LICENSE) per il testo completo.

## Contatti

Apri una [issue](https://github.com/Andrewrusu02/PixelFlow/issues) per domande,
bug o proposte.
