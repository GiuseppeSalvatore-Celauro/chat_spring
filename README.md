# Chat Backend (Spring Boot)

Backend di un sistema di messaggistica sviluppato con Spring Boot.

Questo progetto rappresenta l’evoluzione di una prima versione realizzata in Java puro con socket, con l’obiettivo di costruire un’architettura più strutturata e vicina a casi reali.

---

## Features

- Gestione messaggi tra utenti
- Conversation preview (ultimo messaggio per conversazione)
- API REST per interazione con il sistema
- Struttura a livelli (Controller, Service, Repository)
- Integration tests
  
---

## Conversation Preview

Una delle funzionalità principali è la "conversation preview", simile a quella delle app di messaggistica.

Per ogni utente vengono restituite le conversazioni con:
- ultimo messaggio
- timestamp
- utente con cui ha interagito

Esempio:

```json
{
  "lastMessage": "ciao",
  "timestamp": 1776437160136,
  "withUser": "salvatore"
}
```
---

## Tech Stack

- Java
- Spring Boot
- Spring Web
- JUnit (testing)

---

## Architettura

Il progetto è organizzato in:

- Controller → gestione delle richieste HTTP
- Service → logica di business
- Repository → accesso ai dati
- DTO (Request/Response) → scambio dati

---

## Stato del progetto

Work in progress — il progetto è in continua evoluzione.

---

## Progetti correlati

Prima versione (Java + socket):

---

##Avvio del progetto

./mvn spring-boot:run

---

## Obiettivo

L’obiettivo del progetto è migliorare le competenze backend, passando da una gestione low-level (socket) a un’architettura più strutturata con Spring Boot.

---
