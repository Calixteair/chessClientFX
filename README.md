# chessClientFX

Client JavaFX du jeu d'échecs. Client léger : il ne dépend que du protocole texte du serveur, qui arbitre les règles et pousse l'état de la partie. Il peut affronter un joueur du client web (chessWeb).

```bash
# Serveur lancé au préalable (voir chessServer)
mvn javafx:run
# Autre serveur : CHESS_HOST=192.168.1.10 CHESS_TCP_PORT=8090 mvn javafx:run
```

Écrans : choix du pseudo, salon (parties en attente mises à jour en direct, création), partie (coups légaux affichés, promotion, notation française, abandon).
