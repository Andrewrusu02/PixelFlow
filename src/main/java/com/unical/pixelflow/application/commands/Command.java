package com.unical.pixelflow.application.commands;

public interface Command {
    void execute(); // Esegui l'azione
    void undo();    // Annulla l'azione (torna allo stato precedente)
}

