package com.unical.pixelflow.domain.model;

/**
 * Ruolo del nodo nella pipeline.
 *
 * INPUT e OUTPUT sono nodi strutturali (non effimeri):
 * devono esistere sempre per dare senso al flusso.
 */
public enum NodeRole {
    INPUT,
    FILTER,
    OUTPUT
}

