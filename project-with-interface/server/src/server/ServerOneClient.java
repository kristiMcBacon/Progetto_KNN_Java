package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.SQLException;

import data.Data;
import data.TrainingDataException;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.InsufficientColumnNumberException;
import mining.KNN;

/**
 * definisce il thread che andrà a gestire separatamente le singole connessioni.
 * @author Kristi Dashaj.
 * @author Giuseppe Grisolia.
 */
public class ServerOneClient extends Thread {
    /**
	 * terminale per connessione tra macchine.
	 */
    private Socket socket;

	/**
	 * stream di oggetti in entrata dal quale leggere.
	 */
    private ObjectInputStream in;

	/**
	 * stream di oggetti in uscita sul quale scrivere.
	 */
    private ObjectOutputStream out;

    /**
	 * costruttore di classe, inizializza il socket, in e out e avvia il thread.
	 * @param s soket da inizializzare.
	 * @throws IOException quando avviene un I/O errore.
	 */
    public ServerOneClient(Socket s) throws IOException{
        socket = s;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
		start();
    }

    /**
     * riscrive il metodo run della superclasse thread al fine di gestire le richieste del client,
	 * in modo da rispondere alle richieste del client.
     */
    public void run() {
        try {
            KNN knn=null;
            while(true) {
            
                //DECISIONE
                int d;
                d=(int)(in.readObject());
                switch(d) 
                {	
                //CASO 1: CARICAMENTO TRAINING SET, UN ALTRO SWITCH AL SUO INTERNO
                case 1:{
                    //1-2-3 in base al tipo, 4 quando non è selezionato
                    int t;
                    t=(int)(in.readObject());
                    if(t==4) break;
                    knn=caricaTrainingSet(t);
                }
                break;

                //CASO 2: CREAZIONE EXAMPLE
                case 2:{
                    int l= knn.getSizeExample();
                    out.writeObject(l);
                    knn.getData().setLabel(out);
                }
                break;
                
                //CASO 3: CALCOLO PREDICT
                case 3:{
                    String p="";
                    
                    //ARRIVA VALORE PREDIZIONE
                    p=knn.predictJFX(out, in);
                    //INVIA p CHE PUO ESSERE: 1.il valore, 2."ERRORE EXAMPLE", 3."ERRORE K" 
                    out.writeObject(p);
                }
                break;
                }
				
			if(d==4)break;
            }
            System.out.println("closing...");
        } catch(IOException | ClassNotFoundException e) {
            System.err.println("IO Exception");
        } finally {
            try {
                socket.close();
            } catch(IOException e) {
                System.err.println("Socket non chiuso");
            }
        }
    }

    /**
     * metodo che restituisce il trainingSet.
     * @param t valore che indica da dove caricare il trainingSet, 1-2-3 in base al tipo, 4 se non è selezionato.
     * @return il trainingSet.
     */
    private KNN caricaTrainingSet(int t) throws FileNotFoundException {
		KNN knn=null;
		String tableName="";

        switch(t) {
            //CASO 1(file)
        case 1:{
                Data trainingSet=null;
                String file="";
                //riceve nome tabella
                try {
                    tableName= (String) in.readObject();
                } catch (ClassNotFoundException | IOException e1) {
                    e1.printStackTrace();
                }
                file=tableName+".dat";
                try {
                    try {
                        trainingSet=new Data(file);
                        knn=new KNN(trainingSet);
                        knn.salva(file+".dmp");
                    } 
                    catch (TrainingDataException e) {
                        System.out.println("file non esistente");}
                    catch(IOException exc) {
                    	System.out.println("Il knn è vuoto");}
                }
                catch(TrainingDataException exc){
                    System.out.println(exc.getMessage());}

                try {
                    out.writeObject(knn.toString());
                } catch (IOException | NullPointerException e) {
                    try {
                        out.writeObject("File inesistente");
                    } catch (IOException e1) {e1.printStackTrace();}
                }

        }
        break;
        //CASO 2(binary file)
        case 2:{
                String file="";
                //riceve nome tabella
                try {
                    tableName= (String) in.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                file=tableName+".dmp";
                try {
                    knn=KNN.carica(file);	
                    System.out.println(knn);
                }
                catch(IOException | ClassNotFoundException exc){System.out.println(exc.getMessage());}
                try {
                    out.writeObject(knn.toString());
                } catch (IOException | NullPointerException e) {
                    try {
                        out.writeObject("File inesistente");
                    } catch (IOException e1) {e1.printStackTrace();}
                }

        }
        break;
        //CASO 3(databse)
        case 3:{
                Data trainingSet=null;
                //riceve nome tabella
                try {
                    tableName= (String) in.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                try {
                    System.out.print("Connecting to DB...");
                    DbAccess db=new DbAccess();
                    System.out.println("done!");
                    trainingSet= new Data(db, tableName);
                    System.out.println(trainingSet);
                    db.closeConnection();
                    
                    //Carico il trainingSet nel knn
                    knn=new KNN(trainingSet);
                    try{
                        knn.salva(tableName+"DB.dmp");}
                    catch(IOException exc) {System.out.println(exc.getMessage());}
                    
                }
                //Gestione eccezione
                catch(InsufficientColumnNumberException | TrainingDataException exc1) {
					System.out.println("Numero di colonne insufficente o trainingset vuoto");}
                //Gestione eccezione connessione database assente
				catch(DatabaseConnectionException exc2) {
					System.out.println("Connessione Database assente");
                    try {
                        out.writeObject("Connessione assente"); 
                        break;
                    } catch (IOException e) {e.printStackTrace();}
                } 
                //Gestore eccezione
				catch (SQLException exc3) {
					System.out.println("Errore di sintassi nell'istruzione SQL");}

                    try {
                        out.writeObject(knn.toString());
                    } catch (IOException | NullPointerException e) {
                        try {
                            out.writeObject("File inesistente");
                        } catch (IOException e1) {e1.printStackTrace();}   
                    }
        }
        break;
    }	//fine switch
    return knn;
    }
}