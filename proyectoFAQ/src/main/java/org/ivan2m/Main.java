package org.ivan2m;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        try {
            udpateIndex();
        }catch(IOException e){
            e.printStackTrace();
        }

        ApiContextInitializer.init();
        //Creamos el bot
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try{
            //Registramos el bot
            botsApi.registerBot(new FaqTfgBot());
        }catch(TelegramApiException e){
            e.printStackTrace();
        }
    }

    /**
     * Llamamos a la clase Indexer para crear o actualizar un índice en caso de que el usuario quiera,
     * porque hay mas archivos de FAQ por ejemplo
     * @throws IOException
     */
    private static void udpateIndex() throws IOException {
        String option = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        do {
            System.out.print("¿Deseas actualizar el índice?(S/n): ");
            System.out.println();

            option = br.readLine();
        }while(!option.equals("S") && !option.equals("s") && !option.equals("N") && !option.equals("n"));

        if(option.equals("S") || option.equals("s")){
            Indexer indexer;

            do {
                System.out.print("¿Ha cambiado alguno de los FAQ que ya estaban indexados?(S/n): ");
                System.out.println();

                option = br.readLine();
                br.close();
            }while(!option.equals("S") && !option.equals("s") && !option.equals("N") && !option.equals("n"));
            br.close();

            if(option.equals("S") || option.equals("s")) {
                indexer = new Indexer(true);
            }
            else{
                indexer = new Indexer(false);
            }

            indexer.createIndex();
            Map<String, Integer> numIndexed = indexer.getIndexInfo();
            indexer.close();

            System.out.println("Hay un total de " + numIndexed.get("totalQuestions") + " preguntas indexadas de " +
                    numIndexed.get("totalFAQs") + " FAQs (archivos), " + numIndexed.get("newFAQs") + " FAQs nuevos");
        }
    }
}
