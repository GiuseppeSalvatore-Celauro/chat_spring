# Chat Backend (Spring Boot)

Backend REST per un sistema di messaggistica real-time sviluppato con Spring Boot.
Il progetto simula le funzionalità base di un'app di chat (tipo WhatsApp/Telegram lato server).

---

## Features

## User Management
  - Creazione utente
  - Login / Logout
  - Stato online/offline
  - Last seen timestamp
  - Controllo accesso basato su stato utente

## Messaging System
  - Invio messaggi tra utenti
  - Conversazioni bidirezionali
  - Lista conversazioni (ultima chat per utente)
  - Ordinamento per timestamp
  - Sistema read/unread

## Advanced Features
  - Conteggio messaggi per utente
  - Messaggi non letti per conversazione
  - Mark as read
  - Filtri avanzati:
    - per username
    - per testo
    - combinati

## Search & Filtering
  - Ricerca messaggi per:
    - username
    - contenuto testuale (case insensitive)
  - Limite risultati con paginazione
  - Ordinamento per data (desc)
---

## Business Logic Highlights
  - Controllo utente online prima di:
    - inviare messaggi
    - leggere messaggi
  - Aggregazione conversazioni via Map
  - Ottimizzazione query con JPA custom
  - Gestione errori custom:
    - NotFoundException
    - UserOfflineException
    - UserOnlineException
    
---

## Testing

  - Unit Test:
    - Mockito
    - Copertura service layer
    - Edge cases gestiti
  - Integration Test:
    - SpringBootTest
    - DB reale (H2 / configurabile)
    - Test flussi completi
  - Controller Test:
    - MockMvc
    - Validazione endpoint REST
    - Test HTTP status e JSON response
     
---

## Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- Hibernate
- JUnit 5
- Mockito
- MockMvc

---

##  API Endpoints (principali)
- User:
  - POST /api/chat/user → create user
  - PUT /api/chat/users/login → login
  - PUT /api/chat/users/logout → logout
  - GET /api/chat/users/{username}/status → stato utente
- Messages:
  - POST /api/chat/messages → invio messaggio
  - GET /api/chat/messages → lista messaggi
  - GET /api/chat/messages/search → ricerca
  - GET /api/chat/messages/count → conteggio
  - GET /api/chat/messages/conversation → chat tra 2 utenti
  - GET /api/chat/messages/conversations/{username} → lista conversazioni
  - POST /api/chat/messages/read → leggere un messaggio
  - GET /api/chat/messages/unread/{username} → conteggio messaggi non letti

---

## Stato del progetto

Work in progress — il progetto è in continua evoluzione.

---

## Progetti correlati

Prima versione (Java + socket):
https://github.com/GiuseppeSalvatore-Celauro/chatroom_java

---

##Avvio del progetto

./mvn spring-boot:run

---

## Note

Questo progetto è stato sviluppato come esercizio avanzato backend per:
imparare Spring Boot
progettare API REST reali
scrivere test professionali

---
