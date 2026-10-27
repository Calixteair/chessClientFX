# chessClientFX

Client JavaFX du jeu d'échecs. Client léger : il ne dépend que du protocole texte du serveur, qui arbitre les règles et pousse l'état de la partie. Il peut affronter un joueur du client web (https://chess.calixteair.fr).

## Jouer (sans rien installer)

Télécharger l'archive de son système dans les [Releases](https://github.com/Calixteair/chessClientFX/releases), l'extraire et lancer :

- **Linux** : `Echecs/bin/Echecs`
- **Windows** : `Echecs\Echecs.exe`
- **macOS** : `Echecs.app` (clic droit → « Ouvrir » au premier lancement, l'application n'est pas signée)

Java et JavaFX sont inclus. La version distribuée se connecte au serveur public `chessfx.calixteair.fr` en TLS.

## Développer

```bash
mvn javafx:run                                   # serveur local (localhost:8090, voir chessServer)
CHESS_HOST=chessfx.calixteair.fr CHESS_TCP_PORT=443 CHESS_TLS=true mvn javafx:run   # serveur public
packaging/package.sh 1.0.0                       # version autonome pour le système courant (target/release/)
```

Publier une version : pousser un tag `vX.Y.Z`. Le workflow `.github/workflows/release.yml` construit Linux, Windows, macOS Intel et Apple Silicon, puis crée la Release.

Écrans : choix du pseudo, salon (parties en attente mises à jour en direct, création), partie (coups légaux affichés, promotion, notation française, abandon).
