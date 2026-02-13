# BW-Divisions

Sistema di progressione per BedWars che funziona sia su BedWars1058 (server arena) che su BedWarsProxy (server lobby).

## Caratteristiche

- **Compatibilità Dual**: Funziona sia su server arena (BedWars1058) che su server lobby (BedWarsProxy)
- **6 Gradi di Progressione**: Sistema di divisioni basato sui livelli del giocatore
- **PlaceholderAPI Support**: Placeholders disponibili per integrazione con altri plugin
- **Interfaccia Pulita**: Prefissi ottimizzati per il TAB

## Gradi Disponibili

1. **Recluta** - Livello 1-14 (Colore: §8)
2. **Soldato** - Livello 15-29 (Colore: §f)
3. **Veterano** - Livello 30-49 (Colore: §a)
4. **Elite** - Livello 50-74 (Colore: §3)
5. **Maestro** - Livello 75-99 (Colore: §d)
6. **Supremo** - Livello 100+ (Colore: §e)

## Placeholders

- `%bwdiv_prefix%` - Prefisso della divisione (formato: §7[§(ColoreRank)(Livello)§7])
- `%bwdiv_name%` - Nome della divisione (es. "Recluta", "Soldato", ecc.)

## Requisiti

- Java 8+
- Spigot/Paper 1.8.8+
- BedWars1058 o BedWarsProxy
- PlaceholderAPI (opzionale ma consigliato)

## Installazione

1. Compila il progetto con Maven: `mvn clean package`
2. Copia il JAR generato nella cartella `plugins` del tuo server
3. Riavvia il server
4. Il plugin si inizializzerà automaticamente dopo 40 tick per permettere il caricamento delle API

## Compilazione

```bash
mvn clean package
```

Il JAR sarà generato in `target/bwdivisions-1.0.0.jar`

## Note Tecniche

- Il plugin utilizza un delay di 40 tick all'avvio per garantire che le API di BedWars siano completamente caricate
- Il sistema rileva automaticamente quale API è disponibile (Proxy o Arena)
- I nuovi giocatori iniziano automaticamente a Livello 1 come da impostazioni server

## Autore

GodNev3r
