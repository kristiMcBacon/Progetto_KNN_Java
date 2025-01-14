package app;

import client.Client;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * classe che crea l'interfaccia per il calcolo delpredict in base all'esampio e la k inseriti.
 * @author Kristi Dashaj.
 * @author Giuseppe Grisolia.
 */
public class CreateExample extends Application{

    @FXML
    private Parent scena;

    @FXML
    private Client connection;

    @FXML
    private Label stampa[];
    
    @FXML
    private TextField input[];

    @FXML
    private Button calcola = new Button("predict");
    
    @FXML
    private Label label_k = new Label("inserisci K: ");       
    
    @FXML
    private TextField input_k = new TextField(); 
    
    @FXML
    private Label labPredizione = new Label("0.0"); 

    /**
     * dimensione esempio.
     */
    private int sizeExample;

    /**
     * costruttore di classe.
     * @param l dimensione esempio.
     * @param c connessione al client.
     */
    CreateExample(int l, Client c) {
        Font font1= new Font("Segoe UI Black", 12);

        connection=c;
        sizeExample=l;

        //crea l Label e TextField (in base alla lunghezza dell'Example)
        stampa = new Label[l]; 
        input = new TextField[l];

        String type="";
        //inizializza le Label e i TextField in base all'Example
        for(int i=0; i<l; i++) {
            type=(String) c.ricevi();
            stampa[i] = new Label(type);
            stampa[i].setPrefSize(150, 30);
            stampa[i].setFont(font1);
            input[i] = new TextField();
            input[i].setPrefSize(150, 30);
            input[i].setFont(font1);
        }
 
        //definisce il desig(font, dimensione e colore) dei vari oggetti dell'interfaccia
        input_k.setFont(font1);
        input_k.setPrefSize(150, 30);
        label_k.setPrefSize(150, 30);
        label_k.setFont(font1);

        calcola.setPrefSize(150, 30);
        calcola.setTextFill(Color.GREY);
        calcola.setFont(font1);
        calcola.setBorder(Border.stroke(Color.GREY));

        labPredizione.setPrefSize(150, 30);
        labPredizione.setFont(font1);
        labPredizione.setTextFill(Color.GREY);
        labPredizione.setAlignment(Pos.BOTTOM_CENTER);
        labPredizione.setBorder(Border.EMPTY);
        labPredizione.setBorder(Border.stroke(Color.GREY));

        //Associa calcolo predict al pulsante "calcola"
        calcola.setOnAction( e->calcolaPredict(e));

        //organizza i vari oggetti dell'interfaccia
        GridPane Gpane=new GridPane();
        int i;
        for(i=0; i<l; i++) {
            Gpane.add(stampa[i], 0, i);
            Gpane.add(input[i], 1, i);
        }
        
        Gpane.add(label_k, 0, i);
        Gpane.add(input_k, 1, i);

        HBox inf= new HBox(calcola, labPredizione);
        inf.setBorder(Border.stroke(Color.GREY));
        VBox root=new VBox(15, Gpane, inf);
        scena=root;
    }

    @Override
    public void start(Stage arg0) throws Exception {
    }
    
    /**
     * restituisce scena.
     * @return scena interfaccia.
     */
    Parent getScena() {
        return scena;
    }

    /**
     * Azione che calcola in predict.
     * @param e evento.
     */
    private void calcolaPredict(ActionEvent e) {
        String predizione="";
        Boolean correct=false;

        //invia decisione switch per calcolo predict
        connection.invia(3);
        //Invia valori caselle input
        for(int i=0; i<sizeExample; i++) {
            connection.invia(input[i].getText());    
        }
        //Riceve boolean per verifica example
        correct=(Boolean) connection.ricevi();
        if(!correct){
            predizione=(String) connection.ricevi();
            for(int i=0; i<sizeExample; i++) {
                input[i].setBorder(Border.stroke(Color.RED));
            }
            labPredizione.setText("ERROR");     //mostra ERROR nella predizione
            return;
        }

        //imposta bordo caselle input Example
        for(int i=0; i<sizeExample; i++) {
            input[i].setBorder(Border.stroke(Color.GREEN));
        }

        //Invia valore K
        connection.invia(input_k.getText());
        //Riceve boolean per verifica k
        correct=(Boolean) connection.ricevi();
        if(!correct){
            predizione=(String) connection.ricevi();
            input_k.setBorder(Border.stroke(Color.RED));    //imposta il bordo della k in rosso
            labPredizione.setText("ERROR");                 //mostra ERROR nella predizione
        }
        else{
            input_k.setBorder(Border.stroke(Color.GREEN));
            //ricevo predizione
            predizione=(String) connection.ricevi();
            //imposta predict in etichetta
            labPredizione.setText(predizione);
        }
        
    }
}