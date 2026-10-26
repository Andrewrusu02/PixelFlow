# Contributing to PixelFlow

Grazie per il tuo interesse nel contribuire a PixelFlow. Bug report, proposte
di funzionalità, documentazione e miglioramenti al codice sono tutti
benvenuti.

## Prima di iniziare

- Cerca nelle [issue aperte](https://github.com/Andrewrusu02/PixelFlow/issues)
  per evitare duplicati.
- Per un bug, usa il template **Bug report**.
- Per una nuova funzionalità, usa il template **Feature request**.
- Per modifiche più ampie, apri prima una issue così da concordare l'approccio.

## Requisiti locali

- Git.
- JDK 25.
- Maven 3.9 o superiore, oppure il Maven Wrapper incluso nel repository.

Verifica l'ambiente con:

```bash
git --version
java --version
```

## Setup del progetto

1. Fai un fork del repository dal pulsante **Fork** su GitHub.
2. Clona il tuo fork:

   ```bash
   git clone https://github.com/<your-username>/PixelFlow.git
   cd PixelFlow
   ```

3. Configura il repository upstream:

   ```bash
   git remote add upstream https://github.com/Andrewrusu02/PixelFlow.git
   git remote -v
   ```

4. Crea un branch descrittivo partendo da `main`:

   ```bash
   git checkout main
   git pull upstream main
   git checkout -b feat/short-description
   ```

5. Compila il progetto e verifica che la baseline sia funzionante:

   Su Windows:

   ```powershell
   .\mvnw.cmd clean test
   ```

   Su macOS/Linux:

   ```bash
   ./mvnw clean test
   ```

## Linee guida per lo sviluppo

- Mantieni le modifiche focalizzate su una singola issue o funzionalità.
- Rispetta la struttura esistente tra controller, componenti, modelli,
  risorse FXML e fogli di stile.
- Non inserire password, token, database personali o altri dati sensibili
  nel repository.
- Aggiorna la documentazione quando modifichi il comportamento pubblico
  dell'applicazione.
- Aggiungi o aggiorna i test quando introduci o correggi logica applicativa.
- Verifica il funzionamento sia con le configurazioni e le risorse esistenti,
  sia con un database locale generato dai template del progetto.

## Convenzioni per i commit

Usa messaggi chiari, brevi e al tempo presente. È preferibile seguire una
struttura ispirata a Conventional Commits:

```text
<type>(<scope>): <short description>
```

Tipi comuni:

- `feat`: nuova funzionalità.
- `fix`: correzione di un bug.
- `refactor`: modifica interna senza variazione del comportamento.
- `test`: aggiunta o modifica dei test.
- `docs`: documentazione.
- `style`: formattazione o stile senza modifica della logica.
- `build`: dipendenze o sistema di build.
- `ci`: configurazione della CI.

Esempi:

```text
feat(editor): add node connection validation
fix(auth): handle invalid local credentials
docs: clarify local setup instructions
```

Evita commit generici come `updates`, `fixes` o `final version`. Mantieni ogni
commit atomico e non includere modifiche non correlate.

## Pull Request

Prima di aprire la Pull Request:

1. Aggiorna il branch con l'ultima versione di `main`:

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. Esegui i test:

   ```bash
   .\mvnw.cmd clean test
   ```

   Su macOS/Linux usa `./mvnw clean test`.

3. Avvia l'applicazione e verifica manualmente il comportamento modificato:

   ```bash
   .\mvnw.cmd javafx:run
   ```

4. Controlla le modifiche:

   ```bash
   git diff --check
   git status
   ```

5. Pubblica il branch:

   ```bash
   git push origin feat/short-description
   ```

6. Apri una Pull Request verso il branch `main`.

La descrizione della Pull Request deve:

- spiegare cosa cambia e perché;
- collegare la relativa issue, quando presente;
- indicare i test eseguiti;
- descrivere eventuali passaggi manuali necessari;
- includere screenshot o GIF per modifiche all'interfaccia;
- segnalare limitazioni o attività ancora da completare.

Usa un titolo descrittivo, ad esempio:

```text
feat(editor): add node parameter validation
```

Non includere file generati, configurazioni dell'IDE, credenziali o database
personali. Le Pull Request devono essere aggiornate in seguito ai commenti dei
reviewer e devono superare i controlli automatici prima del merge.

## Code review e comportamento della community

Le review devono concentrarsi sul codice e proporre feedback concreti e
rispettosi. Tutti i partecipanti sono tenuti a mantenere un ambiente
collaborativo, inclusivo e professionale.

Grazie per aver contribuito a PixelFlow!
