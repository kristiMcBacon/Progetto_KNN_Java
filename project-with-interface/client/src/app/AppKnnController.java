package app;

import java.io.IOException;
import java.util.Optional;
import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * classe che gestisce le azioni dei pulsanti dell'interfaccia appKnn e inizializza i sui oggetti.
 * @author Kristi Dashaj.
 * @author Giuseppe Grisolia.
 */

public class AppKnnController {

    /**
     * indirizzo del localhost per creare la connessione.
     */
    private String address="127.0.0.1";

    /**
     * porta per creare la connessione.
     */
    private int port=2025;

    /**
     * connessione 
     */
    private Client connection;

    /**
     * lista di opzioni selezionabili nella ComboBox.
     */
    private ObservableList<String> tipoCaricamento = FXCollections.observableArrayList("File (.dat)","File binario (.dmp)","database");

    @FXML
    private Button carica;

    @FXML
    private TextField input;

    @FXML
    private Button inputExample;

    @FXML
    private HBox supP;

    @FXML
    private ScrollPane stampa;

    @FXML
    private ComboBox<String> tipo;

    @FXML
    private TextArea trainingSet;

    @FXML
    private SubScene panello;

    @FXML
    
    /**
     * descrive evento del bottone carica.
     * @param event evento.
     */
    private void caricaTset(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR, "Il file inserito è inesistente!");
        Alert alertConn = new Alert(AlertType.ERROR, "Connessione al database assente!");
        Optional<ButtonType> result;

        String train="";
        String t="";

        panello.setOpacity(0);          //nasconde pannello caricamento esempio
        
        connection.invia(1);       //invia scelta azione (1=caricamento trainingset)

        t=tipo.getValue();

        //in base alla t seleziona il tipo di caricamento
        if(t.equals("File (.dat)")) {
            connection.invia(1);
        }else if(t.equals("File binario (.dmp)")) {
            connection.invia(2);
        }else if(t.equals("database")) {
            connection.invia(3);
        }else {
            connection.invia(4);
            inputExample.setDisable(true);
            return;
        }
        
        String file = input.getText();      //prende nome file (senza estensione)

        connection.invia(file);             //invia nome file
        
        train=(String) connection.ricevi(); //riceve trainingSet calcolato, se non esiste riceve "File inesistente"

        trainingSet.setText(train);         //imposta il TrainingSet sulla label 
        
        //se train è stato caricato attiva il bottone della predizione
        if(train.equals("") || train.equals("File inesistente")) {
            inputExample.setDisable(true);
            result = alert.showAndWait();
        }
        else if(train.equals("Connessione assente")) {
            inputExample.setDisable(true);
            result = alertConn.showAndWait();}
            else {inputExample.setDisable(false);}
    };

    @FXML
    /**
     * funzione che carica example da interfaccia.
     * @param event evento.
	 * @throws IOException quando avviene un I/O errore.
	 * @throws ClassNotFoundException se una classe serializzabile non puo essere caricata.
     */
    private void caricaExample(ActionEvent event) throws ClassNotFoundException, IOException {

        //invia scelta azione (2=caricamento Example)
        connection.invia(2);

        //riceve lunghezza example
        int l=(int) connection.ricevi();

        CreateExample sc= new CreateExample(l, connection);
        panello.setRoot(sc.getScena());
        panello.setOpacity(1);
    }

    @FXML
    private void initialize() {
        //crea il client
        try {
            connection=new Client(address,port);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }     

        assert carica != null : "fx:id=\"carica\" was not injected: check your FXML file 'interFinal.fxml'.";
        assert input != null : "fx:id=\"input\" was not injected: check your FXML file 'interFinal.fxml'.";
        assert stampa != null : "fx:id=\"stampa\" was not injected: check your FXML file 'interFinal.fxml'.";
        tipo.setValue("");
        tipo.setItems(tipoCaricamento);
        inputExample.setDisable(true);
        supP.setBorder(Border.stroke(Color.BLUE));
        tipo.setBorder(Border.stroke(Color.BLUE));
        input.setBorder(Border.stroke(Color.BLUE));
        carica.setBorder(Border.stroke(Color.BLUE));
    }
}