/**
 * Client JavaFX du jeu d'échecs. Module nommé pour pouvoir embarquer un runtime Java minimal
 * (jlink / jpackage) dans les versions téléchargeables.
 */
module com.chessclientfx {
    requires javafx.controls;
    requires javafx.fxml;
    // TLS vers le serveur public (certificat ECDSA, échange ECDHE) : sans ce module,
    // le runtime réduit par jlink échoue à la poignée de main.
    requires jdk.crypto.ec;

    exports com.chessclientfx;
    // FXML injecte les champs @FXML et appelle les méthodes des contrôleurs par réflexion.
    opens com.chessclientfx.controller to javafx.fxml;
}
